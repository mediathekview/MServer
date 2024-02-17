package de.mediathekview.mserver.crawler.orfon.json;

import com.google.gson.*;

import de.mediathekview.mserver.base.utils.JsonUtils;
import de.mediathekview.mserver.crawler.basic.PagedElementListDTO;
import de.mediathekview.mserver.crawler.orfon.OrfOnBreadCrumsUrlDTO;

import java.lang.reflect.Type;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class OrfOnHistoryDeserializer implements JsonDeserializer<PagedElementListDTO<OrfOnBreadCrumsUrlDTO>> {
  private static final Logger LOG = LogManager.getLogger(OrfOnHistoryDeserializer.class);
  private String[] TAG_NEXT_PAGE = {};
  private String[] TAG_ITEM_ARRAY_TOP = {"history_highlights"};
  private String[] TAG_ITEM_TITLE = {"title"};
  private String[] TAG_ITEM_ARRAY_BUTTOM = {"history_items"};
  private String[] TAG_TARGET_URL = {"_links", "children", "href"};
  
  @Override
  public PagedElementListDTO<OrfOnBreadCrumsUrlDTO> deserialize(
      final JsonElement jsonElement, final Type typeOfT, final JsonDeserializationContext context)
      throws JsonParseException {
    //
    PagedElementListDTO<OrfOnBreadCrumsUrlDTO> page = new PagedElementListDTO<>();
    page.setNextPage(JsonUtils.getElementValueAsString(jsonElement, TAG_NEXT_PAGE));
    //
    Optional<JsonElement> itemArrayTop = JsonUtils.getElement(jsonElement, TAG_ITEM_ARRAY_TOP);
    if (itemArrayTop.isPresent() && itemArrayTop.get().isJsonArray()) {
      page.addElements(parseSection(itemArrayTop.get().getAsJsonArray()).getElements());
    }
    //
    Optional<JsonElement> itemArrayButtom = JsonUtils.getElement(jsonElement, TAG_ITEM_ARRAY_BUTTOM);
    if (itemArrayButtom.isPresent() && itemArrayButtom.get().isJsonArray()) {
      page.addElements(parseSection(itemArrayButtom.get().getAsJsonArray()).getElements());
    }
    //
    return page;
  }
  
  public PagedElementListDTO<OrfOnBreadCrumsUrlDTO> parseSection(JsonArray itemArray) {
    PagedElementListDTO<OrfOnBreadCrumsUrlDTO> items = new PagedElementListDTO<>();
    for (JsonElement item : itemArray) {
      Optional<String> url = JsonUtils.getElementValueAsString(item, TAG_TARGET_URL);
      Optional<String> title = JsonUtils.getElementValueAsString(item, TAG_ITEM_TITLE);
      if (url.isPresent()) {
        items.addElement(new OrfOnBreadCrumsUrlDTO(
            title.orElse("EMPTY"),
            url.get()
        ));
      } else {
        LOG.debug("missing url for {}", title);
      }
      //LOG.debug("History Item {} {}", title, url);
    }
    return items;
  }
  
  
  
}
