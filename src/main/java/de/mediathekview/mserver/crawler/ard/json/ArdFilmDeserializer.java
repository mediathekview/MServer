package de.mediathekview.mserver.crawler.ard.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.FilmUrl;
import de.mediathekview.mlib.daten.Resolution;
import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mserver.base.utils.GeoLocationGuesser;
import de.mediathekview.mserver.base.utils.JsonUtils;
import de.mediathekview.mserver.base.utils.UrlUtils;
import de.mediathekview.mserver.crawler.ard.ArdConstants;
import de.mediathekview.mserver.crawler.ard.ArdFilmDto;
import de.mediathekview.mserver.crawler.ard.ArdFilmInfoDto;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;

import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class ArdFilmDeserializer implements JsonDeserializer<List<ArdFilmDto>> {

  private static final org.apache.logging.log4j.Logger LOG =
    LogManager.getLogger(ArdFilmDeserializer.class);

  private static final String GERMAN_TIME_ZONE = "Europe/Berlin";

  private static final String ELEMENT_EMBEDDED = "embedded";
  private static final String ELEMENT_MEDIA_COLLECTION = "mediaCollection";
  private static final String ELEMENT_PUBLICATION_SERVICE = "publicationService";
  private static final String ELEMENT_SHOW = "show";
  private static final String ELEMENT_TEASERS = "teasers";
  private static final String ELEMENT_WIDGETS = "widgets";

  private static final String ATTRIBUTE_BROADCAST = "broadcastedOn";
  private static final String ATTRIBUTE_DURATION = "_duration";
  private static final String ATTRIBUTE_ID = "id";
  private static final String ATTRIBUTE_NAME = "name";
  private static final String ATTRIBUTE_PARTNER = "partner";
  private static final String ATTRIBUTE_SYNOPSIS = "synopsis";
  private static final String ATTRIBUTE_TITLE = "title";

  private final ArdVideoInfoJsonDeserializer videoDeserializer;
  private final AbstractCrawler crawler;

  public ArdFilmDeserializer(final AbstractCrawler crawler) {
    videoDeserializer = new ArdVideoInfoJsonDeserializer(crawler);
    this.crawler = crawler;
  }

  private static Optional<JsonObject> getMediaCollectionObject(final JsonObject itemObject) {
    if (itemObject.has(ELEMENT_MEDIA_COLLECTION)
      && !itemObject.get(ELEMENT_MEDIA_COLLECTION).isJsonNull()
      && itemObject.getAsJsonObject(ELEMENT_MEDIA_COLLECTION).has(ELEMENT_EMBEDDED)
      && !itemObject.getAsJsonObject(ELEMENT_MEDIA_COLLECTION).get(ELEMENT_EMBEDDED).isJsonNull()) {

      return Optional.of(itemObject.getAsJsonObject(ELEMENT_MEDIA_COLLECTION).getAsJsonObject(ELEMENT_EMBEDDED));
    }

    return Optional.empty();
  }

  private static Optional<String> parseTopic(final JsonObject playerPageObject) {
    Optional<String> topic;
    if (playerPageObject.has(ELEMENT_SHOW) && !playerPageObject.get(ELEMENT_SHOW).isJsonNull()) {
      final JsonObject showObject = playerPageObject.get(ELEMENT_SHOW).getAsJsonObject();
      topic = JsonUtils.getAttributeAsString(showObject, ATTRIBUTE_TITLE);
    } else {
      // no show element found -> use title as topic
      topic = JsonUtils.getAttributeAsString(playerPageObject, ATTRIBUTE_TITLE);
    }

    if (topic.isPresent() &&
        topic.get().contains("MDR aktuell")) {
      // remove time in topic
      return Optional.of(topic.get().replaceAll("[0-9][0-9]:[0-9][0-9] Uhr$", "").trim());
    }

    return topic;
  }

  private static Optional<LocalDateTime> parseDate(final JsonObject playerPageObject) {
    final Optional<String> dateValue =
      JsonUtils.getAttributeAsString(playerPageObject, ATTRIBUTE_BROADCAST);
    if (dateValue.isPresent()) {
      try {
        final ZonedDateTime inputDateTime = ZonedDateTime.parse(dateValue.get());
        final LocalDateTime localDateTime =
          inputDateTime.withZoneSameInstant(ZoneId.of(GERMAN_TIME_ZONE)).toLocalDateTime();
        return Optional.of(localDateTime);
      } catch (final DateTimeParseException ex) {
        LOG.error("Error parsing date time value '{}'",dateValue.get(), ex);
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

    final List<ArdFilmDto> films = new ArrayList<>();

    if (!JsonUtils.hasElements(jsonElement, ELEMENT_WIDGETS)
      || !jsonElement.getAsJsonObject().get(ELEMENT_WIDGETS).isJsonArray()) {
      return films;
    }

    final JsonArray widgets = jsonElement.getAsJsonObject().getAsJsonArray(ELEMENT_WIDGETS);
    if (widgets.size() == 0) {
      return films;
    }

    final JsonObject itemObject = widgets.get(0).getAsJsonObject();

    final Optional<String> topic = parseTopic(itemObject);
    final Optional<String> title = parseTitle(itemObject);
    final Optional<String> description =
      JsonUtils.getAttributeAsString(itemObject, ATTRIBUTE_SYNOPSIS);
    final Optional<LocalDateTime> date = parseDate(itemObject);
    final Optional<Duration> duration = parseDuration(itemObject);
    final Optional<ArdVideoInfoDto> videoInfo = parseVideoUrls(itemObject);
    final Optional<String> partner = parsePartner(itemObject);

    if (topic.isPresent()
      && title.isPresent()
      && videoInfo.isPresent()
      && videoInfo.get().getVideoUrls().size() > 0) {

      final Sender sender;
      // If partner is present and a existing sender set it. Like for RBB
      if (partner.isPresent()) {
        final Optional<Sender> additionalSender = Sender.getSenderByName(partner.get());
        sender = additionalSender.orElse(Sender.ARD);
      } else {
        sender = Sender.ARD;
      }

      // add film to ARD
      final ArdFilmDto filmDto =
        new ArdFilmDto(
          createFilm(
            sender, topic.get(), title.get(), description, date, duration, videoInfo.get()));
      if (widgets.size() > 1) {
        parseRelatedFilms(filmDto, widgets.get(1).getAsJsonObject());
      }
      films.add(filmDto);
    }

    return films;
  }

  private Optional<String> parseTitle(final JsonObject playerPageObject) {
    Optional<String> title = JsonUtils.getAttributeAsString(playerPageObject, ATTRIBUTE_TITLE);
    if (title.isPresent()) {
      return Optional.of(title.get().replace("Hörfassung", "Audiodeskription"));
    }

    return title;
  }

  private Optional<String> parsePartner(final JsonObject playerPageObject) {
    if (playerPageObject.has(ELEMENT_PUBLICATION_SERVICE)) {
      final JsonObject publicationServiceObject =
        playerPageObject.get(ELEMENT_PUBLICATION_SERVICE).getAsJsonObject();
      final Optional<String> channelAttribute =
        JsonUtils.getAttributeAsString(publicationServiceObject, ATTRIBUTE_PARTNER);
      if (channelAttribute.isPresent()) {
        return channelAttribute;
      }

      final Optional<String> nameAttribute =
        JsonUtils.getAttributeAsString(publicationServiceObject, ATTRIBUTE_NAME);
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
    if (playerPageObject.has(ELEMENT_TEASERS)) {
      final JsonElement teasersElement = playerPageObject.get(ELEMENT_TEASERS);
      if (teasersElement.isJsonArray()) {
        for (final JsonElement teasersItemElement : teasersElement.getAsJsonArray()) {
          final JsonObject teasersItemObject = teasersItemElement.getAsJsonObject();
          final Optional<String> id =
            JsonUtils.getAttributeAsString(teasersItemObject, ATTRIBUTE_ID);
          if (id.isPresent()) {
            final String url = ArdConstants.ITEM_URL + id.get();

            filmDto.addRelatedFilm(new ArdFilmInfoDto(id.get(), url, 0));
          }
        }
      }
    }
  }

  private Film createFilm(
    final Sender sender,
    final String topic,
    final String title,
    final Optional<String> description,
    final Optional<LocalDateTime> date,
    final Optional<Duration> duration,
    final ArdVideoInfoDto videoInfo) {

    final Film film =
      new Film(
        UUID.randomUUID(),
        sender,
        title,
        topic,
        date.orElse(null),
        duration.orElse(Duration.ofSeconds(0)));

    description.ifPresent(film::setBeschreibung);

    film.setGeoLocations(
      GeoLocationGuesser.getGeoLocations(Sender.ARD, videoInfo.getDefaultVideoUrl()));
    if (StringUtils.isNotBlank(videoInfo.getSubtitleUrl())) {
      try {
        film.addSubtitle(new URL(prepareSubtitleUrl(videoInfo.getSubtitleUrl())));
      } catch (final MalformedURLException ex) {
        LOG.error("{}, {}, {} Invalid subtitle url: {}", topic, title, date.toString(), videoInfo.getSubtitleUrl(), ex); 
      }
    }
    addUrls(film, videoInfo.getVideoUrls());

    return film;
  }

  private void addUrls(final Film film, final Map<Resolution, String> videoUrls) {
    for (final Map.Entry<Resolution, String> qualitiesEntry : videoUrls.entrySet()) {
      final String url = qualitiesEntry.getValue();
      try {
        film.addUrl(
          qualitiesEntry.getKey(),
          new FilmUrl(url, crawler.determineFileSizeInKB(url)));
      } catch (final MalformedURLException ex) {
        LOG.error("InvalidUrl: {}", url, ex);
      }
    }
  }

  private Optional<ArdVideoInfoDto> parseVideoUrls(final JsonObject playerPageObject) {
    final Optional<JsonObject> mediaCollectionObject = getMediaCollectionObject(playerPageObject);
    if (mediaCollectionObject.isPresent()) {
      final ArdVideoInfoDto videoDto =
        videoDeserializer.deserialize(mediaCollectionObject.get(), null, null);
      return Optional.of(videoDto);
    }

    return Optional.empty();
  }
}
