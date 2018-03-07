package de.mediathekview.mserver.crawler.ard.json;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import de.mediathekview.mlib.daten.Resolution;
import de.mediathekview.mserver.base.messages.ServerMessages;
import de.mediathekview.mserver.base.utils.UrlUtils;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import java.util.HashSet;
import java.util.Set;

public class ArdMediaArrayToDownloadUrlsConverter {

  private static final String ELEMENT_STREAM = "_stream";
  private static final Logger LOG =
      LogManager.getLogger(ArdMediaArrayToDownloadUrlsConverter.class);
  private static final String URL_PREFIX_PATTERN = "\\w+:";
  private static final String URL_PATTERN = "\\w+.*";
  private static final String ELEMENT_MEDIA_ARRAY = "_mediaArray";
  private static final String ELEMENT_MEDIA_STREAM_ARRAY = "_mediaStreamArray";
  private static final String ELEMENT_QUALITY = "_quality";
  private static final String ELEMENT_SERVER = "_server";
  private static final String PROTOCOL_RTMP = "rtmp";

  private ArdMediaArrayToDownloadUrlsConverter() {}

  public static Map<Resolution, URL> toDownloadUrls(final JsonElement aJsonElement,
      final AbstractCrawler aCrawler) {
    final Map<Resolution, URL> downloadUrls = new EnumMap<>(Resolution.class);

    Map<Resolution, Set<String>> availableUrls = new EnumMap<>(Resolution.class);
    availableUrls.put(Resolution.SMALL, new HashSet<>());
    availableUrls.put(Resolution.NORMAL, new HashSet<>());
    availableUrls.put(Resolution.HD, new HashSet<>());

    final JsonArray mediaArray = aJsonElement.getAsJsonObject().getAsJsonArray(ELEMENT_MEDIA_ARRAY);
    parseMediaArray(mediaArray, availableUrls);
    
    extractRelevantUrls(availableUrls, downloadUrls, aCrawler);

    return downloadUrls;
  }
  
  private static void extractRelevantUrls(final Map<Resolution, Set<String>> aUrls, 
    final Map<Resolution, URL> aDownloadUrls, 
    final AbstractCrawler aCrawler) {
    
    aUrls.entrySet().forEach(entry -> {
      Set<String> urls = entry.getValue();
      if (!urls.isEmpty()) {
    
        String url = determineUrl(urls);
        if (!url.isEmpty()) {
        
          try {
            aDownloadUrls.put(entry.getKey(), new URL(url));
          } catch (MalformedURLException malformedURLException) {
            LOG.error("A download URL is defect.", malformedURLException);
            aCrawler.printMessage(ServerMessages.DEBUG_INVALID_URL, aCrawler.getSender().getName(),
                url);
          }
        }
      }
    });
  }
  
  /**
   * returns the url to use for downloads
   * uses the following order:
   * mp4 > m3u8 > Rest
   * @param aUrls list of possible urls
   * @return the download url
   */
  private static String determineUrl(Set<String> aUrls) {
    String usedFileType = "";
    String relevantUrl = "";
    
    for (String url: aUrls) {
      Optional<String> fileType = UrlUtils.getFileType(url);
      if (fileType.isPresent()) {
        switch(fileType.get()) {
          case "mp4":
            return url;
          case "m3u8":
            if (!usedFileType.equals("mp4")) {
              usedFileType = fileType.get();
              relevantUrl = url;
            }
            break;
          default:
            if (usedFileType.isEmpty()) {
              usedFileType = fileType.get();
              relevantUrl = url;
            }
        }
      }
    }
    
    return relevantUrl;
  }
  
  private static void parseMediaArray(JsonArray aMediaArray, Map<Resolution, Set<String>> aUrls) {
    
    aMediaArray.forEach(mediaEntry -> {
      final JsonArray mediaStreamArray = mediaEntry.getAsJsonObject()
              .getAsJsonArray(ELEMENT_MEDIA_STREAM_ARRAY);

      parseMediaStreamArray(mediaStreamArray, aUrls);
    });    
  }

  private static void parseMediaStreamArray(JsonArray aMediaStreamArray, Map<Resolution, Set<String>> aUrls) {

    for (int i = 0; i < aMediaStreamArray.size(); i++) {
      final JsonElement videoElement = aMediaStreamArray.get(i);
      final String qualityAsText =
          videoElement.getAsJsonObject().get(ELEMENT_QUALITY).getAsString();
      
      Optional<Resolution> quality = getQuality(qualityAsText);
      if (quality.isPresent()) {
        String downloadUrl = "";
        
        if (videoElement.getAsJsonObject().has(ELEMENT_SERVER)) {
          final String baseUrl = videoElement.getAsJsonObject().get(ELEMENT_SERVER).getAsString();
          downloadUrl = videoElementToUrl(videoElement, baseUrl);
          addUrl(downloadUrl, quality.get(), aUrls);
        } 
        
        if (downloadUrl.isEmpty() && videoElement.getAsJsonObject().has(ELEMENT_STREAM)) {
          JsonElement streamObject = videoElement.getAsJsonObject().get(ELEMENT_STREAM);
          if (streamObject.isJsonPrimitive()) {
            final String baseUrl = streamObject.getAsString();
            downloadUrl= videoElementToUrl(videoElement, baseUrl);
            addUrl(downloadUrl, quality.get(), aUrls);          
          } else if(streamObject.isJsonArray()) {
            JsonArray streamArray = streamObject.getAsJsonArray();
            streamArray.forEach(stream -> {
              final String baseUrl = stream.getAsString();
              addUrl(baseUrl, quality.get(), aUrls);          
            });
          }
        }
      }
    }
  }
  
  private static void addUrl(final String aUrl, 
    final Resolution quality, 
    Map<Resolution, Set<String>> aUrls) {

    if (!aUrl.isEmpty()) {
      if (aUrl.startsWith(PROTOCOL_RTMP)) {
        LOG.debug("Found an Sendung with the old RTMP format: " + aUrl);
      } else {
        aUrls.get(quality).add(aUrl);
      }
    }
  }
  
  private static Optional<Resolution> getQuality(String aQualityAsText) {
    int qualityNumber;
    try {
      if (aQualityAsText.equals("auto")) {
        qualityNumber = -1;
      } else {
        qualityNumber = Integer.parseInt(aQualityAsText);
      }
    } catch (final NumberFormatException numberFormatException) {
      LOG.debug("Can't convert quality %s to an integer.", aQualityAsText,
          numberFormatException);
      qualityNumber = -1;
    }

    if (qualityNumber > 0) {
      return Optional.of(getQualityForNumber(qualityNumber));
    }

    return Optional.empty();
  }
  
  private static Resolution getQualityForNumber(final int i) {
    switch (i) {
      case 0:
        return Resolution.VERY_SMALL;

      case 1:
        return Resolution.SMALL;

      case 3:
        return Resolution.HD;

      case 2:
      default:
        return Resolution.NORMAL;

    }
  }

  private static String videoElementToUrl(final JsonElement aVideoElement, final String aBaseUrl) {
    if (aBaseUrl.isEmpty()) {
      return aBaseUrl;
    }
    
    String url = aVideoElement.getAsJsonObject().get(ELEMENT_STREAM).getAsString();
    if (url.matches(URL_PREFIX_PATTERN + URL_PATTERN)) {
      url = url.replaceFirst(URL_PREFIX_PATTERN, aBaseUrl);
    }
    return url;
  }

}
