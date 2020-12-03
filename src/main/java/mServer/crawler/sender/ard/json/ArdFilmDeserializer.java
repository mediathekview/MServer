package mServer.crawler.sender.ard.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.mediathekview.mlib.Const;
import de.mediathekview.mlib.daten.DatenFilm;
import de.mediathekview.mlib.tool.Log;
import java.lang.reflect.Type;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import mServer.crawler.CrawlerTool;
import mServer.crawler.sender.ard.ArdConstants;
import mServer.crawler.sender.ard.ArdFilmDto;
import mServer.crawler.sender.ard.ArdFilmInfoDto;
import mServer.crawler.sender.base.JsonUtils;
import mServer.crawler.sender.base.Qualities;
import mServer.crawler.sender.base.UrlUtils;
import org.apache.logging.log4j.LogManager;

public class ArdFilmDeserializer implements JsonDeserializer<List<ArdFilmDto>> {

  private static final org.apache.logging.log4j.Logger LOG
      = LogManager.getLogger(ArdFilmDeserializer.class);

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
    ADDITIONAL_SENDER.put("wdr", Const.WDR);
    ADDITIONAL_SENDER.put("radio_bremen", "rbtv");
  }

  private final ArdVideoInfoJsonDeserializer videoDeserializer;

  public ArdFilmDeserializer() {
    videoDeserializer = new ArdVideoInfoJsonDeserializer();
  }

  private static Optional<JsonObject> getMediaCollectionObject(final JsonObject itemObject) {
    if (itemObject.has(ELEMENT_MEDIA_COLLECTION)
        && !itemObject.get(ELEMENT_MEDIA_COLLECTION).isJsonNull()
        && itemObject.getAsJsonObject(ELEMENT_MEDIA_COLLECTION).has(ELEMENT_EMBEDDED)
        && !itemObject.getAsJsonObject(ELEMENT_MEDIA_COLLECTION).get(ELEMENT_EMBEDDED)
        .isJsonNull()) {

      return Optional.of(itemObject.getAsJsonObject(ELEMENT_MEDIA_COLLECTION)
          .getAsJsonObject(ELEMENT_EMBEDDED));
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
    final Optional<String> description
        = JsonUtils.getAttributeAsString(itemObject, ATTRIBUTE_SYNOPSIS);
    final Optional<LocalDateTime> date = parseDate(itemObject);
    final Optional<Duration> duration = parseDuration(itemObject);
    final Optional<ArdVideoInfoDto> videoInfo = parseVideoUrls(itemObject);
    final Optional<String> partner = parsePartner(itemObject);

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
      if (widgets.size() > 1) {
        parseRelatedFilms(filmDto, widgets.get(1).getAsJsonObject());
      }
      films.add(filmDto);

      if (partner.isPresent() && ADDITIONAL_SENDER.containsKey(partner.get())) {
        // add film to other sender (like RBB)
        DatenFilm additionalFilm
            = createFilm(
            ADDITIONAL_SENDER.get(partner.get()),
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

  private Optional<String> parsePartner(JsonObject playerPageObject) {
    if (playerPageObject.has(ELEMENT_PUBLICATION_SERVICE)) {
      JsonObject publicationServiceObject
          = playerPageObject.get(ELEMENT_PUBLICATION_SERVICE).getAsJsonObject();
      Optional<String> partnerAttribute = JsonUtils
          .getAttributeAsString(publicationServiceObject, ATTRIBUTE_PARTNER);
      if (partnerAttribute.isPresent()) {
        return partnerAttribute;
      }

      Optional<String> nameAttribute = JsonUtils
          .getAttributeAsString(publicationServiceObject, ATTRIBUTE_NAME);
      if (nameAttribute.isPresent()) {
        return Optional.of(nameAttribute.get());
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
          final Optional<String> id
              = JsonUtils.getAttributeAsString(teasersItemObject, ATTRIBUTE_ID);
          if (id.isPresent()) {
            final String url = ArdConstants.ITEM_URL + id.get();

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

    DatenFilm film = new DatenFilm(sender, topic, "", title, videoInfo.getDefaultVideoUrl(), "",
        dateValue, timeValue, duration.orElse(Duration.ZERO).getSeconds(), description.orElse(""));
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
