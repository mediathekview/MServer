package mServer.crawler.sender.arte;

import java.lang.reflect.Type;

import mServer.crawler.sender.newsearch.Qualities;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

public class ArteVideoDeserializer implements JsonDeserializer<ArteVideoDTO> {
    @Override
    public ArteVideoDTO deserialize(JsonElement aJsonElement, Type aType, JsonDeserializationContext aContext) throws JsonParseException {
        ArteVideoDTO arteVideoDTO = new ArteVideoDTO();
        
        JsonObject vsrJsonObject = aJsonElement.getAsJsonObject().get("videoJsonPlayer").getAsJsonObject().get("VSR").getAsJsonObject();
        if(vsrJsonObject.has("HTTPS_HQ_1"))//small 640*360
        {
          arteVideoDTO.addVideo(Qualities.SMALL, vsrJsonObject.get("HTTPS_HQ_1").getAsJsonObject().get("url").getAsString());
        }
        if(vsrJsonObject.has("HTTPS_EQ_1"))//norm 720*406
        {
            arteVideoDTO.addVideo(Qualities.SMALL, vsrJsonObject.get("HTTPS_EQ_1").getAsJsonObject().get("url").getAsString());
        }
        if(vsrJsonObject.has("HTTPS_SQ_1"))//hd 1280*720
        {
            arteVideoDTO.addVideo(Qualities.SMALL, vsrJsonObject.get("HTTPS_SQ_1").getAsJsonObject().get("url").getAsString());
        }
        
        
        return arteVideoDTO;
    }
}
