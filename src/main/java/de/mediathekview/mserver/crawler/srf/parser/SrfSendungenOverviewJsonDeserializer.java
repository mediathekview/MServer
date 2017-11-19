package de.mediathekview.mserver.crawler.srf.parser;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SrfSendungenOverviewJsonDeserializer implements JsonDeserializer<Set<CrawlerUrlDTO>>{
  
  private static final String ELEMENT_TEASERLIST = "showTeaserList";
  private static final String ELEMENT_URL = "absoluteOverviewUrl";

  private static final Logger LOG = LogManager.getLogger(SrfSendungenOverviewJsonDeserializer.class);
  
  @Override
  public Set<CrawlerUrlDTO> deserialize(JsonElement aJsonElement, Type aType, JsonDeserializationContext aJdc) throws JsonParseException {
    final Set<CrawlerUrlDTO> results = new HashSet<>();
    
    if (!aJsonElement.isJsonArray()) {
      LOG.error("invalid json elment structure");
    } else {
      JsonArray array = aJsonElement.getAsJsonArray();
      array.forEach(letterElement -> {
        results.addAll(parseLetter(letterElement.getAsJsonObject()));
      });
    }
    
    return results;
  }  
  
  private Set<CrawlerUrlDTO> parseLetter(JsonObject letterObject) {
    final Set<CrawlerUrlDTO> results = new HashSet<>();
    
    if (letterObject.has(ELEMENT_TEASERLIST)) {
      letterObject.get(ELEMENT_TEASERLIST).getAsJsonArray().forEach((JsonElement entry) -> {
        if (!entry.isJsonNull()) {
          Optional<CrawlerUrlDTO> urlDTO = parseEntry(entry.getAsJsonObject());

          if (urlDTO.isPresent()) {
            results.add(urlDTO.get());
          }
        }
      });
    }
    
    return results;
  }
  
  private Optional<CrawlerUrlDTO> parseEntry(JsonObject entryObject) {
    if (entryObject.has(ELEMENT_URL)) {
      return Optional.of(new CrawlerUrlDTO(entryObject.get(ELEMENT_URL).getAsString()));
    }
    
    return Optional.empty();
  }
}
