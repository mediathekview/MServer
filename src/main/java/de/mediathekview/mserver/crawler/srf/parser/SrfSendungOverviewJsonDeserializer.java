package de.mediathekview.mserver.crawler.srf.parser;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.lang.reflect.Type;
import java.util.Optional;

public class SrfSendungOverviewJsonDeserializer implements JsonDeserializer<SrfSendungOverviewDTO> {

  private static final String BASE_URL = "https://www.srf.ch";
  private static final String ELEMENT_NEXT_PAGE = "nextPageUrl";
  private static final String ELEMENT_EPISODES = "episodes";
  private static final String ELEMENT_ID = "id";
  
  @Override
  public SrfSendungOverviewDTO deserialize(JsonElement aJsonElement, Type aType, JsonDeserializationContext aJdc) throws JsonParseException {
    
    SrfSendungOverviewDTO dto = new SrfSendungOverviewDTO();
    try {
    JsonObject object = aJsonElement.getAsJsonObject();
    parseNextPage(dto, object);
    parseEpisodes(dto, object);
    } catch(Exception e) {
      if(e != null) {
        
      }
    }
    return dto;
  }
  
  private void parseNextPage(SrfSendungOverviewDTO aDto, JsonObject aJsonObject) {
    if (aJsonObject.has(ELEMENT_NEXT_PAGE)) {
      JsonElement nextPageElement = aJsonObject.get(ELEMENT_NEXT_PAGE);
              
      if (!nextPageElement.isJsonNull()) {
        aDto.setNextPageId(Optional.of(BASE_URL + nextPageElement.getAsString()));
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
    return String.format("https://il.srgssr.ch/integrationlayer/2.0/srf/mediaComposition/video/%s.json", aId);
  }
  
}
