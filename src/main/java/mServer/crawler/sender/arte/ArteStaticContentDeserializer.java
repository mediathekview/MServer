package mServer.crawler.sender.arte;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.lang.reflect.Type;

/**
 * Deserializes static content information
 */
public class ArteStaticContentDeserializer implements JsonDeserializer<ArteInfoDTO> {

    private static final String OBJECT_CATEGORY_LINKS = "categoryLinks";
    private static final String ELEMENT_CATEGORY_LABEL = "label";
    private static final String ELEMENT_CATEGORY_HREF = "href";
        
    @Override
    public ArteInfoDTO deserialize(JsonElement aJsonElement, Type aType, JsonDeserializationContext aContext) throws JsonParseException {
        
        ArteInfoDTO dto = new ArteInfoDTO();
        
        if (aJsonElement.isJsonObject() && aJsonElement.getAsJsonObject().has(OBJECT_CATEGORY_LINKS)) {
            JsonArray categoryLinks = aJsonElement.getAsJsonObject().get(OBJECT_CATEGORY_LINKS).getAsJsonArray();
            
            for (JsonElement element : categoryLinks) {
                JsonObject elementObject = element.getAsJsonObject();
                if (elementObject != null) {
                    String name = elementObject.get(ELEMENT_CATEGORY_LABEL).getAsString();
                    String url = elementObject.get(ELEMENT_CATEGORY_HREF).getAsString();
                    
                    dto.addCategory(name, url);
                }
            }
        }
        
        return dto;        
    }
    
}
