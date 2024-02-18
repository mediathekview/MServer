package de.mediathekview.mserver.crawler.orfon.json;

import com.google.gson.*;

import de.mediathekview.mserver.base.utils.JsonUtils;
import de.mediathekview.mserver.crawler.basic.PagedElementListDTO;
import de.mediathekview.mserver.crawler.orfon.OrfOnVideoInfoDTO;

import java.lang.reflect.Type;
import java.util.Optional;

public class OrfOnEpisodesDeserializer implements JsonDeserializer<PagedElementListDTO<OrfOnVideoInfoDTO>> {
  private static final String[] TAG_NEXT_PAGE = {"_links", "next", "href"};
  private static final String[] TAG_ITEMS = {"_embedded", "items"};
  private static final OrfOnEpisodeDeserializer itemDeserializer = new OrfOnEpisodeDeserializer();
  
  @Override
  public PagedElementListDTO<OrfOnVideoInfoDTO> deserialize(
      final JsonElement jsonElement, final Type typeOfT, final JsonDeserializationContext context)
      throws JsonParseException {
    JsonObject jsonPage = jsonElement.getAsJsonObject();
    //
    PagedElementListDTO<OrfOnVideoInfoDTO> page = new PagedElementListDTO<OrfOnVideoInfoDTO>();
    page.setNextPage(JsonUtils.getElementValueAsString(jsonElement, TAG_NEXT_PAGE));
    //
    final Optional<JsonElement> items = JsonUtils.getElement(jsonPage, TAG_ITEMS);
    if (items.isPresent() && items.get().isJsonArray()) {
      for (JsonElement item : items.get().getAsJsonArray()) {
        page.addElement(itemDeserializer.deserialize(item, null, null));
      }
    }
    return page;
  }
  
}
