package mServer.crawler.sender.srf.parser;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import mServer.crawler.sender.srf.SrfConstants;
import java.lang.reflect.Type;
import java.util.Optional;
import mServer.crawler.sender.base.SendungOverviewDto;
import org.apache.logging.log4j.LogManager;

public class SrfSendungOverviewJsonDeserializer implements JsonDeserializer<Optional<SendungOverviewDto>> {

  private static final org.apache.logging.log4j.Logger LOG = LogManager.getLogger(SrfSendungOverviewJsonDeserializer.class);

  private static final String ELEMENT_NEXT_PAGE = "nextPageUrl";
  private static final String ELEMENT_EPISODES = "episodes";
  private static final String ELEMENT_ID = "id";

  private final String baseUrl;

  public SrfSendungOverviewJsonDeserializer(String aBaseUrl) {
    baseUrl = aBaseUrl;
  }

  @Override
  public Optional<SendungOverviewDto> deserialize(JsonElement aJsonElement, Type aType, JsonDeserializationContext aJdc) {

    try {
      SendungOverviewDto dto = new SendungOverviewDto();

      JsonObject object = aJsonElement.getAsJsonObject();
      parseNextPage(dto, object);
      parseEpisodes(dto, object);
      return Optional.of(dto);
    } catch (Exception e) {
      LOG.error(e);
    }
    return Optional.empty();
  }

  private void parseNextPage(SendungOverviewDto aDto, JsonObject aJsonObject) {
    if (aJsonObject.has(ELEMENT_NEXT_PAGE)) {
      JsonElement nextPageElement = aJsonObject.get(ELEMENT_NEXT_PAGE);

      if (!nextPageElement.isJsonNull()) {
        aDto.setNextPageId(Optional.of(baseUrl + nextPageElement.getAsString()));
      }
    }
  }

  private void parseEpisodes(SendungOverviewDto aDto, JsonObject aJsonObject) {
    if (aJsonObject.has(ELEMENT_EPISODES)) {
      JsonElement episodesElement = aJsonObject.get(ELEMENT_EPISODES);
      if (episodesElement.isJsonArray()) {
        episodesElement.getAsJsonArray().forEach(episode -> parseEpisode(aDto, episode));
      }
    }
  }

  private void parseEpisode(SendungOverviewDto aDto, JsonElement aEpisode) {
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
