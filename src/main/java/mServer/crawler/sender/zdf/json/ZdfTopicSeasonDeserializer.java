package mServer.crawler.sender.zdf.json;

import com.google.gson.*;
import mServer.crawler.sender.base.JsonUtils;
import mServer.crawler.sender.base.PagedElementListDTO;
import mServer.crawler.sender.zdf.ZdfFilmDto;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Type;
import java.util.Optional;

public class ZdfTopicSeasonDeserializer extends ZdfTopicBaseClass
    implements JsonDeserializer<PagedElementListDTO<ZdfFilmDto>> {

  private static final Logger LOG = LogManager.getLogger(ZdfTopicSeasonDeserializer.class);

  @Override
  public PagedElementListDTO<ZdfFilmDto> deserialize(
      JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext)
      throws JsonParseException {
    final PagedElementListDTO<ZdfFilmDto> films = new PagedElementListDTO<>();

    JsonObject rootNode = jsonElement.getAsJsonObject();
    final JsonObject data = rootNode.getAsJsonObject("data");
    if (data.isJsonNull()) {
      LOG.error("ZdfTopicSeasonDeserializer: No data found in response");
      return films;
    }

    if (data.has("smartCollectionByCanonical")) {
      final Optional<JsonElement> optionalNodesElement =
          JsonUtils.getElement(data, "smartCollectionByCanonical", "seasons", "nodes");
      if (optionalNodesElement.isPresent() && optionalNodesElement.get().isJsonArray()) {
        final JsonArray seasonNodes = optionalNodesElement.get().getAsJsonArray();
        for (JsonElement element : seasonNodes) {
          final JsonObject episodes = element.getAsJsonObject().getAsJsonObject("episodes");
          final JsonArray episodeNodes = episodes.getAsJsonArray("nodes");
          addFilms(films, episodeNodes);
          films.setNextPage(parseNextPage(episodes.getAsJsonObject("pageInfo")));
        }
      }
    } else if (data.has("metaCollectionContent")) {
      final JsonObject metaCollectionContent = data.getAsJsonObject("metaCollectionContent");
      final JsonArray collectionNodes = metaCollectionContent.getAsJsonArray("smartCollections");
      addFilms(films, collectionNodes);
      films.setNextPage(parseNextPage(metaCollectionContent.getAsJsonObject("pageInfo")));
    } else {
      LOG.error("ZdfTopicSeasonDeserializer: No valid entry nodes found");
    }
    return films;
  }

  private void addFilms(PagedElementListDTO<ZdfFilmDto> films, JsonArray episodeNodes) {
    for (JsonElement episode : episodeNodes) {
      films.addElements(deserializeMovie(episode));
    }
  }

  private Optional<String> parseNextPage(JsonObject pageInfo) {
    if (!pageInfo.isJsonNull()) {
      final Optional<String> hasNextPage = JsonUtils.getAttributeAsString(pageInfo, "hasNextPage");
      if (hasNextPage.isPresent() && hasNextPage.get().equals("true")) {
        return JsonUtils.getAttributeAsString(pageInfo, "endCursor");
      }
    }
    return Optional.empty();
  }
}
