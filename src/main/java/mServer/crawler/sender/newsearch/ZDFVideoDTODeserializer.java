package mServer.crawler.sender.newsearch;

import com.google.gson.*;
import java.lang.reflect.Type;

/**
 * A JSON deserializer to gather the needed information for a {@link VideoDTO}.
 */
public class ZDFVideoDTODeserializer implements JsonDeserializer<VideoDTO> {

    private static final String JSON_ELEMENT_BRAND = "http://zdf.de/rels/brand";
    private static final String JSON_ELEMENT_TITLE = "title";
    
    @Override
    public VideoDTO deserialize(final JsonElement aJsonElement, final Type aTypeOfT, final JsonDeserializationContext aJsonDeserializationContext) throws JsonParseException {
        VideoDTO dto = new VideoDTO();

        JsonObject object = aJsonElement.getAsJsonObject();
        JsonObject brand = object.getAsJsonObject(JSON_ELEMENT_BRAND);
        if(brand != null) {
            JsonElement topic = brand.get(JSON_ELEMENT_TITLE);
            dto.setTopic(topic.getAsString());
        }       
        
        return dto;
    }
}
