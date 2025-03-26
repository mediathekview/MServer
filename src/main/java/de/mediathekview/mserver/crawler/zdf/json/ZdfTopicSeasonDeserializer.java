package de.mediathekview.mserver.crawler.zdf.json;

import com.google.gson.*;
import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mserver.base.utils.JsonUtils;
import de.mediathekview.mserver.base.utils.UrlUtils;
import de.mediathekview.mserver.crawler.basic.PagedElementListDTO;
import de.mediathekview.mserver.crawler.zdf.ZdfConstants;
import de.mediathekview.mserver.crawler.zdf.ZdfFilmDto;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ZdfTopicSeasonDeserializer implements JsonDeserializer<PagedElementListDTO<ZdfFilmDto>> {

  private static final Logger LOG = LogManager.getLogger(ZdfTopicSeasonDeserializer.class);

  private static final String PLACEHOLDER_PLAYER_ID = "{playerId}";
  // todo check if this is the correct player id
  private static final String PLAYER_ID = "android_native_5";
  private static final DateTimeFormatter DATE_FORMATTER_EDITORIAL =
      DateTimeFormatter.ofPattern(
          "yyyy-MM-dd'T'HH:mm:ss.SSSSSSXXX"); // 2016-10-29T16:15:00.000+02:00

  private static Optional<LocalDateTime> parseDate(JsonObject episodeObject) {
    // todo Ausstrahlungszeit scheint es nicht zu geben!
    final Optional<String> dateValue =
        JsonUtils.getAttributeAsString(episodeObject, "editorialDate");
    return dateValue.map(s -> LocalDateTime.parse(s, DATE_FORMATTER_EDITORIAL));
  }

  @Override
  public PagedElementListDTO<ZdfFilmDto> deserialize(
      JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext)
      throws JsonParseException {
    final PagedElementListDTO<ZdfFilmDto> films = new PagedElementListDTO<>();

    JsonObject rootNode = jsonElement.getAsJsonObject();
    JsonArray nodes =
        rootNode
            .getAsJsonObject("data")
            .getAsJsonObject("smartCollectionByCanonical")
            .getAsJsonObject("seasons")
            .getAsJsonArray("nodes");

    for (JsonElement element : nodes) {

      final JsonObject episodes = element.getAsJsonObject().getAsJsonObject("episodes");
      final JsonArray episodeNodes = episodes.getAsJsonArray("nodes");
      for (JsonElement episode : episodeNodes) {
        final JsonObject episodeObject = episode.getAsJsonObject();
        final Optional<String> title = parseTitle(episodeObject);
        final Optional<String> website =
            JsonUtils.getAttributeAsString(episodeObject, "sharingUrl");
        final Optional<LocalDateTime> time = parseDate(episodeObject);
        final Optional<String> description =
            JsonUtils.getAttributeAsString(episodeObject.getAsJsonObject("teaser"), "description");
        // todo hier kann ein JSONNull auftreten
        final Optional<String> sender =
            JsonUtils.getAttributeAsString(episodeObject.getAsJsonObject("contentOwner"), "title");

        // streamingoptions relevant, um zu erkennen ob uhd/dgs/ad/ov...?
        final Map<String, String> downloadUrls = new HashMap<>();

        final JsonArray mediaNodes =
            episodeObject.getAsJsonObject("currentMedia").getAsJsonArray("nodes");
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
          films.addElements(
              createFilm(
                  ZdfConstants.PARTNER_TO_SENDER.get(sender.orElse("EMPTY")),
                  title.get(),
                  description,
                  website,
                  time,
                  downloadUrls));
        } else {
          LOG.error("ZdfTopicSeasonDeserializer: no title found");
        }
      }

      films.setNextPage(parseNextPage(episodes.getAsJsonObject("pageInfo")));
    }

    return films;
  }

  private Optional<String> parseNextPage(JsonObject pageInfo) {
    if (!pageInfo.isJsonNull()) {
      final Optional<String> hasNextPage = JsonUtils.getAttributeAsString(pageInfo, "hasNextPage");
      if (hasNextPage.isPresent() && hasNextPage.get().equals("true")) {
        return JsonUtils.getAttributeAsString(pageInfo, "endCursor");
      }
    }
    return Optional.empty();
  }

  private Optional<String> parseTitle(final JsonObject episodeObject) {
    final Optional<String> title = JsonUtils.getAttributeAsString(episodeObject, "title");
    final Optional<String> subtitle = JsonUtils.getAttributeAsString(episodeObject, "subtitle");
    Optional<String> resultingTitle = formatTitle(title, subtitle);

    if (resultingTitle.isPresent()) {
      final Optional<Integer> season =
          JsonUtils.getAttributeAsInt(episodeObject.getAsJsonObject("episodeInfo"), "seasonNumber");
      final Optional<Integer> episode =
          JsonUtils.getAttributeAsInt(
              episodeObject.getAsJsonObject("episodeInfo"), "episodeNumber");
      final Optional<String> seasonEpisodeTitle = formatEpisodeTitle(season, episode);
      return cleanupTitle((resultingTitle.get() + " " + seasonEpisodeTitle.orElse("")).trim());
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

  private Set<ZdfFilmDto> createFilm(
      final Sender sender,
      final String aTitle,
      final Optional<String> aDescription,
      final Optional<String> aWebsite,
      final Optional<LocalDateTime> aTime,
      final Map<String, String> downloadUrls) {

    Set<ZdfFilmDto> films = new HashSet<>();
    if (!downloadUrls.isEmpty()) {
      downloadUrls.forEach(
          (key, url) -> films.add(
              new ZdfFilmDto(
                  sender,
                  aTitle,
                  aDescription.orElse(""),
                  aWebsite.orElse(""),
                  aTime.orElse(LocalDateTime.now()),
                  key.toLowerCase(),
                  url)));
    } else LOG.error("ZdfTopicSeasonDeserializer: no video found");

    return films;
  }

  private String finalizeDownloadUrl(final String url) {
    return UrlUtils.addDomainIfMissing(url, ZdfConstants.URL_API_BASE)
        .replace(PLACEHOLDER_PLAYER_ID, PLAYER_ID);
  }
}
