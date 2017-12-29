package mServer.crawler.sender.ard;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
    
    private static String addMissingHttpPrefixIfNecessary(String aUrl) {
      if(aUrl.startsWith("//")) {
        return TEXT_START_HTTP + ":" + aUrl;
      }
      
      return aUrl;
    }
    
    @Override
    public ArdVideoDTO deserialize(JsonElement aJsonElement, Type aType, JsonDeserializationContext aContext) throws JsonParseException {
        ArdVideoDTO dto = new ArdVideoDTO();
        
        if (aJsonElement.isJsonObject()) {
            JsonObject jsonObject = aJsonElement.getAsJsonObject();
            if (jsonObject.has(JSON_OBJECT_MEDIAARRAY)) {
                Map<Qualities, String> urlMap = new HashMap<>();
                
                JsonArray mediaArray = jsonObject.get(JSON_OBJECT_MEDIAARRAY).getAsJsonArray();
                if (mediaArray.size() > 0) {
                  JsonElement mediaArrayItem = mediaArray.get(mediaArray.size() - 1);
                  JsonObject item = mediaArrayItem.getAsJsonObject();
                  JsonArray streamArray = item.get(JSON_OBJECT_MEDIASTREAMARRAY).getAsJsonArray();
                  deserializeMediaStreamArray(streamArray, urlMap);
                }
                
                // URLs setzen
                urlMap.forEach((key, value) -> {
                    dto.addVideo(key, value);
                });
            }
        }
        
        return dto;
    }    
    
    private void deserializeMediaStreamArray(JsonArray streamArray, Map<Qualities, String> urlMap) {
      List<QualityUrlWidthHeightDTO> qualities = new ArrayList<>();
      
      streamArray.forEach(streamItem -> {
        qualities.add(deserializeUrlInfos(streamItem.getAsJsonObject()));
      });
      
      convertQualities(qualities, urlMap);
    }
    
    private void convertQualities(List<QualityUrlWidthHeightDTO> qualities, Map<Qualities, String> urlMap) {
      // bei der besten Qualität anfangen
      qualities.sort((a, b) -> b.quality.compareTo(a.quality));
      
      for (int i = 0; i < qualities.size(); i++) {
        QualityUrlWidthHeightDTO qualityDto = qualities.get(i);
        if (qualityDto.urls.isEmpty()) {
          continue;
        }
        
        switch(qualityDto.quality){
          case "1":
            // für SMALL die erste URL nehmen, ist immer die bessere
            addUrl(urlMap, Qualities.SMALL, qualityDto.urls.get(0));
            break;
          case "2":
            // für NORMAL die letzte URL nehmen, ist immer die bessere
            addUrl(urlMap, Qualities.NORMAL, qualityDto.urls.get(qualityDto.urls.size()-1));
            break;
          case "3": 
            if (qualityDto.width >= 1280 && qualityDto.height >= 720) {
              addUrl(urlMap, Qualities.HD, getUrl(qualityDto, qualities.get(i+1)));
            }
            if (qualityDto.width == 0 && qualityDto.height == 0) {
              String url = getUrl(qualityDto, qualities.get(i+1));
              
              // Die Prüfung auf den Dateinamen der URL filtert noch zusätzlich
              // Filme heraus, die eine Auflösung von 960x... haben
              if (!url.substring(url.lastIndexOf('/') + 1).startsWith("960")) {
                addUrl(urlMap, Qualities.HD, url);
              } else {
                addUrl(urlMap, Qualities.NORMAL, url);
              }
            }
            break;
        }
      }
    }
    
    private void addUrl(Map<Qualities, String> urlMap, Qualities quality, String url) {
      if (url != null && !url.isEmpty() && !urlMap.containsKey(quality)) {
        urlMap.put(quality, url);
      }
    }
    
    private String getUrl(QualityUrlWidthHeightDTO quality, QualityUrlWidthHeightDTO lowerQuality) {
      for (int i = 0; i < quality.urls.size(); i++) {
        String url = quality.urls.get(i);
        
        if (!lowerQuality.urls.contains(url)) {
          return url;
        }
      }
      
      return "";
    }
 
    private QualityUrlWidthHeightDTO deserializeUrlInfos(JsonObject stream) {
        QualityUrlWidthHeightDTO dto = new QualityUrlWidthHeightDTO();
        
        dto.quality = stream.get(JSON_ELEMENT_QUALITY).getAsString();
        
        JsonElement streamElement = stream.get(JSON_ELEMENT_STREAM);
        if(streamElement.isJsonArray()) {
            JsonArray streamArray = streamElement.getAsJsonArray();
            streamArray.forEach(arrayElement -> {
              dto.urls.add(addMissingHttpPrefixIfNecessary(arrayElement.getAsString()));
            });
        } else {
            dto.urls.add(addMissingHttpPrefixIfNecessary(streamElement.getAsString()));
        }
        
        // Auflösung ermitteln
        if( stream.has(JSON_ELEMENT_WIDTH)) {
            dto.width = stream.get(JSON_ELEMENT_WIDTH).getAsInt();
        }
        if( stream.has(JSON_ELEMENT_HEIGHT)) {
            dto.height = stream.get(JSON_ELEMENT_HEIGHT).getAsInt();
        }        
        
        return dto;
    }
    

    // Hilfsklasse für Zwischenspeichern von URL und zugehörigen Auflösungsinfos
    private class QualityUrlWidthHeightDTO {
      public String quality = "";
        public List<String> urls = new ArrayList<>();
        public int width;
        public int height;
    }
}
