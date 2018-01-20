package de.mediathekview.mserver.crawler.srf.parser;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import de.mediathekview.mserver.crawler.srf.SrfConstants;
import java.lang.reflect.Type;
import java.util.Optional;
import org.apache.logging.log4j.LogManager;

public class SrfSendungOverviewJsonDeserializer implements JsonDeserializer<Optional<SrfSendungOverviewDTO>> {

  private static final org.apache.logging.log4j.Logger LOG = LogManager.getLogger(SrfSendungOverviewJsonDeserializer.class);
  
  private static final String ELEMENT_NEXT_PAGE = "nextPageUrl";
  private static final String ELEMENT_EPISODES = "episodes";
  private static final String ELEMENT_ID = "id";
  
  private final String baseUrl;
  
  public SrfSendungOverviewJsonDeserializer(String aBaseUrl) {
    baseUrl = aBaseUrl;
  }
  
  @Override
  public Optional<SrfSendungOverviewDTO> deserialize(JsonElement aJsonElement, Type aType, JsonDeserializationContext aJdc) throws JsonParseException {
    
    try {
      SrfSendungOverviewDTO dto = new SrfSendungOverviewDTO();

      JsonObject object = aJsonElement.getAsJsonObject();
      parseNextPage(dto, object);
      parseEpisodes(dto, object);
      return Optional.of(dto);
    } catch(Exception e) {
      LOG.error(e);
    }
    return Optional.empty();
  }
  
  private void parseNextPage(SrfSendungOverviewDTO aDto, JsonObject aJsonObject) {
    if (aJsonObject.has(ELEMENT_NEXT_PAGE)) {
      JsonElement nextPageElement = aJsonObject.get(ELEMENT_NEXT_PAGE);
              
      if (!nextPageElement.isJsonNull()) {
        aDto.setNextPageId(Optional.of(baseUrl + nextPageElement.getAsString()));
      }
    }
  }
  
  private void parseEpisodes(SrfSendungOverviewDTO aDto, JsonObject aJsonObject) {
    if (aJsonObject.has(ELEMENT_EPISODES)) {
      JsonElement episodesElement = aJsonObject.get(ELEMENT_EPISODES);
      if (episodesElement.isJsonArray()) {
        episodesElement.getAsJsonArray().forEach(episode -> {
          parseEpisode(aDto, episode);
        });
      }
    }
  }
  
  private void parseEpisode(SrfSendungOverviewDTO aDto, JsonElement aEpisode) {
    if (!aEpisode.isJsonNull()) {
      JsonObject episodeObject = aEpisode.getAsJsonObject();
      if (episodeObject.has(ELEMENT_ID)) {
        aDto.addUrl(getUrl(episodeObject.get(ELEMENT_ID).getAsString()));
      }
    }    
  }
  
  private String getUrl(String aId) {
    return String.format(SrfConstants.SHOW_DETAIL_PAGE_URL, aId);
  }
  
}
