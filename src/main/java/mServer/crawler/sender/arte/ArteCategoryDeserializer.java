package mServer.crawler.sender.arte;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.lang.reflect.Type;

/**
 *
 */
public class ArteCategoryDeserializer implements JsonDeserializer<ArteCategoryDTO> {

    private static final String OBJECT_ZONES = "zones";
    private static final String OBJECT_LINK = "link";
    private static final String ELEMENT_PAGE = "page";
    private static final String ELEMENT_TITLE = "title";
    private static final String ELEMENT_TYPE = "type";
    
    private static final String RELEVANT_TYPE = "category";
    
    @Override
    public ArteCategoryDTO deserialize(JsonElement aJsonElement, Type aType, JsonDeserializationContext aContext) throws JsonParseException {
        
        JsonObject jsonObject = aJsonElement.getAsJsonObject();
        String title = jsonObject.get(ELEMENT_TITLE).getAsString();
        
        ArteCategoryDTO dto = new ArteCategoryDTO(title);

        deserializeCategories(dto, jsonObject);
        
        return dto;
    }
    
    private void deserializeCategories(ArteCategoryDTO dto, JsonObject jsonObject) {
        if(jsonObject.has(OBJECT_ZONES)) {
            JsonArray zones = jsonObject.get(OBJECT_ZONES).getAsJsonArray();

            for (JsonElement zone : zones) {
                JsonObject zoneObject = zone.getAsJsonObject();
                if(zoneObject.has(OBJECT_LINK)) {
                    String type = zoneObject.get(ELEMENT_TYPE).getAsString();
                    if(type.equals(RELEVANT_TYPE)) {
                        JsonObject linkObject = zoneObject.get(OBJECT_LINK).getAsJsonObject();
                        String page = linkObject.get(ELEMENT_PAGE).getAsString();
                        dto.addSubCategory(page);
                    }
                }
            }
        }
    }
}
