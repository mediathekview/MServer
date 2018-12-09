package de.mediathekview.mserver.crawler.ard.json;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.Resolution;
import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mserver.base.utils.JsonUtils;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import mServer.crawler.CrawlerTool;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;

public class ArdFilmDeserializer implements JsonDeserializer<Optional<Film>> {
  
  private static final org.apache.logging.log4j.Logger LOG = LogManager.getLogger(ArdFilmDeserializer.class);
  
  private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssX");
  
  private static final String GERMAN_TIME_ZONE = "Europe/Berlin";
  
  private static final String ELEMENT_DATA = "data";
  private static final String ELEMENT_MEDIA_COLLECTION = "mediaCollection";
  private static final String ELEMENT_PLAYER_PAGE = "playerPage";
  private static final String ELEMENT_SHOW = "show";
  
  private static final String ATTRIBUTE_BROADCAST = "broadcastedOn";
  private static final String ATTRIBUTE_DURATION = "_duration";
  private static final String ATTRIBUTE_SYNOPSIS = "synopsis";
  private static final String ATTRIBUTE_TITLE = "title";
  
  private final ArdVideoInfoJsonDeserializer videoDeserializer;
  
  public ArdFilmDeserializer(final AbstractCrawler crawler) {
    videoDeserializer = new ArdVideoInfoJsonDeserializer(crawler);
  }
  
  @Override
  public Optional<Film> deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context) {
    
    if (!JsonUtils.checkTreePath(jsonElement, Optional.empty(), ELEMENT_DATA, ELEMENT_PLAYER_PAGE)) {
      return Optional.empty();
    }
    
    JsonObject playerPageObject = jsonElement.getAsJsonObject().get(ELEMENT_DATA).getAsJsonObject().get(ELEMENT_PLAYER_PAGE).getAsJsonObject();
    Optional<String> topic = parseTopic(playerPageObject);
    Optional<String> title = JsonUtils.getAttributeAsString(playerPageObject, ATTRIBUTE_TITLE);
    Optional<String> description = JsonUtils.getAttributeAsString(playerPageObject, ATTRIBUTE_SYNOPSIS);
    Optional<LocalDateTime> date = parseDate(playerPageObject);
    Optional<Duration> duration = parseDuration(playerPageObject);
    Optional<ArdVideoInfoDTO> videoInfo = parseVideoUrls(playerPageObject);
    
    if (topic.isPresent() && title.isPresent() && videoInfo.isPresent() && videoInfo.get().getVideoUrls().size() > 0) {
      return createFilm(topic.get(), title.get(), description, date, duration, videoInfo.get());
    }
    
    return Optional.empty();
  }
  
  private static Optional<JsonObject> getMediaCollectionObject(final JsonObject playerPageObject) {
    if (playerPageObject.has(ELEMENT_MEDIA_COLLECTION) && !playerPageObject.get(ELEMENT_MEDIA_COLLECTION).isJsonNull()) {
      return Optional.of(playerPageObject.get(ELEMENT_MEDIA_COLLECTION).getAsJsonObject());
    }
    
    return Optional.empty();
  }
  
  private Optional<Film> createFilm(String topic, String title, Optional<String> description, 
          Optional<LocalDateTime> date, Optional<Duration> duration, ArdVideoInfoDTO videoInfo) {
    
    final Film film = new Film(UUID.randomUUID(), Sender.ARD, title, topic, date.orElse(null), duration.orElse(Duration.ofSeconds(0)));
    
    description.ifPresent(film::setBeschreibung);
    
    film.setGeoLocations(CrawlerTool.getGeoLocations(Sender.ARD, videoInfo.getDefaultVideoUrl()));
    if (StringUtils.isNotBlank(videoInfo.getSubtitleUrl())) {
      try {
        film.addSubtitle(new URL(videoInfo.getSubtitleUrl()));
      } catch (MalformedURLException ex) {
        LOG.error("Invalid subtitle url: " + videoInfo.getSubtitleUrl(), ex);
      }
    }
    addUrls(film, videoInfo.getVideoUrls());
    
    return Optional.of(film);
  }
  
  private void addUrls(final Film aFilm, final Map<Resolution, String> aVideoUrls) {
    for (final Map.Entry<Resolution, String> qualitiesEntry : aVideoUrls.entrySet()) {
      try {
        aFilm.addUrl(qualitiesEntry.getKey(), CrawlerTool.stringToFilmUrl(qualitiesEntry.getValue()));
      } catch (MalformedURLException ex) {
        LOG.error("InvalidUrl: " + qualitiesEntry.getValue(), ex);
      }
    }
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
      } catch(DateTimeParseException ex) {
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
  
  private Optional<ArdVideoInfoDTO> parseVideoUrls(final JsonObject playerPageObject) {
    final Optional<JsonObject> mediaCollectionObject = getMediaCollectionObject(playerPageObject);
    if (mediaCollectionObject.isPresent()) {
      ArdVideoInfoDTO videoDto = videoDeserializer.deserialize(mediaCollectionObject.get(), null, null);
      return Optional.of(videoDto);
    }
    
    return Optional.empty();
  }
}
