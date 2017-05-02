package mServer.crawler.sender.arte;

import java.lang.reflect.Type;

import mServer.crawler.sender.newsearch.Qualities;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

public class ArteVideoDeserializer implements JsonDeserializer<ArteVideoDTO> {
    private static final String JSON_OBJECT_KEY_PLAYER = "videoJsonPlayer";
    private static final String JSON_ELEMENT_KEY_VIDEO_DURATION_SECONDS="videoDurationSeconds";
    private static final String JSON_OBJECT_KEY_VSR = "VSR";
    
    @Override
    public ArteVideoDTO deserialize(JsonElement aJsonElement, Type aType, JsonDeserializationContext aContext) throws JsonParseException {
        ArteVideoDTO arteVideoDTO = new ArteVideoDTO();
        if(aJsonElement.isJsonObject() && 
            aJsonElement.getAsJsonObject().has(JSON_OBJECT_KEY_PLAYER) && 
            aJsonElement.getAsJsonObject().get(JSON_OBJECT_KEY_PLAYER).isJsonObject() &&
            aJsonElement.getAsJsonObject().get(JSON_OBJECT_KEY_PLAYER).getAsJsonObject().has(JSON_OBJECT_KEY_VSR) &&
            aJsonElement.getAsJsonObject().get(JSON_OBJECT_KEY_PLAYER).getAsJsonObject().get(JSON_OBJECT_KEY_VSR).isJsonObject()
        )
        {   JsonObject playerObject = aJsonElement.getAsJsonObject().get(JSON_OBJECT_KEY_PLAYER).getAsJsonObject();
            JsonObject vsrJsonObject = playerObject.get(JSON_OBJECT_KEY_VSR).getAsJsonObject();
            if(vsrJsonObject.has("HTTPS_HQ_1"))//small 640*360
            {
              arteVideoDTO.addVideo(Qualities.SMALL, getVideoUrl(vsrJsonObject, "HTTPS_HQ_1"));
            } else if(vsrJsonObject.has("HTTPS_MP4_HQ_1")) 
            {
                arteVideoDTO.addVideo(Qualities.SMALL, getVideoUrl(vsrJsonObject, "HTTPS_MP4_HQ_1"));
            }
            
            if(vsrJsonObject.has("HTTPS_EQ_1"))//norm 720*406
            {
                arteVideoDTO.addVideo(Qualities.NORMAL, getVideoUrl(vsrJsonObject, "HTTPS_EQ_1"));
            } else if(vsrJsonObject.has("HTTPS_MP4_EQ_1")) 
            {
                arteVideoDTO.addVideo(Qualities.NORMAL, getVideoUrl(vsrJsonObject, "HTTPS_MP4_EQ_1"));
            }
            
            if(vsrJsonObject.has("HTTPS_SQ_1"))//hd 1280*720
            {
                arteVideoDTO.addVideo(Qualities.HD, getVideoUrl(vsrJsonObject, "HTTPS_SQ_1"));
            } else if(vsrJsonObject.has("HTTPS_MP4_SQ_1")) 
            {
                arteVideoDTO.addVideo(Qualities.HD, getVideoUrl(vsrJsonObject, "HTTPS_MP4_SQ_1"));
            }
            
            if(!playerObject.get(JSON_ELEMENT_KEY_VIDEO_DURATION_SECONDS).isJsonNull())
            {
                arteVideoDTO.setDurationInSeconds(playerObject.get(JSON_ELEMENT_KEY_VIDEO_DURATION_SECONDS).getAsLong());
            }
    }

        
        return arteVideoDTO;
    }
    
    private static String getVideoUrl(JsonObject vsrJsonObject, String qualityTag) {
        return vsrJsonObject.get(qualityTag).getAsJsonObject().get("url").getAsString();   
    }
}
