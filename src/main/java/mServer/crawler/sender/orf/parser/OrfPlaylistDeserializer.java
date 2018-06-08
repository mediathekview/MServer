package mServer.crawler.sender.orf.parser;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.lang.reflect.Type;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import mServer.crawler.sender.orf.JsonUtils;
import mServer.crawler.sender.orf.OrfEpisodeInfoDTO;
import mServer.crawler.sender.orf.OrfVideoInfoDTO;

public class OrfPlaylistDeserializer implements JsonDeserializer<List<OrfEpisodeInfoDTO>> {

  private static final String ELEMENT_GAPLESS_VIDEO = "gapless_video";
  private static final String ELEMENT_PLAYLIST = "playlist";
  private static final String ELEMENT_VIDEOS = "videos";

  private static final String ATTRIBUTE_TITLE = "title";
  private static final String ATTRIBUTE_DESCRIPTION = "description";
  private static final String ATTRIBUTE_DURATION = "duration";
  private static final String ATTRIBUTE_DURATION_IN_SECONDS = "duration_in_seconds";

  @Override
  public List<OrfEpisodeInfoDTO> deserialize(JsonElement aJsonElement, Type aType, JsonDeserializationContext aContext) {

    List<OrfEpisodeInfoDTO> episodes = new ArrayList<>();

    if (!aJsonElement.getAsJsonObject().has(ELEMENT_PLAYLIST)) {
      return episodes;
    }

    JsonObject playlistObject = aJsonElement.getAsJsonObject().get(ELEMENT_PLAYLIST).getAsJsonObject();
    if (JsonUtils.hasElements(playlistObject, ELEMENT_GAPLESS_VIDEO)) {
      parseGaplessVideo(episodes, playlistObject);
    }

    parseVideos(episodes, playlistObject);

    return episodes;
  }

  private void parseGaplessVideo(List<OrfEpisodeInfoDTO> aEpisodes, JsonObject aPlaylistObject) {

    final Optional<String> title = JsonUtils.getAttributeAsString(aPlaylistObject, ATTRIBUTE_TITLE);
    final Optional<Duration> duration = parseDurationInSeconds(aPlaylistObject);

    final Optional<OrfVideoInfoDTO> videoInfoOptional = parseUrls(aPlaylistObject.getAsJsonObject(ELEMENT_GAPLESS_VIDEO));

    if (videoInfoOptional.isPresent()) {
      OrfEpisodeInfoDTO episode = new OrfEpisodeInfoDTO(videoInfoOptional.get(), title, Optional.empty(), duration);
      aEpisodes.add(episode);
    }
  }

  private void parseVideos(List<OrfEpisodeInfoDTO> aEpisodes, JsonObject aPlaylistObject) {
    JsonArray videosArray = aPlaylistObject.getAsJsonObject().get(ELEMENT_VIDEOS).getAsJsonArray();

    for (JsonElement videoElement : videosArray) {
      JsonObject videoObject = videoElement.getAsJsonObject();
      final Optional<String> title = JsonUtils.getAttributeAsString(videoObject, ATTRIBUTE_TITLE);
      final Optional<String> description = JsonUtils.getAttributeAsString(videoObject, ATTRIBUTE_DESCRIPTION);
      final Optional<Duration> duration = parseDuration(videoObject);

      final Optional<OrfVideoInfoDTO> videoInfoOptional = parseUrls(videoObject);

      if (videoInfoOptional.isPresent()) {
        OrfEpisodeInfoDTO episode = new OrfEpisodeInfoDTO(videoInfoOptional.get(), title, description, duration);
        aEpisodes.add(episode);
      }
    }
  }

  private Optional<OrfVideoInfoDTO> parseUrls(final JsonObject aVideoObject) {

    OrfVideoDetailDeserializer deserializer = new OrfVideoDetailDeserializer();
    return deserializer.deserializeVideoObject(aVideoObject);
  }

  private static Optional<Duration> parseDuration(final JsonObject aVideoObject) {
    if (aVideoObject.has(ATTRIBUTE_DURATION)) {
      Long durationValue = aVideoObject.get(ATTRIBUTE_DURATION).getAsLong();

      // Duration ist in Millisekunden angegeben, diese interessieren aber nicht
      return Optional.of(Duration.ofSeconds(durationValue / 1000));
    }

    return Optional.empty();
  }

  private static Optional<Duration> parseDurationInSeconds(final JsonObject aVideoObject) {
    if (aVideoObject.has(ATTRIBUTE_DURATION_IN_SECONDS)) {
      Double durationValue = aVideoObject.get(ATTRIBUTE_DURATION_IN_SECONDS).getAsDouble();

      return Optional.of(Duration.ofSeconds(durationValue.longValue()));
    }

    return Optional.empty();
  }
}
