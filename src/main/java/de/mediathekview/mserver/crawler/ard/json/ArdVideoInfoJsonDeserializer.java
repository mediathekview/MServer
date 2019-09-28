package de.mediathekview.mserver.crawler.ard.json;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import de.mediathekview.mlib.daten.Resolution;
import de.mediathekview.mlib.tool.MVHttpClient;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.M3U8Dto;
import de.mediathekview.mserver.crawler.basic.M3U8Parser;
import okhttp3.Request;
import okhttp3.ResponseBody;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Converts json with basic video from {@literal
 * http://www.ardmediathek.de/play/media/[documentId]?devicetype=pc&features=flash} to a map of
 * {@link Resolution} with corresponding urls.
 */
public class ArdVideoInfoJsonDeserializer implements JsonDeserializer<ArdVideoInfoDto> {

  private static final String ELEMENT_SUBTITLE_URL = "_subtitleUrl";

  private static final Logger LOG = LogManager.getLogger(ArdVideoInfoJsonDeserializer.class);

  private static final Request.Builder REQUEST_BUILDER = new Request.Builder();

  private final AbstractCrawler crawler;

  public ArdVideoInfoJsonDeserializer(final AbstractCrawler aCrawler) {
    crawler = aCrawler;
  }

  @Override
  public ArdVideoInfoDto deserialize(
      final JsonElement aJsonElement,
      final Type aType,
      final JsonDeserializationContext aJsonDeserializationContext) {
    final ArdVideoInfoDto videoInfo = new ArdVideoInfoDto();
    final JsonElement subtitleElement = aJsonElement.getAsJsonObject().get(ELEMENT_SUBTITLE_URL);
    if (subtitleElement != null && !subtitleElement.isJsonNull()) {
      videoInfo.setSubtitleUrl(subtitleElement.getAsString());
    }

    final Map<Resolution, URL> resolutionUrlMap =
        ArdMediaArrayToDownloadUrlsConverter.toDownloadUrls(aJsonElement, crawler);

    // if map contains only a m3u8 url, load the m3u8 file and use the containing
    // urls
    if (resolutionUrlMap.size() == 1
        && resolutionUrlMap.containsKey(Resolution.NORMAL)
        && resolutionUrlMap.get(Resolution.NORMAL).getFile().endsWith("m3u8")) {

      loadM3U8(resolutionUrlMap);
    }

    resolutionUrlMap.forEach((key, value) -> videoInfo.put(key, value.toString()));
    return videoInfo;
  }

  /**
   * reads an url.
   *
   * @param aUrl the url
   * @return the content of the url
   */
  private static Optional<String> readContent(final URL aUrl) {
    final Request request = REQUEST_BUILDER.url(aUrl).build();
    try (final okhttp3.Response response =
            MVHttpClient.getInstance().getHttpClient().newCall(request).execute();
        final ResponseBody body = response.body()) {
      if (response.isSuccessful()) {
        return Optional.of(body.string());
      } else {
        LOG.error(
            String.format(
                "ArdVideoInfoJsonDeserializer: Request '%s' failed: %s", aUrl, response.code()));
      }
    } catch (final IOException ex) {
      LOG.error("ArdVideoInfoJsonDeserializer: ", ex);
    }

    return Optional.empty();
  }

  private void loadM3U8(final Map<Resolution, URL> resolutionUrlMap) {
    final Optional<String> m3u8Content = readContent(resolutionUrlMap.get(Resolution.NORMAL));
    resolutionUrlMap.clear();
    if (m3u8Content.isPresent()) {

      final M3U8Parser parser = new M3U8Parser();
      final List<M3U8Dto> m3u8Data = parser.parse(m3u8Content.get());

      m3u8Data.forEach(
          entry -> {
            final Optional<Resolution> resolution = entry.getResolution();
            if (resolution.isPresent()) {
              try {
                resolutionUrlMap.put(resolution.get(), new URL(entry.getUrl()));
              } catch (final MalformedURLException e) {
                LOG.error("ArdVideoInfoJsonDeserializer: invalid url " + entry.getUrl(), e);
              }
            }
          });
    }
  }
}
