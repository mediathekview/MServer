package mServer.crawler.sender.ard.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import mServer.crawler.sender.base.FilmUrlInfoDto;
import mServer.crawler.sender.base.JsonUtils;
import mServer.crawler.sender.base.UrlUtils;
import mServer.crawler.sender.newsearch.Qualities;
import mServer.crawler.sender.swr.SwrUrlOptimizer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ArdMediaArrayToDownloadUrlsConverter {

  private static final String ELEMENT_STREAM = "_stream";
  private static final Logger LOG
          = LogManager.getLogger(ArdMediaArrayToDownloadUrlsConverter.class);
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

  private static final SwrUrlOptimizer SWR_OPTIMIZER = new SwrUrlOptimizer();

  private ArdMediaArrayToDownloadUrlsConverter() {
  }

  public static Map<Qualities, URL> toDownloadUrls(final JsonElement aJsonElement) {
    final Map<Qualities, URL> downloadUrls = new EnumMap<>(Qualities.class);

    final Map<Qualities, LinkedHashSet<FilmUrlInfoDto>> availableUrls
            = new EnumMap<>(Qualities.class);
    availableUrls.put(Qualities.SMALL, new LinkedHashSet<>());
    availableUrls.put(Qualities.NORMAL, new LinkedHashSet<>());
    availableUrls.put(Qualities.HD, new LinkedHashSet<>());

    final int pluginValue = extractPluginValue(aJsonElement.getAsJsonObject());

    final JsonArray mediaArray = aJsonElement.getAsJsonObject().getAsJsonArray(ELEMENT_MEDIA_ARRAY);
    parseMediaArray(pluginValue, mediaArray, availableUrls);

    extractRelevantUrls(availableUrls, downloadUrls);

    return downloadUrls;
  }

  private static void addUrl(final String aUrl, final Qualities quality,
          final Map<Qualities, LinkedHashSet<FilmUrlInfoDto>> aUrls, final Optional<String> aHeight,
          final Optional<String> aWidth) {

    if (!aUrl.isEmpty()) {
      if (aUrl.startsWith(PROTOCOL_RTMP)) {
        LOG.debug("Found an Sendung with the old RTMP format: " + aUrl);
      } else {
        final FilmUrlInfoDto info = new FilmUrlInfoDto(UrlUtils.addProtocolIfMissing(aUrl, "http:"));
        if (aHeight.isPresent() && aWidth.isPresent()) {
          info.setResolution(Integer.parseInt(aWidth.get()), Integer.parseInt(aHeight.get()));
        }
        aUrls.get(quality).add(info);
      }
    }
  }

  /**
   * returns the url to use for downloads uses the following order: mp4 > m3u8 >
   * Rest.
   *
   * @param aUrls list of possible urls
   * @return the download url
   */
  private static String determineUrl(final Qualities resolution, final Set<FilmUrlInfoDto> aUrls) {

    if (aUrls.isEmpty()) {
      return "";
    }

    FilmUrlInfoDto ardUrlInfo;

    List<FilmUrlInfoDto> urls = filterUrls(aUrls, "mp4");
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

  private static void extractRelevantUrls(final Map<Qualities, LinkedHashSet<FilmUrlInfoDto>> aUrls,
          final Map<Qualities, URL> aDownloadUrls) {

    aUrls.entrySet().forEach(entry -> {
      final Set<FilmUrlInfoDto> urls = entry.getValue();
      if (!urls.isEmpty()) {

        final String url = determineUrl(entry.getKey(), urls);
        if (!url.isEmpty()) {

          try {
            aDownloadUrls.put(entry.getKey(), new URL(optimizeUrl(entry.getKey(), url)));
          } catch (final MalformedURLException malformedUrlException) {
            LOG.error("A download URL is defect.", malformedUrlException);
          }
        }
      }
    });
  }

  private static String optimizeUrl(Qualities key, String url) {
    if (key == Qualities.HD) {
      return SWR_OPTIMIZER.optimizeHdUrl(url);
    }

    return url;
  }

  private static List<FilmUrlInfoDto> filterUrls(final Set<FilmUrlInfoDto> aUrls, final String aFileType) {
    return aUrls.stream().filter(u -> {
      if (u.getFileType().isPresent()) {
        return u.getFileType().get().equalsIgnoreCase(aFileType);
      }
      return false;
    }).collect(Collectors.toList());
  }
  
  private static Optional<Qualities> getQuality(final String aQualityAsText) {
    int qualityNumber;
    try {
      if (aQualityAsText.equals("auto")) {
        // Some films only contains "auto" quality with a m3u8-url
        // treat quality "auto" as NORMAL though the m3u8-url is returned
        return Optional.of(Qualities.NORMAL);
      } else {
        qualityNumber = Integer.parseInt(aQualityAsText);
      }
    } catch (final NumberFormatException numberFormatException) {
      LOG.debug("Can't convert quality %s to an integer.", aQualityAsText, numberFormatException);
      qualityNumber = -1;
    }

    if (qualityNumber > 0) {
      return Optional.of(getQualityForNumber(qualityNumber));
    }

    return Optional.empty();
  }

  private static Qualities getQualityForNumber(final int i) {
    switch (i) {
      case 0:
      case 1:
        return Qualities.SMALL;

      case 3:
      case 4:
        return Qualities.HD;

      case 2:
      default:
        return Qualities.NORMAL;

    }
  }

  private static FilmUrlInfoDto getRelevantUrlMp4(final Qualities aQualities,
          final List<FilmUrlInfoDto> aUrls) {
    switch (aQualities) {
      case SMALL:
        // the first url is the best
        return aUrls.get(0);
      case NORMAL:
        // the last url is the best
        return aUrls.get(aUrls.size() - 1);
      case HD:
        for (final FilmUrlInfoDto info : aUrls) {
          if (info.getWidth() >= 1280 && info.getHeight() >= 720) {
            return info;
          }
          if (info.getWidth() == 0 && info.getHeight() == 0) {
            final String url = info.getUrl();

            // Sometimes videos with a resolution of 960 are listed as quality HD
            if (!url.startsWith("960", url.lastIndexOf('/') + 1)) {
              return info;
            }
          }
        }
        return null;
      default:
        return null;
    }
  }

  private static void parseMediaArray(final int aPluginValue, final JsonArray aMediaArray,
          final Map<Qualities, LinkedHashSet<FilmUrlInfoDto>> aUrls) {

    aMediaArray.forEach(mediaEntry -> {

      final JsonObject mediaObject = mediaEntry.getAsJsonObject();
      final int pluginValue = mediaObject.get(ELEMENT_PLUGIN).getAsInt();

      // only use the urls of the relevant plugin
      if (pluginValue == aPluginValue) {
        final JsonArray mediaStreamArray = mediaObject.getAsJsonArray(ELEMENT_MEDIA_STREAM_ARRAY);

        parseMediaStreamArray(mediaStreamArray, aUrls);
      }
    });
  }

  private static void parseMediaStreamArray(final JsonArray aMediaStreamArray,
          final Map<Qualities, LinkedHashSet<FilmUrlInfoDto>> aUrls) {

    for (int i = 0; i < aMediaStreamArray.size(); i++) {
      final JsonElement videoElement = aMediaStreamArray.get(i);
      final String qualityAsText
              = videoElement.getAsJsonObject().get(ELEMENT_QUALITY).getAsString();

      final Optional<Qualities> quality = getQuality(qualityAsText);
      if (quality.isPresent()) {
        parseMediaStreamServer(aUrls, videoElement, quality.get());
        parseMediaStreamStream(aUrls, videoElement, quality.get());
      }
    }
  }

  private static void parseMediaStreamServer(final Map<Qualities, LinkedHashSet<FilmUrlInfoDto>> aUrls,
          final JsonElement videoElement, final Qualities quality) {
    if (videoElement.getAsJsonObject().has(ELEMENT_SERVER)) {
      final String baseUrl = videoElement.getAsJsonObject().get(ELEMENT_SERVER).getAsString();
      final String downloadUrl = videoElementToUrl(videoElement, baseUrl);
      addUrl(downloadUrl, quality, aUrls, Optional.empty(), Optional.empty());
    }
  }

  private static void parseMediaStreamStream(final Map<Qualities, LinkedHashSet<FilmUrlInfoDto>> aUrls,
          final JsonElement videoElement, final Qualities quality) {

    if (videoElement.getAsJsonObject().has(ELEMENT_STREAM)) {

      final JsonObject videoObject = videoElement.getAsJsonObject();
      final JsonElement streamObject = videoObject.get(ELEMENT_STREAM);

      final Optional<String> height = JsonUtils.getAttributeAsString(videoObject, ELEMENT_HEIGHT);
      final Optional<String> width = JsonUtils.getAttributeAsString(videoObject, ELEMENT_WIDTH);

      if (streamObject.isJsonPrimitive()) {
        final String baseUrl = streamObject.getAsString();
        final String downloadUrl = videoElementToUrl(videoElement, baseUrl);
        addUrl(downloadUrl, quality, aUrls, height, width);
      } else if (streamObject.isJsonArray()) {
        final JsonArray streamArray = streamObject.getAsJsonArray();
        streamArray.forEach(stream -> {
          final String baseUrl = stream.getAsString();
          addUrl(baseUrl, quality, aUrls, height, width);
        });
      }
    }
  }

  private static String videoElementToUrl(final JsonElement aVideoElement, final String aBaseUrl) {
    if (aBaseUrl.isEmpty()) {
      return aBaseUrl;
    }

    String url = aVideoElement.getAsJsonObject().get(ELEMENT_STREAM).getAsString();
    if (url.equals(aBaseUrl)) {
      return url;
    }
    if (url.matches(URL_PREFIX_PATTERN + URL_PATTERN)) {
      url = url.replaceFirst(URL_PREFIX_PATTERN, aBaseUrl);
    } else {
      url = aBaseUrl + url;
    }
    return url;
  }

}
