package de.mediathekview.mserver.crawler.tagesschau.json;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.mediathekview.mserver.base.utils.JsonUtils;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.daten.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URI;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

public class TagesschauVideoDeserializer implements JsonDeserializer<List<Film>> {
  private static final String ELEMENT_MC = "mc";
  private static final String ELEMENT_MEDIA = "media";
  private static final String ELEMENT_META = "meta";
  private static final String ELEMENT_PLUG_IN_DATA = "pluginData";
  private static final String ELEMENT_SHARING_WEB = "sharing@web";
  private static final String ELEMENT_STREAMS = "streams";

  private static final String ATTRIBUTE_DATE = "broadcastedOnDateTime";
  private static final String ATTRIBUTE_DURATION = "durationSeconds";
  private static final String ATTRIBUTE_TOPIC = "seriesTitle";
  private static final String ATTRIBUTE_TITLE = "title";

  private static final String ATTRIBUTE_WIDTH = "maxHResolutionPx";
  private static final String ATTRIBUTE_MIMETYPE = "mimeType";
  private static final String ATTRIBUTE_URL = "url";
  private static final String ATTRIBUTE_LINK = "link";
  private static final String[] SUPPORTED_MIME_TYPES = new String[] { "video/mp4" };

  private static final DateTimeFormatter DATE_TIME_FORMATTER =
          DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ", Locale.GERMANY); // 2016-10-29T16:15:00+02:00
  private static final String GERMAN_TIME_ZONE = "Europe/Berlin";
  private static final Logger LOG = LogManager.getLogger(TagesschauVideoDeserializer.class);
  private final AbstractCrawler crawler;

  public TagesschauVideoDeserializer(AbstractCrawler crawler) {
    this.crawler = crawler;
  }

  @Override
  public List<Film> deserialize(
      JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) {

    final List<Film> results = new ArrayList<>();

    Optional<JsonElement> mcElement = JsonUtils.getElement(jsonElement, ELEMENT_MC);
    if (mcElement.isPresent()) {
      final Optional<JsonElement> metaElement = JsonUtils.getElement(mcElement.get(), ELEMENT_META);
      if (metaElement.isPresent()) {
        final JsonObject metaObject = metaElement.get().getAsJsonObject();
        final Optional<String> topic = JsonUtils.getAttributeAsString(metaObject, ATTRIBUTE_TOPIC);
        final Optional<String> title = JsonUtils.getAttributeAsString(metaObject, ATTRIBUTE_TITLE);
        final Optional<Integer> duration = JsonUtils.getAttributeAsInt(metaObject, ATTRIBUTE_DURATION);
        final Optional<LocalDateTime> date = parseDate(metaObject);
        final Map<Resolution, String> urls = parseUrls(mcElement.get().getAsJsonObject());
        final String website = parseWebsite(mcElement.get().getAsJsonObject());

        // TODO Prüfungen auf Topic+Titel
        // TODO Zeitzone passt nicht

        final Film film =
            new Film(
                UUID.randomUUID(),
                Sender.TAGESSCHAU24,
                title.orElse(""),
                topic.orElse(""),
                date.get(),
                duration.isEmpty() ? Duration.ofSeconds(0) : Duration.ofSeconds(duration.get()));
        film.addGeolocation(GeoLocations.GEO_NONE);
        if (!website.isEmpty()) {
          try {
            film.setWebsite(URI.create(website).toURL());
          } catch (MalformedURLException e) {
            LOG.error("Invalid website URL: {}", website, e);
          }
        }

        urls.forEach((resolution, url) -> {
        try {
          film.addUrl(resolution, new FilmUrl(url, crawler.determineFileSizeInKB(url)));
        } catch (final MalformedURLException ex) {
          LOG.error("InvalidUrl: {}", url, ex);
        }
        });

        results.add(film);
      }
    }

    return results;
  }

  private String parseWebsite(JsonObject mcObject) {
    return JsonUtils.getElementValueAsString(mcObject, ELEMENT_PLUG_IN_DATA, ELEMENT_SHARING_WEB, ATTRIBUTE_LINK).orElse("");
  }

  private Map<Resolution, String> parseUrls(final JsonObject mcObject) {
    // TODO robust machen gegen fehlende Elemente
    final Map<Resolution, String> urls = new EnumMap<>(Resolution.class);

    mcObject.get(ELEMENT_STREAMS).getAsJsonArray().forEach(stream -> {
      stream.getAsJsonObject().get(ELEMENT_MEDIA).getAsJsonArray().forEach(media -> {
        final Optional<String> mimeType = JsonUtils.getElementValueAsString(media, ATTRIBUTE_MIMETYPE);
        if (mimeType.isPresent() && Arrays.stream(SUPPORTED_MIME_TYPES).anyMatch(type -> type.equals(mimeType.get()))) {
          final Optional<Integer> width = JsonUtils.getAttributeAsInt(media.getAsJsonObject(), ATTRIBUTE_WIDTH);
          final Optional<String> url = JsonUtils.getElementValueAsString(media, ATTRIBUTE_URL);

          if (width.isPresent() && url.isPresent()) {
            final Resolution resolution = Resolution.getResolutionFromWidth(width.get());
            urls.put(resolution, url.get());
          }
        }
      });
    });

    return urls;
  }

  private static Optional<LocalDateTime> parseDate(final JsonObject metaObject) {
    final Optional<String> dateValue =
            JsonUtils.getAttributeAsString(metaObject, ATTRIBUTE_DATE);
    if (dateValue.isPresent()) {
      try {
        final OffsetDateTime inputDateTime = OffsetDateTime.parse(dateValue.get(), DATE_TIME_FORMATTER);
        final LocalDateTime localDateTime = inputDateTime.atZoneSameInstant(ZoneId.of(GERMAN_TIME_ZONE)).toLocalDateTime();
        return Optional.of(localDateTime);
      } catch (final DateTimeParseException ex) {
        LOG.error("Error parsing date time value {}", dateValue.get(), ex);
      }
    }

    return Optional.empty();
  }

}
