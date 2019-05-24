package de.mediathekview.mserver.crawler.dw.parser;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.mediathekview.mlib.daten.FilmUrl;
import de.mediathekview.mlib.daten.Resolution;
import de.mediathekview.mserver.base.utils.JsonUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.util.EnumMap;
import java.util.Map;

public class DWDownloadUrlsParser implements JsonDeserializer<Map<Resolution, FilmUrl>> {

  private static final Logger LOG = LogManager.getLogger(DWDownloadUrlsParser.class);
  private static final String ELEMENT_LABEL = "label";
  private static final String ELEMENT_FILE = "file";

  @Override
  public Map<Resolution, FilmUrl> deserialize(
      final JsonElement aJsonElement, final Type aType, final JsonDeserializationContext aContext) {
    final Map<Resolution, FilmUrl> urls = new EnumMap<>(Resolution.class);

    for (final JsonElement element : aJsonElement.getAsJsonArray()) {
      if (JsonUtils.hasElements(element, ELEMENT_FILE, ELEMENT_LABEL)) {
        final JsonObject elementObj = element.getAsJsonObject();
        final int height = elementObj.get(ELEMENT_LABEL).getAsInt();
        final String url = elementObj.get(ELEMENT_FILE).getAsString();
        try {
          urls.put(getResolution(height), new FilmUrl(url));
        } catch (final MalformedURLException malformedUrlException) {
          LOG.error(String.format("A found download URL \"%s\" isn't valid.", url));
        }
      }
    }
    return urls;
  }

  private static Resolution getResolution(final int height) {
    if (height >= 720) {
      return Resolution.HD;
    }

    return Resolution.NORMAL;
  }
}
