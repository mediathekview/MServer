package mServer.crawler.sender.arte;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import java.lang.reflect.Type;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Deserialisiert Ergebnisse der Anfrage den Filmen einer Kategorie.
 * Beispiel-URL: http://www.arte.tv/guide/api/api/videos/de/subcategory/ACC?page=1
 */
public class ArteCategoryFilmListDeserializer implements JsonDeserializer<ArteCategoryFilmsDTO> {
    
    private static final String JSON_ELEMENT_VIDEOS = "videos";
    private static final String JSON_ELEMENT_URL = "url";
    
    private static final Logger LOG = LogManager.getLogger(ArteCategoryFilmListDeserializer.class);
    
    @Override
    public ArteCategoryFilmsDTO deserialize(JsonElement aJsonElement, Type aType, JsonDeserializationContext aContext) throws JsonParseException {
        ArteCategoryFilmsDTO dto = new ArteCategoryFilmsDTO();
        
        for (JsonElement jsonElement : aJsonElement.getAsJsonObject().get(JSON_ELEMENT_VIDEOS).getAsJsonArray())
        {
            String url = jsonElement.getAsJsonObject().get(JSON_ELEMENT_URL).getAsString();
            String programId = parseUrl(url);
            if(programId != null) {
                dto.addProgramId(programId);
            }
        }
        
        return dto;
    }    
    
    /**
     * Ermittelt aus der Url /{language}/video/{programId}/{text} die ProgramId eines Films
     * @param aUrl
     * @return 
     */
    private static String parseUrl(String aUrl) {
        String[] urlParts = aUrl.split("/");
        if(urlParts.length == 5) {
            return urlParts[3];
        }
        
        LOG.error("URL contains invalid parts: " + aUrl);
        return null;
    }
}

