package mServer.crawler.sender.ard.json;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.mediathekview.mlib.Const;
import de.mediathekview.mlib.daten.DatenFilm;
import java.lang.reflect.Type;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Map;
import java.util.Optional;
import mServer.crawler.CrawlerTool;
import mServer.crawler.sender.ard.ArdConstants;
import mServer.crawler.sender.ard.ArdFilmDto;
import mServer.crawler.sender.ard.ArdFilmInfoDto;
import mServer.crawler.sender.ard.ArdUrlBuilder;
import mServer.crawler.sender.base.JsonUtils;
import mServer.crawler.sender.base.UrlUtils;
import mServer.crawler.sender.newsearch.Qualities;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;

public class ArdFilmDeserializer implements JsonDeserializer<Optional<ArdFilmDto>> {

  private static final org.apache.logging.log4j.Logger LOG = LogManager.getLogger(ArdFilmDeserializer.class);

  private static final String GERMAN_TIME_ZONE = "Europe/Berlin";

  private static final String ELEMENT_DATA = "data";
  private static final String ELEMENT_MEDIA_COLLECTION = "mediaCollection";
  private static final String ELEMENT_PLAYER_PAGE = "playerPage";
  private static final String ELEMENT_RELATES = "relates";
  private static final String ELEMENT_SHOW = "show";

  private static final String ATTRIBUTE_BROADCAST = "broadcastedOn";
  private static final String ATTRIBUTE_DURATION = "_duration";
  private static final String ATTRIBUTE_ID = "id";
  private static final String ATTRIBUTE_SYNOPSIS = "synopsis";
  private static final String ATTRIBUTE_TITLE = "title";

  private static final DateTimeFormatter DATE_FORMAT
          = DateTimeFormatter.ofPattern("dd.MM.yyyy");
  private static final DateTimeFormatter TIME_FORMAT
          = DateTimeFormatter.ofPattern("HH:mm:ss");

  private final ArdVideoInfoJsonDeserializer videoDeserializer;

  public ArdFilmDeserializer() {
    videoDeserializer = new ArdVideoInfoJsonDeserializer();
  }

  @Override
  public Optional<ArdFilmDto> deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context) {

    if (!JsonUtils.checkTreePath(jsonElement, ELEMENT_DATA, ELEMENT_PLAYER_PAGE)) {
      return Optional.empty();
    }

    JsonElement playerPageElement = jsonElement.getAsJsonObject().get(ELEMENT_DATA).getAsJsonObject().get(ELEMENT_PLAYER_PAGE);
    if (playerPageElement.isJsonNull()) {
      return Optional.empty();
    }

    JsonObject playerPageObject = playerPageElement.getAsJsonObject();
    Optional<String> topic = parseTopic(playerPageObject);
    Optional<String> title = JsonUtils.getAttributeAsString(playerPageObject, ATTRIBUTE_TITLE);
    Optional<String> description = JsonUtils.getAttributeAsString(playerPageObject, ATTRIBUTE_SYNOPSIS);
    Optional<LocalDateTime> date = parseDate(playerPageObject);
    Optional<Duration> duration = parseDuration(playerPageObject);
    Optional<ArdVideoInfoDto> videoInfo = parseVideoUrls(playerPageObject);

    if (topic.isPresent() && title.isPresent() && videoInfo.isPresent() && videoInfo.get().getVideoUrls().size() > 0) {
      Optional<DatenFilm> film = createFilm(topic.get(), title.get(), description, date, duration, videoInfo.get());
      if (film.isPresent()) {
        ArdFilmDto filmDto = new ArdFilmDto(film.get());
        parseRelatedFilms(filmDto, playerPageObject);

        return Optional.of(filmDto);
      }
    }

    return Optional.empty();
  }

  private void parseRelatedFilms(ArdFilmDto filmDto, JsonObject playerPageObject) {
    if (playerPageObject.has(ELEMENT_RELATES)) {
      JsonElement relatesElement = playerPageObject.get(ELEMENT_RELATES);
      if (relatesElement.isJsonArray()) {
        for (JsonElement relatesItemElement : relatesElement.getAsJsonArray()) {
          JsonObject relatesItemObject = relatesItemElement.getAsJsonObject();
          Optional<String> id = JsonUtils.getAttributeAsString(relatesItemObject, ATTRIBUTE_ID);
          if (id.isPresent()) {
            final String url = new ArdUrlBuilder(ArdConstants.BASE_URL, ArdConstants.DEFAULT_CLIENT)
                    .addClipId(id.get(), ArdConstants.DEFAULT_DEVICE)
                    .addSavedQuery(ArdConstants.QUERY_FILM_VERSION, ArdConstants.QUERY_FILM_HASH)
                    .build();

            filmDto.addRelatedFilm(new ArdFilmInfoDto(id.get(), url, 0));
          }
        }
      }
    }
  }

  private static Optional<JsonObject> getMediaCollectionObject(final JsonObject playerPageObject) {
    if (playerPageObject.has(ELEMENT_MEDIA_COLLECTION) && !playerPageObject.get(ELEMENT_MEDIA_COLLECTION).isJsonNull()) {
      return Optional.of(playerPageObject.get(ELEMENT_MEDIA_COLLECTION).getAsJsonObject());
    }

    return Optional.empty();
  }

  private Optional<DatenFilm> createFilm(String topic, String title, Optional<String> description,
          Optional<LocalDateTime> date, Optional<Duration> duration, ArdVideoInfoDto videoInfo) {

    LocalDateTime time = date.orElse(null);
    String datum = time.format(DATE_FORMAT);
    String zeit = time.format(TIME_FORMAT);

    final DatenFilm film = new DatenFilm(Const.ARD, topic, "", title,
            videoInfo.getDefaultVideoUrl(), "", datum, zeit,
            duration.orElse(Duration.ZERO).getSeconds(),
            description.orElse(""));

    if (StringUtils.isNotBlank(videoInfo.getSubtitleUrl())) {
      CrawlerTool.addUrlSubtitle(film, prepareSubtitleUrl(videoInfo.getSubtitleUrl()));
    }

    addUrls(film, videoInfo.getVideoUrls());

    return Optional.of(film);
  }

  private void addUrls(final DatenFilm aFilm, final Map<Qualities, String> aVideoUrls) {
    if (aVideoUrls.containsKey(Qualities.HD)) {
      CrawlerTool.addUrlHd(aFilm, aVideoUrls.get(Qualities.HD), "");
    }
    if (aVideoUrls.containsKey(Qualities.SMALL)) {
      CrawlerTool.addUrlKlein(aFilm, aVideoUrls.get(Qualities.SMALL), "");
    }
  }

  private static String prepareSubtitleUrl(final String url) {
    return UrlUtils.addDomainIfMissing(url, ArdConstants.BASE_URL_SUBTITLES);
  }

  private static Optional<String> parseTopic(final JsonObject playerPageObject) {
    if (playerPageObject.has(ELEMENT_SHOW)) {
      JsonObject showObject = playerPageObject.get(ELEMENT_SHOW).getAsJsonObject();
      return JsonUtils.getAttributeAsString(showObject, ATTRIBUTE_TITLE);
    }

    return Optional.empty();
  }

  private static Optional<LocalDateTime> parseDate(final JsonObject playerPageObject) {
    Optional<String> dateValue = JsonUtils.getAttributeAsString(playerPageObject, ATTRIBUTE_BROADCAST);
    if (dateValue.isPresent()) {
      try {
        final ZonedDateTime inputDateTime = ZonedDateTime.parse(dateValue.get());
        LocalDateTime localDateTime = inputDateTime.withZoneSameInstant(ZoneId.of(GERMAN_TIME_ZONE)).toLocalDateTime();
        return Optional.of(localDateTime);
      } catch (DateTimeParseException ex) {
        LOG.error("Error parsing date time value " + dateValue.get(), ex);
      }
    }

    return Optional.empty();
  }

  private static Optional<Duration> parseDuration(final JsonObject playerPageObject) {
    final Optional<JsonObject> mediaCollectionObject = getMediaCollectionObject(playerPageObject);
    if (mediaCollectionObject.isPresent() && mediaCollectionObject.get().has(ATTRIBUTE_DURATION)) {
      long durationValue = mediaCollectionObject.get().get(ATTRIBUTE_DURATION).getAsLong();
      return Optional.of(Duration.ofSeconds(durationValue));
    }

    return Optional.empty();
  }

  private Optional<ArdVideoInfoDto> parseVideoUrls(final JsonObject playerPageObject) {
    final Optional<JsonObject> mediaCollectionObject = getMediaCollectionObject(playerPageObject);
    if (mediaCollectionObject.isPresent()) {
      ArdVideoInfoDto videoDto = videoDeserializer.deserialize(mediaCollectionObject.get(), null, null);
      return Optional.of(videoDto);
    }

    return Optional.empty();
  }
}
