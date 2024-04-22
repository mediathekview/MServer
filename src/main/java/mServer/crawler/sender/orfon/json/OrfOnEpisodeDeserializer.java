package mServer.crawler.sender.orfon.json;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import de.mediathekview.mlib.tool.Log;
import mServer.crawler.sender.base.JsonUtils;
import mServer.crawler.sender.base.Qualities;
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

  private static final String[] TAG_SUBTITLE_SECTION = {"_embedded", "subtitle"};
  private static final String TAG_SUBTITLE_TTML = "ttml_url";
  private static final String[] PREFERED_CODEC = {"hls", "hds", "progressive"};
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
            parseAiredDate(JsonUtils.getElementValueAsString(jsonElement, TAG_AIRED)),
            parseDuration(JsonUtils.getElementValueAsString(jsonElement, TAG_DURATION)),
            JsonUtils.getElementValueAsString(jsonElement, TAG_DESCRIPTION),
            parseWebsite(JsonUtils.getElementValueAsString(jsonElement, TAG_SHARE_BODY)),
            optimizeUrls(parseUrl(jsonElement)),
            buildOrResolveSubs(jsonElement)

    );
  }

  private Optional<Map<Qualities, String>> optimizeUrls(Optional<Map<Qualities, String>> urls) {
    if (urls.isPresent() && urls.get().size() == 1) {
      final Map<Qualities, String> urlMap = urls.get();
      final String urlToOptimize = urlMap.get(Qualities.NORMAL);
      urlMap.put(Qualities.SMALL, urlToOptimize.replace("QXA", "Q4A"));
      urlMap.put(Qualities.NORMAL, urlToOptimize.replace("QXA", "Q6A"));
      urlMap.put(Qualities.HD, urlToOptimize.replace("QXA", "Q8C"));
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

  private Optional<Map<Qualities, String>> parseUrl(JsonElement jsonElement) {
    Optional<JsonElement> videoPath1 = JsonUtils.getElement(jsonElement, TAG_VIDEO_PATH_1);
    if (videoPath1.isEmpty() || !videoPath1.get().isJsonArray() || videoPath1.get().getAsJsonArray().size() == 0) {
      return Optional.empty();
    }
    // We need to fallback to episode.sources in case there are many elements in the playlist
    if (videoPath1.get().getAsJsonArray().size() == 1) {
      Optional<JsonElement> videoPath2 = JsonUtils.getElement(videoPath1.get().getAsJsonArray().get(0), TAG_VIDEO_PATH_2);
      if (videoPath2.isEmpty() || !videoPath2.get().isJsonArray()) {
        return Optional.empty();
      }
      for (String key : PREFERED_CODEC) {
        Optional<Map<Qualities, String>> resultingVideos = readVideoForTargetCodec(videoPath2.get(), key);
        if (resultingVideos.isPresent()) {
          return resultingVideos;
        }
      }
    }
    return parseFallbackVideo(jsonElement);
  }

  private Optional<Map<Qualities, String>> parseFallbackVideo(JsonElement root) {
    Optional<JsonElement> videoSources = JsonUtils.getElement(root, TAG_VIDEO_FALLBACK);
    if (videoSources.isPresent()) {
      Map<Qualities, String> urls = new EnumMap<>(Qualities.class);
      for (String key : PREFERED_CODEC) {
        Optional<JsonElement> codecs = JsonUtils.getElement(videoSources.get(), key);
        if (codecs.isPresent() && codecs.get().isJsonArray()) {
          for (JsonElement singleVideo : codecs.get().getAsJsonArray()) {
            Optional<String> tgtUrl = JsonUtils.getElementValueAsString(singleVideo, TAG_VIDEO_FALLBACK_URL);
            if (tgtUrl.isPresent() && !tgtUrl.get().contains("/Jugendschutz") && !tgtUrl.get().contains("/no_drm_support") && !tgtUrl.get().contains("/schwarzung")) {
              urls.put(Qualities.NORMAL, tgtUrl.get());
              return Optional.of(urls);
            }
          }
        }
      }
    }
    return Optional.empty();
  }

  private Optional<Map<Qualities, String>> readVideoForTargetCodec(JsonElement urlArray, String targetCodec) {
    Map<Qualities, String> urls = new EnumMap<>(Qualities.class);
    for (JsonElement videoElement : urlArray.getAsJsonArray()) {
      Optional<String> codec = JsonUtils.getElementValueAsString(videoElement, TAG_VIDEO_CODEC);
      Optional<String> qualityValue = JsonUtils.getElementValueAsString(videoElement, TAG_VIDEO_QUALITY);
      Optional<String> url = JsonUtils.getElementValueAsString(videoElement, TAG_VIDEO_URL);
      if (url.isPresent() && codec.isPresent() && qualityValue.isPresent() && targetCodec.equalsIgnoreCase(codec.get()) && OrfOnEpisodeDeserializer.getQuality(qualityValue.get()).isPresent()) {
        if (!url.get().contains("/Jugendschutz") && !url.get().contains("/no_drm_support") && !url.get().contains("/schwarzung")) {
          final Optional<Qualities> quality = OrfOnEpisodeDeserializer.getQuality(qualityValue.get());
          if (quality.isPresent()) {
            urls.put(quality.get(), url.get());
          }
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

  ///////////////

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
