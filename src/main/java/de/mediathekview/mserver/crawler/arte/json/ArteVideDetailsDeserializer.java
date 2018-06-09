package de.mediathekview.mserver.crawler.arte.json;

import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import de.mediathekview.mlib.daten.Resolution;
import de.mediathekview.mserver.base.utils.JsonUtils;
import de.mediathekview.mserver.crawler.arte.tasks.ArteVideoDetailDTO;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;

public class ArteVideDetailsDeserializer implements JsonDeserializer<Optional<ArteVideoDetailDTO>> {
  private static final Logger LOG = LogManager.getLogger(ArteVideDetailsDeserializer.class);
  private static final String ELEMENT_URL = "url";
  private static final String ELEMENT_WIDTH = "width";
  private static final String ELEMENT_CREATION_DATE = "creationDate";
  private static final String ELEMENT_VIDEO_STREAMS = "videoStreams";
  private final AbstractCrawler crawler;

  public ArteVideDetailsDeserializer(final AbstractCrawler aCrawler) {
    crawler = aCrawler;
  }

  @Override
  public Optional<ArteVideoDetailDTO> deserialize(final JsonElement aJsonElement, final Type aType,
      final JsonDeserializationContext aJsonDeserializationContext) throws JsonParseException {
    final ArteVideoDetailDTO videoDetailDTO = new ArteVideoDetailDTO();
    if (JsonUtils.hasElements(aJsonElement, Optional.of(crawler), ELEMENT_VIDEO_STREAMS)) {
      final JsonObject baseObject = aJsonElement.getAsJsonObject();


      for (final JsonElement videoStream : baseObject.get(ELEMENT_VIDEO_STREAMS).getAsJsonArray()) {
        if (JsonUtils.hasElements(videoStream, ELEMENT_WIDTH, ELEMENT_URL)) {
          final JsonObject streamObject = videoStream.getAsJsonObject();
          final String urlText = streamObject.get(ELEMENT_URL).getAsString();
          try {
            videoDetailDTO.put(
                Resolution.getResolutionFromWidth(streamObject.get(ELEMENT_WIDTH).getAsInt()),
                new URL(urlText));
          } catch (final MalformedURLException malformedURLException) {
            LOG.debug(String.format("The URL \"%s\" isn't a valid URL.", urlText));
          }
          if (JsonUtils.hasElements(videoStream, ELEMENT_CREATION_DATE)) {
            videoDetailDTO.setCreationDate(
                Optional.of(streamObject.get(ELEMENT_CREATION_DATE).getAsString()));
          }
        }
      }

    }

    if (videoDetailDTO.getUrls().isEmpty()) {
      crawler.incrementAndGetErrorCount();
      crawler.updateProgress();
      LOG.debug("Found a ARTE video with no video streams, ignoring it.");
      return Optional.empty();
    }
    return Optional.of(videoDetailDTO);
  }

}
