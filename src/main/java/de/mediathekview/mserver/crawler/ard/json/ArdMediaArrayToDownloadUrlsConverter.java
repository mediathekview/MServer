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
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;

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

    final JsonArray mediaStreamArray =
        aJsonElement.getAsJsonObject().getAsJsonArray(ELEMENT_MEDIA_ARRAY).get(0).getAsJsonObject()
            .getAsJsonArray(ELEMENT_MEDIA_STREAM_ARRAY);

    for (int i = 0; i < mediaStreamArray.size(); i++) {
      final JsonElement vidoeElement = mediaStreamArray.get(i);
      final String qualityAsText =
          vidoeElement.getAsJsonObject().get(ELEMENT_QUALITY).getAsString();

      Optional<String> baseUrl = vidoeElement.getAsJsonObject().has(ELEMENT_SERVER)
          ? Optional.of(vidoeElement.getAsJsonObject().get(ELEMENT_SERVER).getAsString())
          : Optional.empty();
      if (!baseUrl.isPresent() || baseUrl.get().isEmpty()) {
        baseUrl = vidoeElement.getAsJsonObject().has(ELEMENT_STREAM)
            ? Optional.of(vidoeElement.getAsJsonObject().get(ELEMENT_STREAM).getAsString())
            : Optional.empty();
      }
      if (baseUrl.isPresent() && baseUrl.get().startsWith(PROTOCOL_RTMP)) {
        LOG.debug("Found an Sendung with the old RTMP format: "
            + videoElementToUrl(vidoeElement, baseUrl.get()));
      } else {
        int qualityNumber;
        try {
          qualityNumber = Integer.parseInt(qualityAsText);
        } catch (final NumberFormatException numberFormatException) {
          LOG.debug("Can't convert quality %s to an integer.", qualityAsText,
              numberFormatException);
          qualityNumber = -1;
        }

        if (qualityNumber > 0 || mediaStreamArray.size() == 1) {
          final Resolution quality = getQualityForNumber(qualityNumber);
          final String downloadUrl = videoElementToUrl(vidoeElement, baseUrl.get());
          try {
            downloadUrls.put(quality, new URL(downloadUrl));
          } catch (final MalformedURLException malformedURLException) {
            LOG.error("A download URL is defect.", malformedURLException);
            aCrawler.printMessage(ServerMessages.DEBUG_INVALID_URL, aCrawler.getSender().getName(),
                downloadUrl);
          }
        }
      }
    }
    return downloadUrls;
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
    String url = aVideoElement.getAsJsonObject().get(ELEMENT_STREAM).getAsString();
    if (url.matches(URL_PREFIX_PATTERN + URL_PATTERN)) {
      url = url.replaceFirst(URL_PREFIX_PATTERN, aBaseUrl);
    }
    return url;
  }

}
