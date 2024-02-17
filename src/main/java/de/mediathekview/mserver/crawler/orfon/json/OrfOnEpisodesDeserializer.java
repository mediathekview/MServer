package de.mediathekview.mserver.crawler.orfon.json;

import com.google.gson.*;

import de.mediathekview.mserver.base.utils.JsonUtils;
import de.mediathekview.mserver.crawler.basic.PagedElementListDTO;
import de.mediathekview.mserver.crawler.orfon.OrfOnVideoInfoDTO;

import java.lang.reflect.Type;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class OrfOnEpisodesDeserializer implements JsonDeserializer<PagedElementListDTO<OrfOnVideoInfoDTO>> {
  private static final Logger LOG = LogManager.getLogger(OrfOnEpisodesDeserializer.class);
  private static final String[] TAG_NEXT_PAGE = {"_links", "next", "href"};
  private static final String[] TAG_ITEMS = {"_embedded", "items"};
  
  @Override
  public PagedElementListDTO<OrfOnVideoInfoDTO> deserialize(
      final JsonElement jsonElement, final Type typeOfT, final JsonDeserializationContext context)
      throws JsonParseException {
    JsonObject jsonPage = jsonElement.getAsJsonObject();
    //
    PagedElementListDTO<OrfOnVideoInfoDTO> page = new PagedElementListDTO<OrfOnVideoInfoDTO>();
    page.setNextPage(JsonUtils.getElementValueAsString(jsonElement, TAG_NEXT_PAGE));
    //
    OrfOnEpisodeDeserializer itemDeserializer = new OrfOnEpisodeDeserializer();
    if (jsonPage.has(TAG_ITEMS[0]) && jsonPage.get(TAG_ITEMS[0]).isJsonObject() &&
        jsonPage.get(TAG_ITEMS[0]).getAsJsonObject().has(TAG_ITEMS[1]) &&
        jsonPage.get(TAG_ITEMS[0]).getAsJsonObject().get(TAG_ITEMS[1]).isJsonArray()) {
       for (JsonElement item : jsonPage.get(TAG_ITEMS[0]).getAsJsonObject().get(TAG_ITEMS[1]).getAsJsonArray()) {
         page.addElement(itemDeserializer.deserialize(item, null, null));
       }
     }
    return page;
  }
  
}
