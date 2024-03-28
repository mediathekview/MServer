package mServer.crawler.sender.orfon.json;

import com.google.gson.*;
import mServer.crawler.sender.base.JsonUtils;
import mServer.crawler.sender.base.PagedElementListDTO;
import mServer.crawler.sender.orfon.OrfOnBreadCrumsUrlDTO;

import java.lang.reflect.Type;
import java.util.Optional;

public class OrfOnEpisodesDeserializer implements JsonDeserializer<PagedElementListDTO<OrfOnBreadCrumsUrlDTO>> {
  private static final String[] TAG_NEXT_PAGE = {"_links", "next", "href"};
  private static final String[] TAG_ITEMS = {"_embedded", "items"};
  private static final String TAG_EPISODE_ID = "id";
  private static final String[] TAG_EPISODE_LINK = { "_links", "self", "href"};
  
  @Override
  public PagedElementListDTO<OrfOnBreadCrumsUrlDTO> deserialize(
      final JsonElement jsonElement, final Type typeOfT, final JsonDeserializationContext context)
      throws JsonParseException {
    JsonObject jsonPage = jsonElement.getAsJsonObject();
    //
    PagedElementListDTO<OrfOnBreadCrumsUrlDTO> page = new PagedElementListDTO<>();
    page.setNextPage(JsonUtils.getElementValueAsString(jsonElement, TAG_NEXT_PAGE));
    //
    final Optional<JsonElement> items = JsonUtils.getElement(jsonPage, TAG_ITEMS);
    if (items.isPresent() && items.get().isJsonArray()) {
      for (JsonElement item : items.get().getAsJsonArray()) {
        Optional<String> episodeId = JsonUtils.getElementValueAsString(item, TAG_EPISODE_ID);
        Optional<String> episodeLink = JsonUtils.getElementValueAsString(item, TAG_EPISODE_LINK);
        episodeLink.ifPresent( link -> page.addElement(new OrfOnBreadCrumsUrlDTO(episodeId.orElse("EMPTY"), link)));
      }
    }
    return page;
  }
  
}
