package de.mediathekview.mserver.crawler.orfon.json;

import com.google.gson.*;

import de.mediathekview.mlib.daten.FilmUrl;
import de.mediathekview.mlib.daten.GeoLocations;
import de.mediathekview.mlib.daten.Resolution;
import de.mediathekview.mserver.base.utils.JsonUtils;
import de.mediathekview.mserver.crawler.orfon.OrfOnVideoInfoDTO;

import java.lang.reflect.Type;
import java.net.URL;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class OrfOnEpisodeDeserializer implements JsonDeserializer<OrfOnVideoInfoDTO> {
  private static final Logger LOG = LogManager.getLogger(OrfOnEpisodeDeserializer.class);
  private static final String[] TAG_CHANNEL = {"_embedded", "channel", "name"};
  private static final String TAG_ID = "id";
  private static final String TAG_TITLE = "title";
  private static final String TAG_TITLE_WITH_DATE = "share_subject";
  private static final String TAG_TOPIC = "profile_title";
  private static final String TAG_AIRED = "date";
  private static final String TAG_DURATION = "duration_seconds";
  private static final String TAG_DESCRIPTION = "description";
  private static final String TAG_SHARE_BODY = "share_body";
  private static final String TAG_RIGHT = "right";
  private static final String TAG_VIDEO_TYPE ="video_type";
  private static final String[] TAG_SEGMENTS = {"_links", "segments", "href"};
  private static final String[] TAG_SUBTITLE = {"_links", "subtitle", "href"};
  private static final String TAG_VIDEO = "sources";
  private static final String TAG_VIDEO_QUALITY = "quality_key";
  private static final String TAG_VIDEO_URL = "src";

  
  @Override
  public OrfOnVideoInfoDTO deserialize(
      final JsonElement jsonElement, final Type typeOfT, final JsonDeserializationContext context)
      throws JsonParseException {
    
    OrfOnVideoInfoDTO aFilm = new OrfOnVideoInfoDTO(
        JsonUtils.getElementValueAsString(jsonElement, TAG_ID),
        JsonUtils.getElementValueAsString(jsonElement, TAG_CHANNEL),
        JsonUtils.getElementValueAsString(jsonElement, TAG_TITLE),
        JsonUtils.getElementValueAsString(jsonElement, TAG_TITLE_WITH_DATE),
        JsonUtils.getElementValueAsString(jsonElement, TAG_TOPIC),
        parseAiredDate(JsonUtils.getElementValueAsString(jsonElement, TAG_AIRED)),
        parseDuration(JsonUtils.getElementValueAsString(jsonElement, TAG_DURATION)),
        JsonUtils.getElementValueAsString(jsonElement, TAG_DESCRIPTION),
        parseWebsite(JsonUtils.getElementValueAsString(jsonElement, TAG_SHARE_BODY)),
        parseGeoLocations(JsonUtils.getElementValueAsString(jsonElement, TAG_RIGHT)),
        parseSubtitle(JsonUtils.getElementValueAsString(jsonElement, TAG_SUBTITLE)),
        parseUrl(jsonElement)
        );
    //LOG.debug("{}",jsonElement );
    
    if (aFilm.getGeorestriction().isEmpty() ||
        aFilm.getVideoUrls().isEmpty()){
    LOG.debug("{} (id)", aFilm.getId().get());
    LOG.debug("{} (genre_title)", JsonUtils.getElementValueAsString(jsonElement, "genre_title").get());
    LOG.debug("{} (headline)", JsonUtils.getElementValueAsString(jsonElement, "headline").get());
    LOG.debug("{} (profile_title*)", JsonUtils.getElementValueAsString(jsonElement, "profile_title").get());
    LOG.debug("{} (title*)", JsonUtils.getElementValueAsString(jsonElement, "title").get());
    LOG.debug("{} (sub_headline)", JsonUtils.getElementValueAsString(jsonElement, "sub_headline").get());
    LOG.debug("{} (share_subject)", JsonUtils.getElementValueAsString(jsonElement, "share_subject").get());
    LOG.debug("{} (TAG_RIGHT)", parseGeoLocations(JsonUtils.getElementValueAsString(jsonElement, TAG_RIGHT)));
    LOG.debug("{} (url)", parseUrl(jsonElement));
    LOG.debug("{} (segments)", JsonUtils.getElementValueAsString(jsonElement, TAG_SEGMENTS));
    LOG.debug("{}",jsonElement );
    LOG.debug("############");
    }
    // "genre_title": "Wetter",
    // "headline": "Wetter Tirol vom 05.01.2024",
    // "profile_title": "Wetter Tirol",
    // "title": "Wetter Tirol vom 05.01.2024",
    // "share_subject": "Wetter Tirol vom 05.01.2024 vom 05.01.2024 um 19:20 Uhr",
    // "sub_headline": "Wetter Tirol",
    
    // 
    return aFilm;
  }
  
  private Optional<URL> parseSubtitle(Optional<String> text) {
    Optional<URL> sub = Optional.empty();
    if (text.isPresent()) {
      try {
        sub =  Optional.of(new URL(text.get()));
      } catch (Exception e) {
        LOG.error("parseSubtitle failed for string {} exception {}", text.get(), e);
      }
    }
    return sub;
    
  }
  
  private Optional<Map<Resolution, FilmUrl>> parseUrl(JsonElement jsonElement) {
    
    for (Map.Entry<String, JsonElement> entry : jsonElement.getAsJsonObject().getAsJsonObject(TAG_VIDEO).entrySet()) {

      if (!"hlshdssmoothdashprogressive_download".contains(entry.getKey())) {
        LOG.debug(" {} ", jsonElement);
      }
    }
    
    
    Optional<Map<Resolution, FilmUrl>> urls = Optional.empty();
    Optional<String> codec = Optional.empty(); //
    if (jsonElement.getAsJsonObject().has(TAG_VIDEO) &&
        jsonElement.getAsJsonObject().getAsJsonObject(TAG_VIDEO).has("progressive_download")) {
      codec = Optional.of("progressive_download");
    } else if (jsonElement.getAsJsonObject().has(TAG_VIDEO) &&
        jsonElement.getAsJsonObject().getAsJsonObject(TAG_VIDEO).has("hls")) {
      codec = Optional.of("hls");
    } else if (jsonElement.getAsJsonObject().has(TAG_VIDEO) &&
        jsonElement.getAsJsonObject().getAsJsonObject(TAG_VIDEO).has("hds")) {
      codec = Optional.of("hds");
    } else if (jsonElement.getAsJsonObject().has(TAG_VIDEO) &&
        jsonElement.getAsJsonObject().getAsJsonObject(TAG_VIDEO).has("smooth")) {
      codec = Optional.of("smooth");
    } else if (jsonElement.getAsJsonObject().has(TAG_VIDEO) &&
        jsonElement.getAsJsonObject().getAsJsonObject(TAG_VIDEO).has("dash")) {
      codec = Optional.of("dash");
    }
    if (codec.isPresent()) {
      urls = Optional.of(new HashMap<Resolution, FilmUrl>());
      for (JsonElement codecUrls : jsonElement.getAsJsonObject().getAsJsonObject(TAG_VIDEO).getAsJsonArray(codec.get())) {
        try {
          String qualityString = codecUrls.getAsJsonObject().get(TAG_VIDEO_QUALITY).getAsString();
          String url = codecUrls.getAsJsonObject().get(TAG_VIDEO_URL).getAsString();
          urls.get().put(
            OrfOnEpisodeDeserializer.getQuality(qualityString).get(),
            new FilmUrl(url, 0L)
          );
        } catch (Exception e) {
          LOG.error(
              "parseUrl failed for quality {} and url {} exception {}", 
              codecUrls.getAsJsonObject().get("quality_key").getAsString(), 
              codecUrls.getAsJsonObject().get("src").getAsString(), 
              e
          );
        }
      }
      if (urls.get().size() == 0) {
        return Optional.empty();
      }
    }
    return urls;
  }
  
  private Optional<URL> parseWebsite(Optional<String> text) {
    Optional<URL> result = Optional.empty();
    if (text.isPresent()) {
      try {
        result = Optional.of(new URL(text.get()));
      } catch (Exception e) {
        LOG.error("parseWebsite failed for string {} exception {}", text.get(), e);
      }
    }
    return result;
  }
  
  private Optional<Collection<GeoLocations>> parseGeoLocations(Optional<String> text) {
    if (text.isPresent()) {
      if (text.get().equalsIgnoreCase("worldwide")) {
        ArrayList<GeoLocations> a = new ArrayList<GeoLocations>();
        a.add(GeoLocations.GEO_NONE);
        return Optional.of(a);
      } else if (text.get().equalsIgnoreCase("austria")) {
        ArrayList<GeoLocations> a = new ArrayList<GeoLocations>();
        a.add(GeoLocations.GEO_AT);
        return Optional.of(a);
      } else {
        LOG.error("parseGeoLocations failed for unknown string {}", text.get());
      }
    }
    return Optional.empty();
  }
  private Optional<LocalDateTime> parseAiredDate(Optional<String> text) {
    Optional<LocalDateTime> result = Optional.empty();
    if (text.isPresent()) {
      try {
        result = Optional.of(LocalDateTime.parse(text.get(), DateTimeFormatter.ISO_ZONED_DATE_TIME));
      } catch (Exception e) {
        LOG.error("DateTimeFormatter failed for string {} exception {}", text.get(), e);
      }
    }
    return result;
  }
  
  private Optional<Duration> parseDuration(Optional<String> text) {
    if (text.isPresent()) {
      try {
        return Optional.of(Duration.ofSeconds(Integer.parseInt(text.get())));
      } catch (Exception e) {
        LOG.error("Duration failed for string {} exception {}", text.get(), e);
      }
    }
    return Optional.empty(); 
    
  }
  
  ///////////////
  
  private static Optional<Resolution> getQuality(final String aQuality) {
    switch (aQuality) {
      case "Q0A":
        return Optional.of(Resolution.VERY_SMALL);
      case "Q1A":
        return Optional.of(Resolution.VERY_SMALL);
      case "Q4A":
        return Optional.of(Resolution.SMALL);
      case "Q6A":
        return Optional.of(Resolution.NORMAL);
      case "Q8C":
        return Optional.of(Resolution.HD);
      case "QXA":
      case "QXADRM":
      case "QXB":
      case "QXBDRM":
      case "Q8A":
        return Optional.of(Resolution.NORMAL);
      default:
        LOG.debug("ORF: unknown quality: {}", aQuality);
    }
    return Optional.empty();
  }
  
}
