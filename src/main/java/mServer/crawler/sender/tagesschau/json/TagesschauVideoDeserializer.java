package mServer.crawler.sender.tagesschau.json;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.lang.reflect.Type;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.mediathekview.mlib.Const;
import de.mediathekview.mlib.daten.DatenFilm;
import mServer.crawler.CrawlerTool;
import mServer.crawler.sender.base.JsonUtils;
import mServer.crawler.sender.base.Qualities;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TagesschauVideoDeserializer implements JsonDeserializer<List<DatenFilm>> {
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

  private static final DateTimeFormatter DATE_FORMAT
          = DateTimeFormatter.ofPattern("dd.MM.yyyy");
  private static final DateTimeFormatter TIME_FORMAT
          = DateTimeFormatter.ofPattern("HH:mm:ss");

  private static final Pattern LONG_MONTH_PATTERN =
      Pattern.compile("(\\d{1,2}(?:\\.|\\s)\\s*[A-Za-zÄÖÜäöüß]+\\s+\\d{4})");
  private static final DateTimeFormatter GERMAN_LONG = new DateTimeFormatterBuilder()
          .parseCaseInsensitive()
          .appendPattern("d. MMMM uuuu")
          .toFormatter(Locale.GERMAN);
  private static final DateTimeFormatter GERMAN_LONG_NO_SPACE = new DateTimeFormatterBuilder()
          .parseCaseInsensitive()
          .appendPattern("d.MMMM uuuu")
          .toFormatter(Locale.GERMAN);
  private static final DateTimeFormatter GERMAN_LONG_NO_DOT = new DateTimeFormatterBuilder()
          .parseCaseInsensitive()
          .appendPattern("d MMMM uuuu")
          .toFormatter(Locale.GERMAN);
  private static final DateTimeFormatter DATE_TIME_FORMATTER =
          DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ", Locale.GERMANY);
  private static final String GERMAN_TIME_ZONE = "Europe/Berlin";
  private static final Logger LOG = LogManager.getLogger(TagesschauVideoDeserializer.class);

  private static Optional<LocalDateTime> parseDate(final JsonObject metaObject, final Optional<LocalDate> titleDate) {
    final Optional<String> dateValue =
            JsonUtils.getAttributeAsString(metaObject, ATTRIBUTE_DATE);
    if (dateValue.isPresent()) {
      try {
        final OffsetDateTime inputDateTime = OffsetDateTime.parse(dateValue.get(), DATE_TIME_FORMATTER);
        LocalDateTime localDateTime = inputDateTime.atZoneSameInstant(ZoneId.of(GERMAN_TIME_ZONE)).toLocalDateTime();
        if (titleDate.isPresent() && titleDate.get().getYear() != localDateTime.getYear()) {
          localDateTime = localDateTime.withYear(titleDate.get().getYear());
        }
        return Optional.of(localDateTime);
      } catch (final DateTimeParseException ex) {
        LOG.warn("Error parsing date time value {}", dateValue.get(), ex);
      }
    }

    return titleDate.map(localDate -> LocalDateTime.of(localDate, LocalTime.of(20, 0)));
  }

  @Override
  public List<DatenFilm> deserialize(
      JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) {

    final List<DatenFilm> results = new ArrayList<>();

    Optional<JsonElement> mcElement = JsonUtils.getElement(jsonElement, ELEMENT_MC);
    if (mcElement.isPresent()) {
      final Optional<JsonElement> metaElement = JsonUtils.getElement(mcElement.get(), ELEMENT_META);
      if (metaElement.isPresent()) {
        final JsonObject metaObject = metaElement.get().getAsJsonObject();
        final Optional<String> topic = JsonUtils.getAttributeAsString(metaObject, ATTRIBUTE_TOPIC);
        final Optional<String> title = JsonUtils.getAttributeAsString(metaObject, ATTRIBUTE_TITLE);
        final Optional<Integer> duration = JsonUtils.getAttributeAsInt(metaObject, ATTRIBUTE_DURATION);
        final LocalDateTime date = parseDate(metaObject, parseDateFromTitle(title.orElse(""))).orElse(LocalDateTime.now());
        final Map<Qualities, String> urls = parseUrls(mcElement.get().getAsJsonObject());
        final String website = parseWebsite(mcElement.get().getAsJsonObject());

        String dateValue = date.format(DATE_FORMAT);
        String timeValue = date.format(TIME_FORMAT);

        DatenFilm film = new DatenFilm(Const.TAGESSCHAU24, topic.orElse(""), website, title.orElse(""), urls.get(Qualities.NORMAL), "",
                dateValue, timeValue, duration.orElse(0), "");

        if (urls.containsKey(Qualities.SMALL)) {
          CrawlerTool.addUrlKlein(film, urls.get(Qualities.SMALL));
        }
        if (urls.containsKey(Qualities.HD)) {
          CrawlerTool.addUrlHd(film, urls.get(Qualities.HD));
        }

        results.add(film);
      }
    }

    return results;
  }

  private Optional<LocalDate> parseDateFromTitle(String title) {
    Matcher m = LONG_MONTH_PATTERN.matcher(title);
    if (m.find()) {
      String datePart = m.group(1).replaceAll("\\s+", " ").trim();
      try {
        return Optional.of(LocalDate.parse(datePart, GERMAN_LONG));
      } catch (DateTimeParseException ignored) {
        // try other conversion
      }
      try {
        return Optional.of(LocalDate.parse(datePart, GERMAN_LONG_NO_SPACE));
      } catch (DateTimeParseException ignored) {
        // try other conversion
      }
      try {
        return Optional.of(LocalDate.parse(datePart, GERMAN_LONG_NO_DOT));
      } catch (DateTimeParseException ex) {
        LOG.debug("no valid date converted", ex);
      }
    }
    return Optional.empty();
  }

  private String parseWebsite(JsonObject mcObject) {
    return JsonUtils.getElementValueAsString(mcObject, ELEMENT_PLUG_IN_DATA, ELEMENT_SHARING_WEB, ATTRIBUTE_LINK).orElse("");
  }

  private Map<Qualities, String> parseUrls(final JsonObject mcObject) {
    final Map<Qualities, String> urls = new EnumMap<>(Qualities.class);
    if (mcObject.has(ELEMENT_STREAMS) && mcObject.get(ELEMENT_STREAMS).isJsonArray()) {
      mcObject
          .get(ELEMENT_STREAMS)
          .getAsJsonArray()
          .forEach(
              stream -> {
                final JsonObject streamObject = stream.getAsJsonObject();
                if (streamObject.has(ELEMENT_MEDIA)
                    && streamObject.get(ELEMENT_MEDIA).isJsonArray()) {
                  streamObject
                      .get(ELEMENT_MEDIA)
                      .getAsJsonArray()
                      .forEach(
                          media -> {
                            final Optional<String> mimeType =
                                JsonUtils.getElementValueAsString(media, ATTRIBUTE_MIMETYPE);
                            if (mimeType.isPresent()
                                && Arrays.stream(SUPPORTED_MIME_TYPES)
                                    .anyMatch(type -> type.equals(mimeType.get()))) {
                              final Optional<Integer> width =
                                  JsonUtils.getAttributeAsInt(
                                      media.getAsJsonObject(), ATTRIBUTE_WIDTH);
                              final Optional<String> url =
                                  JsonUtils.getElementValueAsString(media, ATTRIBUTE_URL);

                              if (width.isPresent() && url.isPresent()) {
                                final Qualities resolution =
                                    Qualities.getResolutionFromWidth(width.get());
                                urls.put(resolution, url.get());
                              }
                            }
                          });
                }
              });
    }
    return urls;
  }
}
