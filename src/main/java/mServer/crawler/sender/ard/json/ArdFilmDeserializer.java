package mServer.crawler.sender.ard.json;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.mediathekview.mlib.Const;
import de.mediathekview.mlib.daten.DatenFilm;
import mServer.crawler.CrawlerTool;
import mServer.crawler.sender.ard.ArdConstants;
import mServer.crawler.sender.ard.ArdFilmDto;
import mServer.crawler.sender.ard.ArdFilmInfoDto;
import mServer.crawler.sender.ard.ArdUrlBuilder;
import mServer.crawler.sender.base.JsonUtils;
import mServer.crawler.sender.base.UrlUtils;
import mServer.crawler.sender.newsearch.Qualities;
import org.apache.logging.log4j.LogManager;

import java.lang.reflect.Type;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

public class ArdFilmDeserializer implements JsonDeserializer<List<ArdFilmDto>> {

  private static final org.apache.logging.log4j.Logger LOG
          = LogManager.getLogger(ArdFilmDeserializer.class);

  private static final String GERMAN_TIME_ZONE = "Europe/Berlin";

  private static final String ELEMENT_DATA = "data";
  private static final String ELEMENT_MEDIA_COLLECTION = "mediaCollection";
  private static final String ELEMENT_PLAYER_PAGE = "playerPage";
  private static final String ELEMENT_PUBLICATION_SERVICE = "publicationService";
  private static final String ELEMENT_RELATES = "relates";
  private static final String ELEMENT_SHOW = "show";

  private static final String ATTRIBUTE_BROADCAST = "broadcastedOn";
  private static final String ATTRIBUTE_CHANNEL_TYPE = "channelType";
  private static final String ATTRIBUTE_DURATION = "_duration";
  private static final String ATTRIBUTE_ID = "id";
  private static final String ATTRIBUTE_NAME = "name";
  private static final String ATTRIBUTE_SYNOPSIS = "synopsis";
  private static final String ATTRIBUTE_TITLE = "title";

  private static final DateTimeFormatter DATE_FORMAT
          = DateTimeFormatter.ofPattern("dd.MM.yyyy");
  private static final DateTimeFormatter TIME_FORMAT
          = DateTimeFormatter.ofPattern("HH:mm:ss");

  // the key of the map is the value of publicationService.channelType in film.json
  private static final Map<String, String> ADDITIONAL_SENDER = new HashMap<>();

  static {
    ADDITIONAL_SENDER.put("rbb", Const.RBB);
    ADDITIONAL_SENDER.put("swr", Const.SWR);
    ADDITIONAL_SENDER.put("mdr", Const.MDR);
    ADDITIONAL_SENDER.put("ndr", Const.NDR);
    ADDITIONAL_SENDER.put("radiobremen", "rbtv");
  }

  private final ArdVideoInfoJsonDeserializer videoDeserializer;

  public ArdFilmDeserializer() {
    videoDeserializer = new ArdVideoInfoJsonDeserializer();
  }

  private static Optional<JsonObject> getMediaCollectionObject(final JsonObject playerPageObject) {
    if (playerPageObject.has(ELEMENT_MEDIA_COLLECTION)
            && !playerPageObject.get(ELEMENT_MEDIA_COLLECTION).isJsonNull()) {
      return Optional.of(playerPageObject.get(ELEMENT_MEDIA_COLLECTION).getAsJsonObject());
    }

    return Optional.empty();
  }

  private static Optional<String> parseTopic(final JsonObject playerPageObject) {
    if (playerPageObject.has(ELEMENT_SHOW) && !playerPageObject.get(ELEMENT_SHOW).isJsonNull()) {
      final JsonObject showObject = playerPageObject.get(ELEMENT_SHOW).getAsJsonObject();
      return JsonUtils.getAttributeAsString(showObject, ATTRIBUTE_TITLE);
    }

    // no show element found -> use title as topic
    return JsonUtils.getAttributeAsString(playerPageObject, ATTRIBUTE_TITLE);
  }

  private Optional<String> parseTitle(final JsonObject playerPageObject) {
    Optional<String> title = JsonUtils.getAttributeAsString(playerPageObject, ATTRIBUTE_TITLE);
    if (title.isPresent()) {
      return Optional.of(title.get().replace("Hörfassung", "Audiodeskription"));
    }

    return title;
  }

  private static Optional<LocalDateTime> parseDate(final JsonObject playerPageObject) {
    final Optional<String> dateValue
            = JsonUtils.getAttributeAsString(playerPageObject, ATTRIBUTE_BROADCAST);
    if (dateValue.isPresent()) {
      try {
        final ZonedDateTime inputDateTime = ZonedDateTime.parse(dateValue.get());
        final LocalDateTime localDateTime
                = inputDateTime.withZoneSameInstant(ZoneId.of(GERMAN_TIME_ZONE)).toLocalDateTime();
        return Optional.of(localDateTime);
      } catch (final DateTimeParseException ex) {
        LOG.error("Error parsing date time value " + dateValue.get(), ex);
      }
    }

    return Optional.empty();
  }

  private static Optional<Duration> parseDuration(final JsonObject playerPageObject) {
    final Optional<JsonObject> mediaCollectionObject = getMediaCollectionObject(playerPageObject);
    if (mediaCollectionObject.isPresent() && mediaCollectionObject.get().has(ATTRIBUTE_DURATION)) {
      final long durationValue = mediaCollectionObject.get().get(ATTRIBUTE_DURATION).getAsLong();
      return Optional.of(Duration.ofSeconds(durationValue));
    }

    return Optional.empty();
  }

  @Override
  public List<ArdFilmDto> deserialize(
          final JsonElement jsonElement, final Type type, final JsonDeserializationContext context) {

    List<ArdFilmDto> films = new ArrayList<>();

    if (!JsonUtils.checkTreePath(
            jsonElement, ELEMENT_DATA, ELEMENT_PLAYER_PAGE)) {
      return films;
    }

    final JsonElement playerPageElement
            = jsonElement.getAsJsonObject().get(ELEMENT_DATA).getAsJsonObject().get(ELEMENT_PLAYER_PAGE);
    if (playerPageElement.isJsonNull()) {
      return films;
    }

    final JsonObject playerPageObject = playerPageElement.getAsJsonObject();
    final Optional<String> topic = parseTopic(playerPageObject);
    final Optional<String> title = parseTitle(playerPageObject);
    final Optional<String> description
            = JsonUtils.getAttributeAsString(playerPageObject, ATTRIBUTE_SYNOPSIS);
    final Optional<LocalDateTime> date = parseDate(playerPageObject);
    final Optional<Duration> duration = parseDuration(playerPageObject);
    final Optional<ArdVideoInfoDto> videoInfo = parseVideoUrls(playerPageObject);
    final Optional<String> channelType = parseChannelType(playerPageObject);

    if (topic.isPresent()
            && title.isPresent()
            && videoInfo.isPresent()
            && videoInfo.get().getVideoUrls().size() > 0) {
      // add film to ARD
      final ArdFilmDto filmDto
              = new ArdFilmDto(
                      createFilm(
                              Const.ARD,
                              topic.get(),
                              title.get(),
                              description,
                              date,
                              duration,
                              videoInfo.get()));
      parseRelatedFilms(filmDto, playerPageObject);
      films.add(filmDto);

      if (channelType.isPresent() && ADDITIONAL_SENDER.containsKey(channelType.get())) {
        // add film to other sender (like RBB)
        DatenFilm additionalFilm
                = createFilm(
                        ADDITIONAL_SENDER.get(channelType.get()),
                        topic.get(),
                        title.get(),
                        description,
                        date,
                        duration,
                        videoInfo.get());
        films.add(new ArdFilmDto(additionalFilm));
      }
    }

    return films;
  }

  private Optional<String> parseChannelType(JsonObject playerPageObject) {
    if (playerPageObject.has(ELEMENT_PUBLICATION_SERVICE)) {
      JsonObject publicationServiceObject
              = playerPageObject.get(ELEMENT_PUBLICATION_SERVICE).getAsJsonObject();
      Optional<String> channelAttribute = JsonUtils
              .getAttributeAsString(publicationServiceObject, ATTRIBUTE_CHANNEL_TYPE);
      if (channelAttribute.isPresent()) {
        return channelAttribute;
      }

      Optional<String> nameAttribute = JsonUtils
              .getAttributeAsString(publicationServiceObject, ATTRIBUTE_NAME);
      if (nameAttribute.isPresent()) {
        return Optional.of(nameAttribute.get().split(" ")[0]);
      }
    }

    return Optional.empty();
  }

  private static String prepareSubtitleUrl(final String url) {
    return UrlUtils.addDomainIfMissing(url, ArdConstants.BASE_URL_SUBTITLES);
  }

  private void parseRelatedFilms(final ArdFilmDto filmDto, final JsonObject playerPageObject) {
    if (playerPageObject.has(ELEMENT_RELATES)) {
      final JsonElement relatesElement = playerPageObject.get(ELEMENT_RELATES);
      if (relatesElement.isJsonArray()) {
        for (final JsonElement relatesItemElement : relatesElement.getAsJsonArray()) {
          final JsonObject relatesItemObject = relatesItemElement.getAsJsonObject();
          final Optional<String> id
                  = JsonUtils.getAttributeAsString(relatesItemObject, ATTRIBUTE_ID);
          if (id.isPresent()) {
            final String url
                    = new ArdUrlBuilder(ArdConstants.BASE_URL, ArdConstants.DEFAULT_CLIENT)
                            .addClipId(id.get(), ArdConstants.DEFAULT_DEVICE)
                            .addSavedQuery(ArdConstants.QUERY_FILM_VERSION, ArdConstants.QUERY_FILM_HASH)
                            .build();

            filmDto.addRelatedFilm(new ArdFilmInfoDto(id.get(), url, 0));
          }
        }
      }
    }
  }

  private DatenFilm createFilm(
          final String sender,
          final String topic,
          final String title,
          final Optional<String> description,
          final Optional<LocalDateTime> date,
          final Optional<Duration> duration,
          final ArdVideoInfoDto videoInfo) {

    LocalDateTime time = date.orElse(LocalDateTime.now());

    String dateValue = time.format(DATE_FORMAT);
    String timeValue = time.format(TIME_FORMAT);

    Map<Qualities, String> videoUrls = videoInfo.getVideoUrls();

    DatenFilm film = new DatenFilm(sender, topic, "", title, videoInfo.getDefaultVideoUrl(), "", dateValue, timeValue, duration.orElse(Duration.ZERO).getSeconds(), description.orElse(""));
    if (videoUrls.containsKey(Qualities.SMALL)) {
      CrawlerTool.addUrlKlein(film, videoUrls.get(Qualities.SMALL));
    }
    if (videoUrls.containsKey(Qualities.HD)) {
      CrawlerTool.addUrlHd(film, videoUrls.get(Qualities.HD));
    }
    if (videoInfo.getSubtitleUrlOptional().isPresent()) {
      CrawlerTool.addUrlSubtitle(film, videoInfo.getSubtitleUrl());
    }

    return film;
  }

  private Optional<ArdVideoInfoDto> parseVideoUrls(final JsonObject playerPageObject) {
    final Optional<JsonObject> mediaCollectionObject = getMediaCollectionObject(playerPageObject);
    if (mediaCollectionObject.isPresent()) {
      final ArdVideoInfoDto videoDto
              = videoDeserializer.deserialize(mediaCollectionObject.get(), null, null);
      return Optional.of(videoDto);
    }

    return Optional.empty();
  }
}
