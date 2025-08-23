package mServer.crawler.sender.zdf.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.mediathekview.mlib.tool.Log;
import mServer.crawler.sender.base.JsonUtils;
import mServer.crawler.sender.base.UrlUtils;
import mServer.crawler.sender.zdf.ZdfConstants;
import mServer.crawler.sender.zdf.ZdfFilmDto;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class ZdfTopicBaseClass {
  private static final String PLACEHOLDER_PLAYER_ID = "{playerId}";
  private static final String PLAYER_ID = "android_native_5";

  private static final Logger LOG = LogManager.getLogger(ZdfTopicBaseClass.class);

  private static final DateTimeFormatter DATE_FORMATTER_EDITORIAL =
      DateTimeFormatter.ofPattern(
          "yyyy-MM-dd'T'HH:mm:ss.SSSSSSXXX"); // 2016-10-29T16:15:00.000+02:00

  private static Optional<LocalDateTime> parseDate(JsonObject episodeObject) {
    final Optional<String> dateValue =
        JsonUtils.getAttributeAsString(episodeObject, "editorialDate");
    return dateValue.map(s -> LocalDateTime.parse(s, DATE_FORMATTER_EDITORIAL));
  }

  protected Set<ZdfFilmDto> deserializeMovie(JsonElement episode) {
    final JsonObject episodeObject = episode.getAsJsonObject();
    final Optional<String> title = parseTitle(episodeObject);
    final Optional<String> website = JsonUtils.getAttributeAsString(episodeObject, "sharingUrl");
    final Optional<LocalDateTime> time = parseDate(episodeObject);
    final Optional<String> description =
        JsonUtils.getAttributeAsString(episodeObject.getAsJsonObject("teaser"), "description");
    final Optional<String> sender = parseSender(episodeObject);

    // streamingoptions relevant, um zu erkennen ob uhd/dgs/ad/ov...?
    final Map<String, String> downloadUrls = new HashMap<>();

    final JsonArray mediaNodes = getMediaNodes(episodeObject);
    for (JsonElement media : mediaNodes) {
      final JsonObject mediaObject = media.getAsJsonObject();
      final Optional<String> mediaType =
          JsonUtils.getAttributeAsString(mediaObject, "vodMediaType");
      final Optional<String> url = JsonUtils.getAttributeAsString(mediaObject, "ptmdTemplate");
      if (mediaType.isPresent() && url.isPresent()) {
        downloadUrls.put(mediaType.get(), finalizeDownloadUrl(url.get()));
      }
    }

    if (title.isPresent()) {
      String senderValue = sender.orElse("EMPTY");
      if (ZdfConstants.PARTNER_TO_SENDER.containsKey(senderValue)) {
        return createFilm(
                ZdfConstants.PARTNER_TO_SENDER.get(senderValue),
                title.get(),
                description,
                website,
                time,
                downloadUrls);
      } else {
        Log.sysLog("ZDF: unsupported sender: " + senderValue + " for title " + title.get());
      }
    } else {
      LOG.error("ZdfTopicSeasonDeserializer: no title found");
    }
    return Collections.emptySet();
  }

  private Optional<String> parseTitle(final JsonObject episodeObject) {
    final Optional<String> title = JsonUtils.getAttributeAsString(episodeObject, "title");
    final Optional<String> subtitle = JsonUtils.getAttributeAsString(episodeObject, "subtitle");
    Optional<String> resultingTitle = formatTitle(title, subtitle);

    if (resultingTitle.isPresent()) {
      if (episodeObject.has("episodeInfo")) {
        final Optional<Integer> season =
            JsonUtils.getAttributeAsInt(
                episodeObject.getAsJsonObject("episodeInfo"), "seasonNumber");
        final Optional<Integer> episode =
            JsonUtils.getAttributeAsInt(
                episodeObject.getAsJsonObject("episodeInfo"), "episodeNumber");
        final Optional<String> seasonEpisodeTitle = formatEpisodeTitle(season, episode);
        return cleanupTitle((resultingTitle.get() + " " + seasonEpisodeTitle.orElse("")).trim());
      } else {
        return cleanupTitle(resultingTitle.get());
      }
    }
    return Optional.empty();
  }

  private Optional<String> formatTitle(Optional<String> title, Optional<String> sub) {
    if (title.isEmpty()) {
      return Optional.empty();
    }
    if (sub.isPresent() && !sub.get().trim().isEmpty()) {
      return Optional.of(title.get().trim() + " - " + sub.get().trim());
    } else {
      return Optional.of(title.get().trim());
    }
  }

  private Optional<String> formatEpisodeTitle(Optional<Integer> season, Optional<Integer> episode) {
    if (season.isEmpty() && episode.isEmpty()) {
      return Optional.empty();
    }
    String result = "";
    if (season.isPresent()) {
      result += String.format("S%02d", season.get());
    }
    if (season.isPresent() && episode.isPresent()) {
      result += "/";
    }
    if (episode.isPresent()) {
      result += String.format("E%02d", episode.get());
    }
    return Optional.of("(" + result + ")");
  }

  private Optional<String> cleanupTitle(String title) {
    return Optional.of(title.replaceAll("\\(CC.*\\) - .* Creative Commons.*", ""));
  }

  private JsonArray getMediaNodes(JsonObject episodeObject) {
    JsonObject videoRootObject = episodeObject;
     if (episodeObject.has("video") && !episodeObject.get("video").isJsonNull()) {
      videoRootObject = episodeObject.getAsJsonObject("video");
    }
    if (!videoRootObject.has("currentMedia")) {
      return new JsonArray();
    }
    return videoRootObject.getAsJsonObject("currentMedia").getAsJsonArray("nodes");
  }

  private Optional<String> parseSender(JsonObject episodeObject) {

    if (!episodeObject.has("tracking")) {
      return Optional.empty();
    }
    final Optional<JsonElement> trackingVideoElement =
        JsonUtils.getElement(episodeObject, "tracking", "piano", "video");
    if (trackingVideoElement.isEmpty() || trackingVideoElement.get().isJsonNull()) {
      return Optional.empty();
    }

    final JsonObject trackingVideoObject = trackingVideoElement.get().getAsJsonObject();
    Optional<String> sender =
        JsonUtils.getAttributeAsString(trackingVideoObject, "av_broadcastdetail");
    if (sender.isEmpty()) {
      sender = JsonUtils.getAttributeAsString(trackingVideoObject, "av_broadcaster");
    }
    return sender;
  }

  private Set<ZdfFilmDto> createFilm(
      final String sender,
      final String aTitle,
      final Optional<String> aDescription,
      final Optional<String> aWebsite,
      final Optional<LocalDateTime> aTime,
      final Map<String, String> downloadUrls) {

    Set<ZdfFilmDto> films = new HashSet<>();
    if (!downloadUrls.isEmpty()) {
      downloadUrls.forEach(
          (key, url) ->
              films.add(
                  new ZdfFilmDto(
                      sender,
                      aTitle,
                      aDescription.orElse(""),
                      aWebsite.orElse(""),
                      aTime.orElse(LocalDateTime.now()),
                      key.toLowerCase(),
                      url)));
    } else {
      LOG.error("ZdfTopicSeasonDeserializer: no video found for {}: {}", sender, aTitle);
    }

    return films;
  }

  private String finalizeDownloadUrl(final String url) {
    return UrlUtils.addDomainIfMissing(url, ZdfConstants.URL_API_BASE)
        .replace(PLACEHOLDER_PLAYER_ID, PLAYER_ID);
  }
}
