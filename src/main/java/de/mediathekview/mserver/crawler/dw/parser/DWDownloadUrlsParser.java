package de.mediathekview.mserver.crawler.dw.parser;

import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.EnumMap;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import de.mediathekview.mlib.daten.Resolution;
import de.mediathekview.mserver.base.utils.JsonUtils;

public class DWDownloadUrlsParser implements JsonDeserializer<Map<Resolution, URL>> {
  private static final Logger LOG = LogManager.getLogger(DWDownloadUrlsParser.class);
  private static final String ELEMENT_LABEL = "label";
  private static final String ELEMENT_FILE = "file";

  @Override
  public Map<Resolution, URL> deserialize(final JsonElement aJsonElement, final Type aType,
      final JsonDeserializationContext aContext) throws JsonParseException {
    final Map<Resolution, URL> urls = new EnumMap<>(Resolution.class);

    for (final JsonElement element : aJsonElement.getAsJsonArray()) {
      if (JsonUtils.hasElements(element, ELEMENT_FILE, ELEMENT_LABEL)) {
        final JsonObject elementObj = element.getAsJsonObject();
        final int width = elementObj.get(ELEMENT_LABEL).getAsInt();
        final String url = elementObj.get(ELEMENT_FILE).getAsString();
        try {
          urls.put(Resolution.getResolutionFromWidth(width), new URL(url));
        } catch (final MalformedURLException malformedURLException) {
          LOG.error(String.format("A found download URL \"%s\" isn't valid.", url));
        }
      }
    }
    return urls;
  }
}
