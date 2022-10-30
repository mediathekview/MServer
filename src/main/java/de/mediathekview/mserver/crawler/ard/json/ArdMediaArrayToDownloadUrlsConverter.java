package de.mediathekview.mserver.crawler.ard.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.mediathekview.mlib.daten.Resolution;
import de.mediathekview.mserver.base.messages.ServerMessages;
import de.mediathekview.mserver.base.utils.JsonUtils;
import de.mediathekview.mserver.base.utils.UrlUtils;
import de.mediathekview.mserver.crawler.ard.ArdFilmUrlInfoDto;
import de.mediathekview.mserver.crawler.ard.ArdUrlOptimizer;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class ArdMediaArrayToDownloadUrlsConverter {

  private static final String ELEMENT_STREAM = "_stream";
  private static final Logger LOG =
      LogManager.getLogger(ArdMediaArrayToDownloadUrlsConverter.class);
  private static final String URL_PREFIX_PATTERN = "\\w+:";
  private static final String URL_PATTERN = "\\w+.*";
  private static final String ELEMENT_HEIGHT = "_height";
  private static final String ELEMENT_MEDIA_ARRAY = "_mediaArray";
  private static final String ELEMENT_MEDIA_STREAM_ARRAY = "_mediaStreamArray";
  private static final String ELEMENT_PLUGIN = "_plugin";
  private static final String ELEMENT_QUALITY = "_quality";
  private static final String ELEMENT_SERVER = "_server";
  private static final String ELEMENT_SORT_ARRAY = "_sortierArray";
  private static final String ELEMENT_WIDTH = "_width";
  private static final String PROTOCOL_RTMP = "rtmp";

  private static final String FILE_TYPE_F4M = "f4m";
  private static final String VIDEO_RESOLUTION = "960";

  private final ArdUrlOptimizer ardOptimizer;
  private final Map<Resolution, Set<ArdFilmUrlInfoDto>> urls;
  private AbstractCrawler crawler;

  public ArdMediaArrayToDownloadUrlsConverter(final AbstractCrawler aCrawler) {
    crawler = aCrawler;
    ardOptimizer = new ArdUrlOptimizer(crawler);
    urls = new EnumMap<>(Resolution.class);
  }

  private static List<ArdFilmUrlInfoDto> filterUrls(
      final Set<ArdFilmUrlInfoDto> aUrls, final String aFileType) {
    return aUrls.stream()
        .filter(
            url ->
                url.getFileType().isPresent()
                    && url.getFileType().get().equalsIgnoreCase(aFileType))
        .collect(Collectors.toList());
  }

  private static Optional<Resolution> getQuality(final String qualityAsText) {
    int qualityNumber;
    try {
      if (qualityAsText.equals("auto")) {
        // Some films only contains "auto" quality with a m3u8-url
        // treat quality "auto" as NORMAL though the m3u8-url is returned
        return Optional.of(Resolution.NORMAL);
      } else {
        qualityNumber = Integer.parseInt(qualityAsText);
      }
    } catch (final NumberFormatException numberFormatException) {
      LOG.debug("Can't convert quality {} to an integer.", qualityAsText, numberFormatException);
      qualityNumber = -1;
    }

    if (qualityNumber > 0) {
      return Optional.of(getQualityForNumber(qualityNumber));
    }

    return Optional.empty();
  }

  /**
   * returns the url to use for downloads uses the following order: mp4 > m3u8 > Rest.
   *
   * @param aUrls list of possible urls
   * @return the download url
   */
  private static String determineUrl(
      final Resolution resolution, final Set<ArdFilmUrlInfoDto> aUrls) {

    if (aUrls.isEmpty()) {
      return "";
    }

    final ArdFilmUrlInfoDto ardUrlInfo;

    List<ArdFilmUrlInfoDto> urls = filterUrls(aUrls, "mp4");
    if (!urls.isEmpty()) {
      ardUrlInfo = getRelevantUrlMp4(resolution, urls);
    } else {

      urls = filterUrls(aUrls, "m3u8");
      if (!urls.isEmpty()) {
        ardUrlInfo = urls.get(0);
      } else {
        ardUrlInfo = aUrls.iterator().next();
      }
    }

    if (ardUrlInfo != null) {
      return ardUrlInfo.getUrl();
    }

    return "";
  }

  private static int extractPluginValue(final JsonObject aJsonObject) {
    if (aJsonObject.has(ELEMENT_SORT_ARRAY)) {
      final JsonElement pluginElement = aJsonObject.get(ELEMENT_SORT_ARRAY);
      if (pluginElement.isJsonArray()) {
        final JsonArray pluginArray = aJsonObject.get(ELEMENT_SORT_ARRAY).getAsJsonArray();
        return pluginArray.get(0).getAsInt();
      }
    }

    return 1;
  }

  private static boolean isFileTypeRelevant(
      final Map.Entry<Resolution, Set<ArdFilmUrlInfoDto>> entry) {
    return entry.getValue().stream()
        .anyMatch(
            video ->
                video.getFileType().isPresent()
                    && !FILE_TYPE_F4M.equalsIgnoreCase(video.getFileType().get()));
  }

  private static Resolution getQualityForNumber(final int qualityIndicator) {
    return switch (qualityIndicator) {
      case 0 -> Resolution.VERY_SMALL;
      case 1 -> Resolution.SMALL;
      case 3, 4 -> Resolution.HD;
      case 5 -> Resolution.UHD;
      case 2 -> Resolution.NORMAL;
      default -> Resolution.NORMAL;
    };
  }

  private static ArdFilmUrlInfoDto getRelevantUrlMp4(
      final Resolution aResolution, final List<ArdFilmUrlInfoDto> aUrls) {
    switch (aResolution) {
      case SMALL:
        // the first url is the best
        return aUrls.get(0);
      case NORMAL:
        // the last url is the best
        return aUrls.get(aUrls.size() - 1);
      case HD:
        ArdFilmUrlInfoDto relevantInfo = null;

        for (final ArdFilmUrlInfoDto info : aUrls) {
          if (info.getWidth() >= 1280 && info.getHeight() >= 720) {
            if (relevantInfo == null) {
              relevantInfo = info;
            } else if (relevantInfo.getQuality().compareTo(info.getQuality()) < 0) {
              relevantInfo = info;
            }
          }
          if (info.getWidth() == 0 && info.getHeight() == 0) {
            final String url = info.getUrl();

            // Sometimes videos with a resolution of 960 are listed as quality HD
            if (!url.startsWith(VIDEO_RESOLUTION, url.lastIndexOf('/') + 1)) {
              if (relevantInfo == null) {
                relevantInfo = info;
              } else if (relevantInfo.getQuality().compareTo(info.getQuality()) < 0) {
                relevantInfo = info;
              }
            }
          }
        }
        return relevantInfo;
      default:
        return null;
    }
  }

  Map<Resolution, URL> toDownloadUrls(
      final JsonElement jsonElement, final AbstractCrawler crawler) {
    this.crawler = crawler;
    final int pluginValue = extractPluginValue(jsonElement.getAsJsonObject());
    final JsonArray mediaArray = jsonElement.getAsJsonObject().getAsJsonArray(ELEMENT_MEDIA_ARRAY);
    parseMediaArray(pluginValue, mediaArray);
    return extractRelevantUrls();
  }

  private void addUrl(
      final String url,
      final String qualityText,
      final Resolution quality,
      @Nullable final String height,
      @Nullable final String width) {

    if (!url.isEmpty()) {
      if (url.startsWith(PROTOCOL_RTMP)) {
        LOG.debug("Found an Sendung with the old RTMP format: {}", url);
      } else {
        final ArdFilmUrlInfoDto info =
            new ArdFilmUrlInfoDto(
                UrlUtils.removeParameters(UrlUtils.addProtocolIfMissing(url, "https:")),
                qualityText);
        if (height != null && width != null) {
          info.setResolution(Integer.parseInt(width), Integer.parseInt(height));
        }

        if (!urls.containsKey(quality)) {
          urls.put(quality, new LinkedHashSet<>());
        }
        urls.get(quality).add(info);
      }
    }
  }

  private Map<Resolution, URL> extractRelevantUrls() {
    final Map<Resolution, URL> downloadUrls = new EnumMap<>(Resolution.class);

    removeAutoM3u8IfMp4Exists();

    urls.entrySet().stream()
        .filter(entry -> !entry.getValue().isEmpty())
        .filter(ArdMediaArrayToDownloadUrlsConverter::isFileTypeRelevant)
        .forEach(
            entry -> finalizeUrl(entry).ifPresent(url -> downloadUrls.put(entry.getKey(), url)));

    // add lowest HD-Url as NORMAL if normal is not present
    if (!downloadUrls.containsKey(Resolution.NORMAL) && urls.containsKey(Resolution.HD)) {
      Optional<URL> normalUrl = determineNormalUrlFromHd(urls.get(Resolution.HD));
      normalUrl.ifPresent(url -> downloadUrls.put(Resolution.NORMAL, url));
    }

    return downloadUrls;
  }

  // removes m3u8-url with quality=auto if at least one mp4 url exists
  // otherwise m3u8-url could be the normal url while small+hd contains mp4-urls
  private void removeAutoM3u8IfMp4Exists() {
    AtomicBoolean existsMp4 = new AtomicBoolean(false);

    urls.values().forEach(set ->
      set.forEach(value -> {
        final Optional<String> fileType = UrlUtils.getFileType(value.getUrl());
        if (fileType.isPresent() && "mp4".equalsIgnoreCase(fileType.get())) {
          existsMp4.set(true);
        }
      }));

    if(existsMp4.get() && urls.containsKey(Resolution.NORMAL)) {
      urls.get(Resolution.NORMAL).removeIf(urlInfo -> urlInfo.getQuality().equalsIgnoreCase("auto"));
    }
  }

  private Optional<URL> determineNormalUrlFromHd(Set<ArdFilmUrlInfoDto> ardFilmUrlInfoDtos) {
    ArdFilmUrlInfoDto relevantInfo = null;

    for (final ArdFilmUrlInfoDto info : ardFilmUrlInfoDtos) {
      if (info.getWidth() > 0 && info.getHeight() > 0) {
        if (relevantInfo == null) {
          relevantInfo = info;
        } else if (relevantInfo.getQuality().compareTo(info.getQuality()) > 0) {
          relevantInfo = info;
        }
      }
    }

    if (relevantInfo != null) {
      try {
        return Optional.of(new URL(relevantInfo.getUrl()));
      } catch (final MalformedURLException malformedUrlException) {
        LOG.error("A download URL is defect.", malformedUrlException);
        crawler.printMessage(ServerMessages.DEBUG_INVALID_URL, crawler.getSender().getName(), relevantInfo.getUrl());
      }
    }

    return Optional.empty();
  }

  private Optional<URL> finalizeUrl(final Map.Entry<Resolution, Set<ArdFilmUrlInfoDto>> entry) {
    final String url = determineUrl(entry.getKey(), entry.getValue());
    if (!url.isEmpty()) {
      try {
        return Optional.of(new URL(optimizeUrl(entry.getKey(), url)));
      } catch (final MalformedURLException malformedUrlException) {
        LOG.error("A download URL is defect.", malformedUrlException);
        crawler.printMessage(ServerMessages.DEBUG_INVALID_URL, crawler.getSender().getName(), url);
      }
    }
    return Optional.empty();
  }

  private String optimizeUrl(final Resolution key, final String url) {
    if (key == Resolution.HD) {
      return ardOptimizer.optimizeHdUrl(url);
    }

    return url;
  }

  private void parseMediaArray(final int pluginValue, final JsonArray mediaArray) {
    StreamSupport.stream(mediaArray.spliterator(), false)
        .map(JsonElement::getAsJsonObject)
        .filter(mediaObj -> mediaObj.get(ELEMENT_PLUGIN).getAsInt() == pluginValue)
        .map(mediaObj -> mediaObj.getAsJsonArray(ELEMENT_MEDIA_STREAM_ARRAY))
        .forEach(this::parseMediaStreamArray);
  }

  private void parseMediaStreamArray(final JsonArray mediaStreamArray) {
    for (final JsonElement videoElement : mediaStreamArray) {
      final String qualityAsText =
          videoElement.getAsJsonObject().get(ELEMENT_QUALITY).getAsString();
      final Optional<Resolution> quality = getQuality(qualityAsText);
      if (quality.isPresent()) {
        parseMediaStreamServer(videoElement, qualityAsText, quality.get());
        parseMediaStreamStream(videoElement, qualityAsText, quality.get());
      }
    }
  }

  private void parseMediaStreamServer(
      final JsonElement videoElement, final String qualityText, final Resolution quality) {
    if (videoElement.getAsJsonObject().has(ELEMENT_SERVER)) {
      final String baseUrl = videoElement.getAsJsonObject().get(ELEMENT_SERVER).getAsString();
      final String downloadUrl = videoElementToUrl(videoElement, baseUrl);
      addUrl(downloadUrl, qualityText, quality, null, null);
    }
  }

  private void parseMediaStreamStream(
      final JsonElement videoElement, final String qualityText, final Resolution quality) {
    if (videoElement.getAsJsonObject().has(ELEMENT_STREAM)) {

      final JsonObject videoObject = videoElement.getAsJsonObject();
      final JsonElement streamObject = videoObject.get(ELEMENT_STREAM);

      final Optional<String> height = JsonUtils.getAttributeAsString(videoObject, ELEMENT_HEIGHT);
      final Optional<String> width = JsonUtils.getAttributeAsString(videoObject, ELEMENT_WIDTH);

      if (streamObject.isJsonPrimitive()) {
        final String baseUrl = streamObject.getAsString();
        final String downloadUrl = videoElementToUrl(videoElement, baseUrl);
        addUrl(downloadUrl, qualityText, quality, height.orElse(null), width.orElse(null));
      } else if (streamObject.isJsonArray()) {
        StreamSupport.stream(streamObject.getAsJsonArray().spliterator(), false)
            .map(JsonElement::getAsString)
            .forEach(baseUrl -> addUrl(baseUrl, qualityText, quality, height.orElse(null), width.orElse(null)));
      }
    }
  }

  private String videoElementToUrl(final JsonElement videoElement, final String baseUrl) {
    if (baseUrl.isEmpty()) {
      return baseUrl;
    }

    String url = videoElement.getAsJsonObject().get(ELEMENT_STREAM).getAsString();
    if (url.equals(baseUrl)) {
      return url;
    }
    if (url.matches(URL_PREFIX_PATTERN + URL_PATTERN)) {
      url = url.replaceFirst(URL_PREFIX_PATTERN, baseUrl);
    } else {
      url = baseUrl + url;
    }
    return url;
  }
}
