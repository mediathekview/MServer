package de.mediathekview.mserver.crawler.ard.json;

import java.lang.reflect.Type;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import de.mediathekview.mlib.daten.Resolution;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;

/**
 * Converts json with basic video from
 * http://www.ardmediathek.de/play/media/[documentId]?devicetype=pc&features=flash to a map of
 * {@link Resolution} with corresponding urls.
 */
public class ArdVideoInfoJsonDeserializer implements JsonDeserializer<ArdVideoInfoDTO> {
  private static final String ELEMENT_SUBTITLE_URL = "_subtitleUrl";
  private final AbstractCrawler crawler;

  public ArdVideoInfoJsonDeserializer(final AbstractCrawler aCrawler) {
    crawler = aCrawler;
  }

  @Override
  public ArdVideoInfoDTO deserialize(final JsonElement aJsonElement, final Type aType,
      final JsonDeserializationContext aJsonDeserializationContext) {
    final ArdVideoInfoDTO videoInfo = new ArdVideoInfoDTO();
    final JsonElement subtitleElement = aJsonElement.getAsJsonObject().get(ELEMENT_SUBTITLE_URL);
    if (subtitleElement != null) {
      videoInfo.setSubtitleUrl(subtitleElement.getAsString());
    }

    ArdMediaArrayToDownloadUrlsConverter.toDownloadUrls(aJsonElement, crawler).entrySet()
        .forEach(e -> videoInfo.put(e.getKey(), e.getValue().toString()));
    return videoInfo;
  }

}
