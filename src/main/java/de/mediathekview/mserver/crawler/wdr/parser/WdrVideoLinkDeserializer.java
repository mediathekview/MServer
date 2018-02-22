package de.mediathekview.mserver.crawler.wdr.parser;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import java.lang.reflect.Type;
import java.util.Optional;

public class WdrVideoLinkDeserializer implements JsonDeserializer<Optional<CrawlerUrlDTO>> {

  private static final String JSON_ELEMENT_MEDIAOBJ = "mediaObj";
  private static final String JSON_ATTRIBUTE_URL = "url";
  
  @Override
  public Optional<CrawlerUrlDTO> deserialize(JsonElement aJsonElement, Type aType, JsonDeserializationContext aContext) {
    final JsonObject jsonObject = aJsonElement.getAsJsonObject();
    if (jsonObject.has(JSON_ELEMENT_MEDIAOBJ)) {
      final JsonElement mediaObjElement = jsonObject.get(JSON_ELEMENT_MEDIAOBJ);
      if (mediaObjElement != null && mediaObjElement.getAsJsonObject().has(JSON_ATTRIBUTE_URL)) {
        final String urlJs = mediaObjElement.getAsJsonObject().get(JSON_ATTRIBUTE_URL).getAsString();
        return Optional.of(new CrawlerUrlDTO(urlJs));
      }
    }
    
    return Optional.empty();
  }  
}
