package mServer.crawler.sender.dw;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.lang.reflect.Type;
import mServer.crawler.sender.newsearch.Qualities;

public class DwVideoDeserializer implements JsonDeserializer<DwVideoDTO> {
    
    private static final String JSON_ELEMENT_FILE = "file";
    
    @Override
    public DwVideoDTO deserialize(JsonElement aJsonElement, Type aType, JsonDeserializationContext aContext) throws JsonParseException {
        DwVideoDTO dto = new DwVideoDTO();
        
        if (aJsonElement.isJsonArray()) {
            JsonArray jsonArray = aJsonElement.getAsJsonArray();
            jsonArray.forEach(itemElement -> {
                JsonObject item = itemElement.getAsJsonObject();
                String file = item.get(JSON_ELEMENT_FILE).getAsString();
                
                Qualities quality = getQuality(file);
                if (quality != null) {
                    dto.addVideo(quality, file);
                }
            });
        }
        
        return dto;
    }    
    
    private static Qualities getQuality(String file) {
        if (file.endsWith("sor.mp4")) {
            return Qualities.NORMAL;
        }
        if (file.endsWith("avc.mp4")) {
            return Qualities.HD;
        }
        return null;
    }
}