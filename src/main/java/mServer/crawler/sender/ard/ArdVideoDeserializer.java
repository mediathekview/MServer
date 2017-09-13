package mServer.crawler.sender.ard;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.lang.reflect.Type;
import mServer.crawler.sender.newsearch.Qualities;

public class ArdVideoDeserializer implements JsonDeserializer<ArdVideoDTO> {
    
    private static final String TEXT_START_HTTP = "http";
    private static final String JSON_OBJECT_MEDIAARRAY = "_mediaArray";
    private static final String JSON_OBJECT_MEDIASTREAMARRAY = "_mediaStreamArray";
    private static final String JSON_ELEMENT_QUALITY = "_quality";
    private static final String JSON_ELEMENT_STREAM = "_stream";
    
    @Override
    public ArdVideoDTO deserialize(JsonElement aJsonElement, Type aType, JsonDeserializationContext aContext) throws JsonParseException {
        ArdVideoDTO dto = new ArdVideoDTO();
        
        if (aJsonElement.isJsonObject()) {
            JsonObject jsonObject = aJsonElement.getAsJsonObject();
            if (jsonObject.has(JSON_OBJECT_MEDIAARRAY)) {
                JsonArray mediaArray = jsonObject.get(JSON_OBJECT_MEDIAARRAY).getAsJsonArray();
                mediaArray.forEach(mediaArrayItem -> {
                    JsonObject item = mediaArrayItem.getAsJsonObject();
                    JsonArray streamArray = item.get(JSON_OBJECT_MEDIASTREAMARRAY).getAsJsonArray();
                    streamArray.forEach(streamItem -> {
                        deserializeMediaStreamArrayItem(streamItem.getAsJsonObject(), dto);
                    });
                });
            }
        }
        
        return dto;
    }    
    
    private static void deserializeMediaStreamArrayItem(JsonObject stream, ArdVideoDTO dto) {
        String qualityValue = stream.get(JSON_ELEMENT_QUALITY).getAsString();
        String url;

        JsonElement streamElement = stream.get(JSON_ELEMENT_STREAM);
        if(streamElement.isJsonArray()) {
            url = streamElement.getAsJsonArray().get(0).getAsString();
        } else {
            url = streamElement.getAsString();
        }

        Qualities quality = convertQuality(qualityValue);
        if (quality != null) {
            if (dto.getUrl(quality) == null) {
                dto.addVideo(quality, addMissingHttpPrefixIfNecessary(url));
            }
        }        
    }
    
    private static String addMissingHttpPrefixIfNecessary(String aUrl) {
        if(aUrl.startsWith("//")) {
            aUrl = TEXT_START_HTTP + ":" + aUrl;
        }

        return aUrl;
    }

    private static Qualities convertQuality(String quality) {
        switch(quality) {
            case "1":
                return Qualities.SMALL;
            case "2":
                return Qualities.NORMAL;
            case "3":
                return Qualities.HD;
        }
        return null;
    }
}
