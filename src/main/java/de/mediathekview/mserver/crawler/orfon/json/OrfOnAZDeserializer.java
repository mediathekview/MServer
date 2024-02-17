package de.mediathekview.mserver.crawler.orfon.json;

import com.google.gson.*;

import de.mediathekview.mserver.base.utils.JsonUtils;
import de.mediathekview.mserver.crawler.basic.PagedElementListDTO;
import de.mediathekview.mserver.crawler.orfon.OrfOnBreadCrumsUrlDTO;

import java.lang.reflect.Type;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class OrfOnAZDeserializer implements JsonDeserializer<PagedElementListDTO<OrfOnBreadCrumsUrlDTO>> {
  private static final Logger LOG = LogManager.getLogger(OrfOnAZDeserializer.class);
  private static final String[] TAG_NEXT_PAGE = {"_links", "next", "href"};
  private static final String[] TAG_ITEMS = {"_embedded", "items"};
  private static final String TAG_ITEM_ID = "id";
  private static final String[] TAG_ITEM_EPISODES = {"_links", "episodes", "href"};
  
  @Override
  public PagedElementListDTO<OrfOnBreadCrumsUrlDTO> deserialize(
      final JsonElement jsonElement, final Type typeOfT, final JsonDeserializationContext context)
      throws JsonParseException {
    JsonObject jsonPage = jsonElement.getAsJsonObject();
    //
    PagedElementListDTO<OrfOnBreadCrumsUrlDTO> page = new PagedElementListDTO<>();
    page.setNextPage(JsonUtils.getElementValueAsString(jsonElement, TAG_NEXT_PAGE));
    //
    if (jsonPage.has(TAG_ITEMS[0]) && jsonPage.get(TAG_ITEMS[0]).isJsonObject() &&
       jsonPage.get(TAG_ITEMS[0]).getAsJsonObject().has(TAG_ITEMS[1]) &&
       jsonPage.get(TAG_ITEMS[0]).getAsJsonObject().get(TAG_ITEMS[1]).isJsonArray()) {
      for (JsonElement topic : jsonPage.get(TAG_ITEMS[0]).getAsJsonObject().get(TAG_ITEMS[1]).getAsJsonArray()) {
        Optional<String> id = JsonUtils.getElementValueAsString(topic, TAG_ITEM_ID);
        Optional<String> url = JsonUtils.getElementValueAsString(topic, TAG_ITEM_EPISODES);
        if (id.isPresent() && url.isPresent()) {
          page.addElement(new OrfOnBreadCrumsUrlDTO(id.get(), url.get()));
        } else {
          LOG.debug("No episodes found in item {}", id);
        }
      }
    }
    return page;
  }
}
