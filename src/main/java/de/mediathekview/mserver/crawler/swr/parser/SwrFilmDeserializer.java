package de.mediathekview.mserver.crawler.swr.parser;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.Resolution;
import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mserver.base.utils.JsonUtils;
import de.mediathekview.mserver.crawler.swr.SwrConstants;
import de.mediathekview.mserver.crawler.swr.SwrUrlOptimizer;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.EnumMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.UUID;
import mServer.crawler.CrawlerTool;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SwrFilmDeserializer implements JsonDeserializer<Optional<Film>> {

  private static final Logger LOG = LogManager.getLogger(SwrFilmDeserializer.class);

  private static final String ELEMENT_ATTR = "attr";
  private static final String ELEMENT_SUB = "sub";

  private static final String ATTRIBUTE_TOPIC = "group_title";
  private static final String ATTRIBUTE_TITLE = "entry_title";
  private static final String ATTRIBUTE_DATE = "entry_pdatet";
  private static final String ATTRIBUTE_DURATION = "entry_durat";
  private static final String ATTRIBUTE_SUBTITLE = "entry_capuri";
  private static final String ATTRIBUTE_ID = "entry_idkey";
  private static final String ATTRIBUTE_DESCRIPTION = "entry_descl";
  private static final String ATTRIBUTE_NAME = "name";
  private static final String ATTRIBUTE_VAL0 = "val0";
  private static final String ATTRIBUTE_VAL1 = "val1";
  private static final String ATTRIBUTE_VAL2 = "val2";

  private static final String VIDEO_ENTRY_NAME = "entry_media";
  private static final String VIDEO_ENTRY_RELEVANT_CODEC = "h264";
  private static final String VIDEO_ENTRY_QUALITY_SMALL = "2";
  private static final String VIDEO_ENTRY_QUALITY_NORMAL = "3";
  private static final String VIDEO_ENTRY_QUALITY_HD = "4";

  private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmm");
  private static final SwrUrlOptimizer optimizer = new SwrUrlOptimizer();

  @Override
  public Optional<Film> deserialize(JsonElement aJson, Type aTypeOfT, JsonDeserializationContext aContext) {

    final JsonObject jsonObject = aJson.getAsJsonObject();
    if (!jsonObject.has(ELEMENT_ATTR)) {
      LOG.error("missing element " + ELEMENT_ATTR);
      return Optional.empty();
    }
    if (!jsonObject.has(ELEMENT_SUB)) {
      LOG.error("missing element " + ELEMENT_SUB);
      return Optional.empty();
    }

    final JsonObject attrObject = jsonObject.get(ELEMENT_ATTR).getAsJsonObject();
    final JsonArray subArray = jsonObject.get(ELEMENT_SUB).getAsJsonArray();

    Map<Resolution, String> videoUrls = parseVideoUrls(subArray);

    Optional<String> topic = JsonUtils.getAttributeAsString(attrObject, ATTRIBUTE_TOPIC);
    Optional<String> title = JsonUtils.getAttributeAsString(attrObject, ATTRIBUTE_TITLE);
    if (topic.isPresent() && title.isPresent()) {
      try {
        return createFilm(attrObject, topic.get(), title.get(), videoUrls);
      } catch (MalformedURLException e) {
        LOG.error("SwrFilmDeserializer: error reading video infos: ", e);
      }
    }

    return Optional.empty();
  }

  private Map<Resolution, String> parseVideoUrls(final JsonArray aSubArray) {
    Map<Resolution, String> urls = new EnumMap<>(Resolution.class);

    for (JsonElement entry : aSubArray) {
      final JsonObject entryObject = entry.getAsJsonObject();
      Optional<String> name = JsonUtils.getAttributeAsString(entryObject, ATTRIBUTE_NAME);
      if (name.isPresent() && name.get().equalsIgnoreCase(VIDEO_ENTRY_NAME)
          && entryObject.has(ELEMENT_ATTR)
      ) {
        final JsonObject attrObject = entryObject.get(ELEMENT_ATTR).getAsJsonObject();
        parseVideoEntry(attrObject, urls);
      }
    }

    return urls;
  }

  private void parseVideoEntry(JsonObject aAttrObject, Map<Resolution, String> aUrls) {
    Optional<String> codec = JsonUtils.getAttributeAsString(aAttrObject, ATTRIBUTE_VAL0);
    Optional<String> quality = JsonUtils.getAttributeAsString(aAttrObject, ATTRIBUTE_VAL1);
    Optional<String> url = JsonUtils.getAttributeAsString(aAttrObject, ATTRIBUTE_VAL2);

    if (codec.isPresent() && quality.isPresent() && url.isPresent()
        && codec.get().equalsIgnoreCase(VIDEO_ENTRY_RELEVANT_CODEC)
    ) {
      // only add the first occurrence => it's the better quality
      switch (quality.get()) {
        case VIDEO_ENTRY_QUALITY_SMALL:
          aUrls.putIfAbsent(Resolution.SMALL, url.get());
          break;
        case VIDEO_ENTRY_QUALITY_NORMAL:
          aUrls.putIfAbsent(Resolution.NORMAL, url.get());
          break;
        case VIDEO_ENTRY_QUALITY_HD:
          aUrls.putIfAbsent(Resolution.HD, url.get());
          break;
        default:
      }
    }
  }

  private Optional<Film> createFilm(final JsonObject attrObject, final String aTopic, String aTitle, Map<Resolution, String> aVideoUrls)
      throws MalformedURLException {

    final LocalDateTime time = parseDate(attrObject);
    final Duration duration = parseDuration(attrObject);
    final Optional<String> description = JsonUtils.getAttributeAsString(attrObject, ATTRIBUTE_DESCRIPTION);
    final Optional<String> id = JsonUtils.getAttributeAsString(attrObject, ATTRIBUTE_ID);
    final Optional<String> subtitle = JsonUtils.getAttributeAsString(attrObject, ATTRIBUTE_SUBTITLE);

    final Film film = new Film(UUID.randomUUID(), Sender.SWR, aTitle, aTopic, time, duration);

    description.ifPresent(film::setBeschreibung);
    if (id.isPresent()) {
      film.setWebsite(buildWebsiteUrl(id.get()));
    }

    if (subtitle.isPresent()) {
      film.addSubtitle(new URL(subtitle.get()));
    }

    for (Entry<Resolution, String> kvp : aVideoUrls.entrySet()) {
      String url = kvp.getValue();
      if (kvp.getKey() == Resolution.HD) {
        url = optimizer.optimizeHdUrl(url);
      }
      film.addUrl(kvp.getKey(), CrawlerTool.stringToFilmUrl(url));
    }

    return Optional.of(film);
  }

  private Optional<URL> buildWebsiteUrl(final String aId) throws MalformedURLException {
    return Optional.of(new URL(SwrConstants.URL_FILM_PAGE + aId));
  }

  private LocalDateTime parseDate(JsonObject attrObject) {
    Optional<String> timeValue = JsonUtils.getAttributeAsString(attrObject, ATTRIBUTE_DATE);
    if (timeValue.isPresent()) {
      return LocalDateTime.parse(timeValue.get(), DATE_TIME_FORMATTER);
    }

    return null;
  }

  private Duration parseDuration(JsonObject attrObject) {
    long duration = 0;

    Optional<String> durationValue = JsonUtils.getAttributeAsString(attrObject, ATTRIBUTE_DURATION);
    if (durationValue.isPresent()) {
      String[] durationParts = durationValue.get().split(":");
      for (String durationPart : durationParts) {
        duration = duration * 60 + Long.parseLong(durationPart);
      }
    }

    return Duration.ofSeconds(duration);
  }
}
