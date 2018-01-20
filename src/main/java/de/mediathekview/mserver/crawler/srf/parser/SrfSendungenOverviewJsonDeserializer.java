package de.mediathekview.mserver.crawler.srf.parser;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.srf.SrfConstants;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SrfSendungenOverviewJsonDeserializer implements JsonDeserializer<Set<CrawlerUrlDTO>>{
  
  private static final String ELEMENT_TEASERLIST = "showTeaserList";
  private static final String ELEMENT_ID = "id";

  private static final int FILMS_PER_PAGE = 100;
  
  private static final Logger LOG = LogManager.getLogger(SrfSendungenOverviewJsonDeserializer.class);
  
  private final String monthYear;
  
  public SrfSendungenOverviewJsonDeserializer() {
    LocalDateTime today = LocalDateTime.now();
    monthYear = today.getMonthValue() + "-" + today.getYear();
 }
  
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
          Optional<String> urlDTO = parseEntry(entry.getAsJsonObject());

          if (urlDTO.isPresent()) {
            results.add(new CrawlerUrlDTO(urlDTO.get()));
          }
        }
      });
    }
    
    return results;
  }
  
  private Optional<String> parseEntry(JsonObject entryObject) {
    if (entryObject.has(ELEMENT_ID)) {
      String id = entryObject.get(ELEMENT_ID).getAsString();

      String url = String.format(SrfConstants.SHOW_OVERVIEW_PAGE_URL, id, FILMS_PER_PAGE, monthYear); 
      
      return Optional.of(url);
    }
    
    return Optional.empty();
  }
}
