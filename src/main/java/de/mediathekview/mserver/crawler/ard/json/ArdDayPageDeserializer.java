package de.mediathekview.mserver.crawler.ard.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.mediathekview.mserver.base.utils.JsonUtils;
import de.mediathekview.mserver.crawler.ard.ArdConstants;
import de.mediathekview.mserver.crawler.ard.ArdUrlBuilder;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class ArdDayPageDeserializer implements JsonDeserializer<Set<CrawlerUrlDTO>> {
  
  private static final String ELEMENT_DATA = "data";
  private static final String ELEMENT_GUIDE_PAGE = "guidePage";
  private static final String ELEMENT_WIDGETS = "widgets";
  private static final String ELEMENT_TEASERS = "teasers";
  private static final String ELEMENT_LINKS = "links";
  private static final String ELEMENT_TARGET = "target";
  
  private static final String ATTRIBUTE_ID = "id";
  
  @Override
  public Set<CrawlerUrlDTO> deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context) {
    Set<CrawlerUrlDTO> results = new HashSet<>();
    
    if (JsonUtils.checkTreePath(jsonElement, Optional.empty(), ELEMENT_DATA, ELEMENT_GUIDE_PAGE, ELEMENT_WIDGETS)) {
      JsonArray widgets = jsonElement.getAsJsonObject().get(ELEMENT_DATA).getAsJsonObject().get(ELEMENT_GUIDE_PAGE).getAsJsonObject().get(ELEMENT_WIDGETS).getAsJsonArray();
      for (JsonElement widgetElement: widgets) {
        JsonObject widgetObject = widgetElement.getAsJsonObject();
        if (widgetObject.has(ELEMENT_TEASERS)) {
          JsonArray teasers = widgetObject.get(ELEMENT_TEASERS).getAsJsonArray();
          for (JsonElement teaserElement: teasers) {
            JsonObject teaserObject = teaserElement.getAsJsonObject();
            Optional<String> id;
            
            if (JsonUtils.checkTreePath(teaserObject, Optional.empty(), ELEMENT_LINKS, ELEMENT_TARGET)) {
              JsonObject targetObject = teaserObject.get(ELEMENT_LINKS).getAsJsonObject().get(ELEMENT_TARGET).getAsJsonObject();
              id = JsonUtils.getAttributeAsString(targetObject, ATTRIBUTE_ID);
            } else {
              id = JsonUtils.getAttributeAsString(teaserObject, ATTRIBUTE_ID);
            }
             
            if (id.isPresent()) {
              final String url = new ArdUrlBuilder(ArdConstants.BASE_URL, ArdConstants.DEFAULT_CLIENT)
                      .addClipId(id.get(), ArdConstants.DEFAULT_DEVICE)
                      .addSavedQuery(ArdConstants.QUERY_FILM_VERSION, ArdConstants.QUERY_FILM_HASH)
                      .build();
                      
              results.add(new CrawlerUrlDTO(url));
            }
          }
        }
      }
    }
    
    return results;
  } 
}
