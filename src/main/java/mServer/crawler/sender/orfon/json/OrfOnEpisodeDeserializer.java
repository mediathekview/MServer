package mServer.crawler.sender.orfon.json;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import de.mediathekview.mlib.tool.Log;
import mServer.crawler.sender.base.JsonUtils;
import mServer.crawler.sender.base.Qualities;
import mServer.crawler.sender.base.UrlUtils;
import mServer.crawler.sender.orfon.OrfHttpClient;
import mServer.crawler.sender.orfon.OrfOnConstants;
import mServer.crawler.sender.orfon.OrfOnVideoInfoDTO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.lang.reflect.Type;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;


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
  private static final String[] TAG_SUBTITLE = {"_links", "subtitle", "href"};
  private static final String[] TAG_VIDEO_PATH_1 = {"_embedded", "segments"};
  private static final String[] TAG_VIDEO_PATH_2 = {"_embedded", "playlist", "sources"};
  private static final String TAG_VIDEO_URL = "src";
  private static final String TAG_VIDEO_CODEC = "delivery";
  private static final String TAG_VIDEO_QUALITY = "quality";
  private static final String TAG_VIDEO_FALLBACK = "sources";
  private static final String TAG_VIDEO_FALLBACK_URL = "src";
  private static final String TAG_DRM_PROTECTED = "is_drm_protected";

  private static final String[] TAG_SUBTITLE_SECTION = {"_embedded", "subtitle"};
  private static final String TAG_SUBTITLE_TTML = "ttml_url";
  private static final String[] PREFERED_CODEC = {"hls", "hds", "progressive"};
  private static final String[] VIDEO_THUMBNAIL = {"thumbnail_sources","hls"};
  //
  private final OrfHttpClient connection;
  //

  public OrfOnEpisodeDeserializer() {
    connection = new OrfHttpClient();
  }

  private static Optional<Qualities> getQuality(final String aQuality) {
    switch (aQuality) {
      case "Q0A":
      case "Q1A":
      case "Q4A":
        return Optional.of(Qualities.SMALL);
      case "Q6A":
        return Optional.of(Qualities.NORMAL);
      case "Q8C":
        return Optional.of(Qualities.HD);
      case "QXA":
      case "QXADRM":
      case "QXB":
      case "QXBDRM":
      case "Q8A":
        return Optional.empty();
      default:
        Log.sysLog("ORF: unknown quality: " + aQuality);
        LOG.debug("ORF: unknown quality: {}", aQuality);
    }
    return Optional.empty();
  }

  @Override
  public OrfOnVideoInfoDTO deserialize(
          final JsonElement jsonElement, final Type typeOfT, final JsonDeserializationContext context)
          throws JsonParseException {
    return new OrfOnVideoInfoDTO(
            JsonUtils.getElementValueAsString(jsonElement, TAG_ID),
            JsonUtils.getElementValueAsString(jsonElement, TAG_CHANNEL),
            JsonUtils.getElementValueAsString(jsonElement, TAG_TITLE),
            JsonUtils.getElementValueAsString(jsonElement, TAG_TITLE_WITH_DATE),
            JsonUtils.getElementValueAsString(jsonElement, TAG_TOPIC),
            JsonUtils.getElementValueAsString(jsonElement, TAG_TOPIC_ARCHIVE),
            JsonUtils.getElementValueAsString(jsonElement, TAG_DRM_PROTECTED),
            parseAiredDate(JsonUtils.getElementValueAsString(jsonElement, TAG_AIRED)),
            parseDuration(JsonUtils.getElementValueAsString(jsonElement, TAG_DURATION)),
            JsonUtils.getElementValueAsString(jsonElement, TAG_DESCRIPTION),
            parseWebsite(JsonUtils.getElementValueAsString(jsonElement, TAG_SHARE_BODY)),
            optimizeUrls(parseVideoFromSegmentPlaylist(jsonElement)),
            buildOrResolveSubs(jsonElement)

    );
  }

  private Optional<Map<Qualities, String>> optimizeUrls(Optional<Map<Qualities, String>> urls) {
    if (urls.isPresent() && urls.get().size() == 1) {
      final Map<Qualities, String> urlMap = urls.get();
      String urlToOptimize = urlMap.get(Qualities.NORMAL);
      for (String s : List.of("QXA","QXB")) {
        urlToOptimize = urlToOptimize.replace(s, "#Q#");  
      }
      urlMap.put(Qualities.SMALL, urlToOptimize.replace("#Q#", "Q4A"));
      urlMap.put(Qualities.NORMAL, urlToOptimize.replace("#Q#", "Q6A"));
      urlMap.put(Qualities.HD, urlToOptimize.replace("#Q#", "Q8C"));
    }
    return urls;
  }
  

  private Optional<String> buildOrResolveSubs(JsonElement jsonElement) {
    Optional<String> subtitleSource = JsonUtils.getElementValueAsString(jsonElement, TAG_SUBTITLE);
    Optional<JsonElement> embeddedSubtitleSection = JsonUtils.getElement(jsonElement, TAG_SUBTITLE_SECTION);
    Optional<String> setOfSubs = Optional.empty();
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
        newRequestForSubs = connection.requestBodyAsJsonElement(subtitleSource.get(), myMap);
        if (newRequestForSubs != null) {
          setOfSubs = parseSubtitleUrls(newRequestForSubs);
        }
      } catch (IOException e) {
        Log.errorLog(873673822, e, "Failed to resolve subtitle: " + subtitleSource);
        LOG.error("Failed to resolve subtitle from {} error {}", subtitleSource, e);
      }

    }
    return setOfSubs;
  }

  private Optional<String> parseSubtitleUrls(JsonElement element) {
    return JsonUtils.getElementValueAsString(element, TAG_SUBTITLE_TTML);
  }

  private Optional<Map<Qualities, String>> parseVideoFromSegmentPlaylist(JsonElement jsonElement) {
    Optional<JsonElement> videoPath1 = JsonUtils.getElement(jsonElement, TAG_VIDEO_PATH_1);
    if (videoPath1.isEmpty() || !videoPath1.get().isJsonArray() || videoPath1.get().getAsJsonArray().isEmpty()) {
      return Optional.empty();
    }

    Optional<Map<String, String>> resultingVideos = Optional.empty();
    // We need to fallback to episode.sources in case there are many elements in the playlist
    if (videoPath1.get().getAsJsonArray().size() == 1) {
      Optional<JsonElement> videoPath2 = JsonUtils.getElement(videoPath1.get().getAsJsonArray().get(0), TAG_VIDEO_PATH_2);
      if (videoPath2.isEmpty() || !videoPath2.get().isJsonArray()) {
        return Optional.empty();
      }
      for (String key : PREFERED_CODEC) {
        resultingVideos = readVideoForTargetCodec(videoPath2.get(), key);
        if (resultingVideos.isPresent()) {
          break;
        }
      }
    }
    if (resultingVideos.isEmpty()) {
      resultingVideos = parseVideoFromSources(jsonElement);
    }

    // map orf codec to quality
    if (resultingVideos.isPresent()) {
      Map<Qualities, String> result = new EnumMap<>(Qualities.class);
      if (resultingVideos.get().containsKey("QXA")) {
        String url = resultingVideos.get().get("QXA");
        result.put(Qualities.SMALL, enrichUrl(url, "chunklist_b620000.m3u8"));
        result.put(Qualities.NORMAL, enrichUrl(url, "chunklist_b2440000.m3u8"));
        result.put(Qualities.HD, enrichUrl(url, "chunklist_b6100000.m3u8"));
      } else {
        resultingVideos.get().entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEachOrdered(
                        entry ->
                                getQuality(entry.getKey())
                                        .ifPresent(resolution -> result.put(resolution, entry.getValue())));
      }
      return Optional .of(result);
    }
    return Optional.empty();
  }

  private String enrichUrl(String m3u8Url, String videoUrl) {
    // some video urls contain only filename
    if (UrlUtils.getProtocol(videoUrl).isEmpty()) {
      final String m3u8WithoutParameters = UrlUtils.removeParameters(m3u8Url);
      final Optional<String> m3u8File = UrlUtils.getFileName(m3u8WithoutParameters);
      if (m3u8File.isPresent()) {
        return m3u8WithoutParameters.replace(m3u8File.get(), videoUrl);
      }

      final Optional<String> lastSegment = UrlUtils.getLastSegment(m3u8WithoutParameters);
      if (lastSegment.isPresent()) {
        return m3u8WithoutParameters.replace(lastSegment.get(), videoUrl);
      }
    }
    return videoUrl;
  }

  private Optional<Map<String, String>> parseVideoFromSources(JsonElement root) {
    Optional<JsonElement> videoSources = JsonUtils.getElement(root, TAG_VIDEO_FALLBACK);
    if (videoSources.isPresent()) {
      Map<String, String> urls = new HashMap<>();
      for (String key : PREFERED_CODEC) {
        Optional<JsonElement> codecs = JsonUtils.getElement(videoSources.get(), key);
        if (codecs.isPresent() && codecs.get().isJsonArray()) {
          for (JsonElement singleVideo : codecs.get().getAsJsonArray()) {
            Optional<String> tgtUrl = JsonUtils.getElementValueAsString(singleVideo, TAG_VIDEO_FALLBACK_URL);
            Optional<String> qualityValue = JsonUtils.getElementValueAsString(singleVideo, "quality_key");
            if (tgtUrl.isPresent() && !tgtUrl.get().contains("/Jugendschutz") && !tgtUrl.get().contains("/no_drm_support") && !tgtUrl.get().contains("/schwarzung") &&
                qualityValue.isPresent()) {
              urls.put(qualityValue.get(), tgtUrl.get());
            }
          }
          if (!urls.isEmpty()) {
            return Optional.of(urls);
          }
        }
      }
    }
    Optional<Map<String, String>> fallbackThumbnail = parseVideoFromThumbnail(root);
    if (fallbackThumbnail.isPresent()) {
      return fallbackThumbnail;
    }
    Optional<Map<String, String>> fallbackGapless = parseVideoFromGapless(root);
    if (fallbackGapless.isPresent()) {
      return fallbackGapless;
    }
    return Optional.empty();
  }
  
  private Optional<Map<String, String>> parseVideoFromThumbnail(JsonElement root) {
    Map<String, String> urls = new HashMap<>();
    try {
      Optional<JsonElement> id = JsonUtils.getElement(root, TAG_ID);
      Optional<JsonElement> thumbnailSources = JsonUtils.getElement(root, VIDEO_THUMBNAIL);
      if (id.isPresent() && thumbnailSources.isPresent() && thumbnailSources.get().isJsonArray() && thumbnailSources.get().getAsJsonArray().size() > 0 ) {
        Optional<JsonElement> thumbnailSrc = JsonUtils.getElement(thumbnailSources.get().getAsJsonArray().get(0), "src");
        if (thumbnailSrc.isPresent()) {
          int indexId = thumbnailSrc.get().getAsString().indexOf(id.get().getAsString());
          String fromSecondIdOnwards = thumbnailSrc.get().getAsString().substring(indexId + id.get().getAsString().length() + 1);
          String secondId = fromSecondIdOnwards.substring(0, fromSecondIdOnwards.indexOf("_"));
          String url = String.format("https://apasfiis.sf.apa.at/ipad/cms-worldwide_episodes/%s_%s_QXA.mp4/playlist.m3u8", id.get().getAsString(), secondId);
          urls.put("QXA", url);
        }
      }
    } catch (Exception e) {
      LOG.error("generateFallbackVideo", e);
    }
    if (urls.isEmpty()) {
      return Optional.empty();
    }
    return Optional.of(urls);
  }

  private Optional<Map<String, String>> parseVideoFromGapless(JsonElement root) {
    Map<String, String> urls = new HashMap<>();
    try {
      Optional<JsonElement> gaplessSourceAT = JsonUtils.getElement(root, "gapless_sources_austria", "hls");
      if (gaplessSourceAT.isPresent()) {
        gaplessSourceAT.get().getAsJsonArray().forEach( e -> {
          Optional<String> url = JsonUtils.getElementValueAsString(e, "src");
          Optional<String> drm = JsonUtils.getElementValueAsString(e, "is_drm_protected");
          if (url.isPresent() && drm.orElse("").equalsIgnoreCase("false")) {
            urls.put("QXA", url.get());
          }
        });
      }
    } catch (Exception e) {
      LOG.error("generateFallbackVideo", e);
    }
    if (urls.isEmpty()) {
      return Optional.empty();
    }
    return Optional.of(urls);
  }

  private Optional<Map<String, String>> readVideoForTargetCodec(JsonElement urlArray, String targetCodec) {
    Map<String, String> urls = new HashMap<>();
    for (JsonElement videoElement : urlArray.getAsJsonArray()) {
      Optional<String> codec = JsonUtils.getElementValueAsString(videoElement, TAG_VIDEO_CODEC);
      Optional<String> qualityValue = JsonUtils.getElementValueAsString(videoElement, TAG_VIDEO_QUALITY);
      Optional<String> url = JsonUtils.getElementValueAsString(videoElement, TAG_VIDEO_URL);
      if (url.isPresent() && codec.isPresent() && qualityValue.isPresent() && targetCodec.equalsIgnoreCase(codec.get())
              && (OrfOnEpisodeDeserializer.getQuality(qualityValue.get()).isPresent() || qualityValue.orElse("").equalsIgnoreCase("QXA"))) {
        if (!url.get().contains("/Jugendschutz") && !url.get().contains("/no_drm_support") && !url.get().contains("/schwarzung")) {
          urls.put(qualityValue.get(), url.get());
        }
      }
    }
    if (urls.isEmpty()) {
      return Optional.empty();
    }
    return Optional.of(urls);
  }

  private Optional<String> parseWebsite(Optional<String> text) {
    Optional<String> result = Optional.empty();
    if (text.isPresent()) {
      result = Optional.of(text.get());
    }
    return result;
  }

  private Optional<LocalDateTime> parseAiredDate(Optional<String> text) {
    Optional<LocalDateTime> result = Optional.empty();
    if (text.isPresent()) {
      try {
        result = Optional.of(LocalDateTime.parse(text.get(), DateTimeFormatter.ISO_ZONED_DATE_TIME));
      } catch (Exception e) {
        Log.errorLog(873673825, e, "datetimeformatter failed: " + text.get());
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
        Log.errorLog(873673826, e, "duration failed: " + text.get());
        LOG.error("Duration failed for string {} exception {}", text.get(), e);
      }
    }
    return Optional.empty();

  }

}
