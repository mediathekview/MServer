package de.mediathekview.mserver.crawler.orfon.json;

import com.google.gson.*;

import de.mediathekview.mlib.daten.FilmUrl;
import de.mediathekview.mlib.daten.GeoLocations;
import de.mediathekview.mlib.daten.Resolution;
import de.mediathekview.mserver.base.utils.JsonUtils;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.orfon.OrfOnConstants;
import de.mediathekview.mserver.crawler.orfon.OrfOnVideoInfoDTO;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class OrfOnEpisodeDeserializer implements JsonDeserializer<OrfOnVideoInfoDTO> {
  private static final Logger LOG = LogManager.getLogger(OrfOnEpisodeDeserializer.class);
  private static final String[] TAG_CHANNEL = {"_embedded", "channel", "name"};
  private static final String TAG_ID = "id";
  private static final String TAG_TITLE = "title";
  private static final String TAG_TITLE_WITH_DATE = "share_subject";
  private static final String TAG_TOPIC = "profile_title";
  private static final String TAG_TOPIC_ARCHIVE = "sub_headline";
  private static final String TAG_AIRED = "date";
  private static final String TAG_DURATION = "duration_seconds";
  private static final String TAG_DESCRIPTION = "description";
  private static final String TAG_SHARE_BODY = "share_body";
  private static final String TAG_RIGHT = "right";
  private static final String TAG_VIDEO_TYPE ="video_type";
  private static final String[] TAG_SUBTITLE = {"_links", "subtitle", "href"};
  private static final String[] TAG_VIDEO_PATH_1 = {"_embedded","segments"};
  private static final String[] TAG_VIDEO_PATH_2 = {"_embedded", "playlist", "sources"};
  private static final String TAG_VIDEO_URL = "src";
  private static final String TAG_VIDEO_CODEC = "delivery";
  private static final String TAG_VIDEO_QUALITY = "quality";
  private static final String[] TAG_SUBTITLE_SECTION = {"_embedded", "subtitle"};
  private static final String TAG_SUBTITLE_SMI = "sami_url";
  private static final String TAG_SUBTITLE_SRT = "srt_url";
  private static final String TAG_SUBTITLE_TTML = "ttml_url";
  private static final String TAG_SUBTITLE_VTT = "vtt_url";
  private static final String TAG_SUBTITLE_XML = "xml_url";
  //
  private AbstractCrawler crawler = null;
  private static final String[] PREFERED_CODEC = {"hls", "hds", "streaming", "progressive"};
  //
  
  public OrfOnEpisodeDeserializer(AbstractCrawler crawler) {
    this.crawler = crawler;
  }
  
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
        JsonUtils.getElementValueAsString(jsonElement, TAG_TOPIC_ARCHIVE),
        parseAiredDate(JsonUtils.getElementValueAsString(jsonElement, TAG_AIRED)),
        parseDuration(JsonUtils.getElementValueAsString(jsonElement, TAG_DURATION)),
        JsonUtils.getElementValueAsString(jsonElement, TAG_DESCRIPTION),
        parseWebsite(JsonUtils.getElementValueAsString(jsonElement, TAG_SHARE_BODY)),
        parseGeoLocations(JsonUtils.getElementValueAsString(jsonElement, TAG_RIGHT)),
        parseSubtitleSource(JsonUtils.getElementValueAsString(jsonElement, TAG_SUBTITLE)),
        parseUrl(jsonElement),
        buildOrResolveSubs(jsonElement)
        
        );
    return aFilm;
  }
  
  private Optional<Set<URL>> buildOrResolveSubs(JsonElement jsonElement) {
    Optional<String> subtitleSource = JsonUtils.getElementValueAsString(jsonElement, TAG_SUBTITLE);
    Optional<JsonElement> embeddedSubtitleSection = JsonUtils.getElement(jsonElement, TAG_SUBTITLE_SECTION);
    Optional<Set<URL>> setOfSubs = Optional.empty();
    if (embeddedSubtitleSection.isPresent()) {
      setOfSubs = parseSubtitleUrls(embeddedSubtitleSection.get());  
    } else if (subtitleSource.isPresent()) {
      Map<String, String> myMap = Map.ofEntries(
          Map.entry("Authorization", OrfOnConstants.AUTH),
          Map.entry("Accept-Charset", "UTF_8"),
          Map.entry("User-Agent", "Mozilla"),
          Map.entry("Accept-Encoding", "*"));
      JsonElement newRequestForSubs = null;
      try {
        newRequestForSubs = crawler.getConnection().requestBodyAsJsonElement(subtitleSource.get(), myMap);
        if (newRequestForSubs != null) {
          setOfSubs = parseSubtitleUrls(newRequestForSubs);
        }
      } catch (IOException e) {
        LOG.error("Failed to resolve subtitle from {} error {}", subtitleSource, e);
      }
      
    }
    return setOfSubs;
  }

  private Optional<URL> parseSubtitleSource(Optional<String> text) {
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
  
  private Optional<Set<URL>> parseSubtitleUrls(JsonElement element) {
    Set<URL> urls = new HashSet<>();
    JsonUtils.getElementValueAsString(element, TAG_SUBTITLE_SMI).ifPresent( stringUrl -> toURL(stringUrl).ifPresent(urls::add));
    JsonUtils.getElementValueAsString(element, TAG_SUBTITLE_SRT).ifPresent( stringUrl -> toURL(stringUrl).ifPresent(urls::add));
    JsonUtils.getElementValueAsString(element, TAG_SUBTITLE_TTML).ifPresent( stringUrl -> toURL(stringUrl).ifPresent(urls::add));
    JsonUtils.getElementValueAsString(element, TAG_SUBTITLE_VTT).ifPresent( stringUrl -> toURL(stringUrl).ifPresent(urls::add));
    JsonUtils.getElementValueAsString(element, TAG_SUBTITLE_XML).ifPresent( stringUrl -> toURL(stringUrl).ifPresent(urls::add));
    if (urls.isEmpty()) {
      return Optional.empty();
    }
    return Optional.of(urls);
  }
  
  private Optional<URL> toURL(String aString) {
    try {
      return Optional.of(new URL(aString));
    } catch (MalformedURLException e) {
      LOG.debug("error converting {} to URL {}", aString, e);
    }
    return Optional.empty();
  }
  
  private Optional<Map<Resolution, FilmUrl>> parseUrl(JsonElement jsonElement) {
    Optional<JsonElement> videoPath1 = JsonUtils.getElement(jsonElement, TAG_VIDEO_PATH_1);
    if (videoPath1.isEmpty() || !videoPath1.get().isJsonArray()) {
      return Optional.empty();
    }
    Optional<JsonElement> videoPath2 = JsonUtils.getElement(videoPath1.get().getAsJsonArray().get(0), TAG_VIDEO_PATH_2);
    if (videoPath2.isEmpty() || !videoPath2.get().isJsonArray()) {
      return Optional.empty();
    }
    for (String key : PREFERED_CODEC) {
      Optional<Map<Resolution,FilmUrl>> resultingVideos = readVideoForTargetCodec(videoPath2.get(),key);
      if (resultingVideos.isPresent()) {
        return resultingVideos;
      }
    }

    return Optional.empty();
  }
  
  private Optional<Map<Resolution, FilmUrl>> readVideoForTargetCodec(JsonElement urlArray, String targetCodec) {
    Map<Resolution, FilmUrl> urls = new EnumMap<>(Resolution.class);
    for (JsonElement videoElement : urlArray.getAsJsonArray()) {
      Optional<String> codec = JsonUtils.getElementValueAsString(videoElement, TAG_VIDEO_CODEC);
      Optional<String> quality = JsonUtils.getElementValueAsString(videoElement, TAG_VIDEO_QUALITY);
      Optional<String> url = JsonUtils.getElementValueAsString(videoElement, TAG_VIDEO_URL);
      if (url.isPresent() && codec.isPresent() && quality.isPresent() && targetCodec.equalsIgnoreCase(codec.get())) {
        try {
          long fileSize = crawler.determineFileSizeInKB(url.get());
          urls.put(
            OrfOnEpisodeDeserializer.getQuality(quality.get()).get(),
            new FilmUrl(url.get(), fileSize)
            );
        } catch (MalformedURLException e) {
          LOG.error("Malformed video url {} {}", url.get(), e);
        }
      }
    }
    if (urls.isEmpty()) {
      Optional.empty();
    }
    return Optional.of(urls);
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
        ArrayList<GeoLocations> a = new ArrayList<>();
        a.add(GeoLocations.GEO_NONE);
        return Optional.of(a);
      } else if (text.get().equalsIgnoreCase("austria")) {
        ArrayList<GeoLocations> a = new ArrayList<>();
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
