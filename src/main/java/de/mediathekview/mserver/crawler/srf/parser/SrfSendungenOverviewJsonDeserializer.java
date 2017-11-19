package de.mediathekview.mserver.crawler.srf.parser;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SrfSendungenOverviewJsonDeserializer implements JsonDeserializer<Set<String>>{
  
  private static final String ELEMENT_TEASERLIST = "showTeaserList";
  private static final String ELEMENT_ID = "id";

  private static final Logger LOG = LogManager.getLogger(SrfSendungenOverviewJsonDeserializer.class);
  
  @Override
  public Set<String> deserialize(JsonElement aJsonElement, Type aType, JsonDeserializationContext aJdc) throws JsonParseException {
    final Set<String> results = new HashSet<>();
    
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
  
  private Set<String> parseLetter(JsonObject letterObject) {
    final Set<String> results = new HashSet<>();
    
    if (letterObject.has(ELEMENT_TEASERLIST)) {
      letterObject.get(ELEMENT_TEASERLIST).getAsJsonArray().forEach((JsonElement entry) -> {
        if (!entry.isJsonNull()) {
          Optional<String> urlDTO = parseEntry(entry.getAsJsonObject());

          if (urlDTO.isPresent()) {
            results.add(urlDTO.get());
          }
        }
      });
    }
    
    return results;
  }
  
  private Optional<String> parseEntry(JsonObject entryObject) {
    if (entryObject.has(ELEMENT_ID)) {
      return Optional.of(entryObject.get(ELEMENT_ID).getAsString());
    }
    
    return Optional.empty();
  }
}
