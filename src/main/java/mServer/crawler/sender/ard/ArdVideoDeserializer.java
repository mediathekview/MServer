package mServer.crawler.sender.ard;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import mServer.crawler.sender.newsearch.Qualities;

public class ArdVideoDeserializer implements JsonDeserializer<ArdVideoDTO> {
    
    private static final String TEXT_START_HTTP = "http";
    private static final String JSON_OBJECT_MEDIAARRAY = "_mediaArray";
    private static final String JSON_OBJECT_MEDIASTREAMARRAY = "_mediaStreamArray";
    private static final String JSON_ELEMENT_QUALITY = "_quality";
    private static final String JSON_ELEMENT_STREAM = "_stream";
    private static final String JSON_ELEMENT_WIDTH = "_width";
    private static final String JSON_ELEMENT_HEIGHT = "_height";
    
    @Override
    public ArdVideoDTO deserialize(JsonElement aJsonElement, Type aType, JsonDeserializationContext aContext) throws JsonParseException {
        ArdVideoDTO dto = new ArdVideoDTO();
        
        if (aJsonElement.isJsonObject()) {
            JsonObject jsonObject = aJsonElement.getAsJsonObject();
            if (jsonObject.has(JSON_OBJECT_MEDIAARRAY)) {
                Map<Qualities, UrlWidthHeightDTO> urlMap = new HashMap<>();
                
                JsonArray mediaArray = jsonObject.get(JSON_OBJECT_MEDIAARRAY).getAsJsonArray();
                mediaArray.forEach(mediaArrayItem -> {
                    JsonObject item = mediaArrayItem.getAsJsonObject();
                    JsonArray streamArray = item.get(JSON_OBJECT_MEDIASTREAMARRAY).getAsJsonArray();
                    streamArray.forEach(streamItem -> {
                        deserializeMediaStreamArrayItem(streamItem.getAsJsonObject(), urlMap);
                    });
                });
                
                // URLs setzen
                urlMap.forEach((key, value) -> {
                    dto.addVideo(key, value.url);
                });
            }
        }
        
        return dto;
    }    
    
    private void deserializeMediaStreamArrayItem(JsonObject stream, Map<Qualities, UrlWidthHeightDTO> urlMap) {
        
        String qualityValue = stream.get(JSON_ELEMENT_QUALITY).getAsString();

        UrlWidthHeightDTO urlInfos = deserializeUrlInfos(stream);

        Qualities quality = convertQuality(qualityValue, urlInfos);
        // nur relevante Qualitäten berücksichtigen und nur http-Links (manchmal sind "mp3:"-Links enthalten)
        if (quality != null && urlInfos.url.startsWith(TEXT_START_HTTP)) {
            if (urlMap.containsKey(quality)) {
                UrlWidthHeightDTO actualUrlInfos = urlMap.get(quality);
                
                // prüfen, ob die Auflösung besser ist, als die bisher für die Qualität hinterlegte
                if (urlInfos.width > 0 && urlInfos.width > actualUrlInfos.width) {
                   urlMap.put(quality, urlInfos);
                } else if (quality == Qualities.NORMAL) {
                    // bei normaler Auflösung ist aus irgendeinem Grund der letzte
                    // Eintrag immer der beste!
                    urlMap.put(quality, urlInfos);
                }
            } else {
                urlMap.put(quality, urlInfos);
            }
        }        
    }
    
    private UrlWidthHeightDTO deserializeUrlInfos(JsonObject stream) {
        UrlWidthHeightDTO dto = new UrlWidthHeightDTO();
        
        // Url ermitteln
        // Wenn es ein Array ist, den letzten Eintrag nehmen, der hat im Zweifel
        // die bessere Auflösung!
        JsonElement streamElement = stream.get(JSON_ELEMENT_STREAM);
        if(streamElement.isJsonArray()) {
            JsonArray streamArray = streamElement.getAsJsonArray();
            dto.url = streamArray.get(streamArray.size()-1).getAsString();
        } else {
            dto.url = streamElement.getAsString();
        }
        dto.url = addMissingHttpPrefixIfNecessary(dto.url);
        
        // Auflösung ermitteln
        if( stream.has(JSON_ELEMENT_WIDTH)) {
            dto.width = stream.get(JSON_ELEMENT_WIDTH).getAsInt();
        }
        if( stream.has(JSON_ELEMENT_HEIGHT)) {
            dto.height = stream.get(JSON_ELEMENT_HEIGHT).getAsInt();
        }        
        
        return dto;
    }
    
    private static String addMissingHttpPrefixIfNecessary(String aUrl) {
        if(aUrl.startsWith("//")) {
            aUrl = TEXT_START_HTTP + ":" + aUrl;
        }

        return aUrl;
    }

    private static Qualities convertQuality(String quality, UrlWidthHeightDTO urlInfos) {
        switch(quality) {
            case "1":
                return Qualities.SMALL;
            case "2":
                return Qualities.NORMAL;
            case "3":
                // Beste Qualität, aber nicht immer ist die Auflösung auch HD
                // Die Prüfung auf den Dateinamen der URL filtert noch zusätzlich
                // Filme heraus, die eine Auflösung von 960x... haben
                if ((urlInfos.width == 0 && urlInfos.height == 0 && !urlInfos.url.substring(urlInfos.url.lastIndexOf("/") + 1).startsWith("960"))
                    || (urlInfos.width >= 1280 && urlInfos.height >= 720)) {
                    return Qualities.HD;
                } else {
                    return Qualities.NORMAL;
                }
        }
        return null;
    }
    
    // Hilfsklasse für Zwischenspeichern von URL und zugehörigen Auflösungsinfos
    private class UrlWidthHeightDTO {
        public String url = "";
        public int width;
        public int height;
    }
}
