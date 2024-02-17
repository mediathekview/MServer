package de.mediathekview.mserver.crawler.orfon.json;

import com.google.gson.*;

import de.mediathekview.mserver.base.utils.JsonUtils;
import de.mediathekview.mserver.crawler.basic.PagedElementListDTO;
import de.mediathekview.mserver.crawler.orfon.OrfOnBreadCrumsUrlDTO;

import java.lang.reflect.Type;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class OrfOnHistoryVideoItemDeserializer implements JsonDeserializer<PagedElementListDTO<OrfOnBreadCrumsUrlDTO>> {
  private static final Logger LOG = LogManager.getLogger(OrfOnHistoryVideoItemDeserializer.class);
  private String[] TAG_NEXT_PAGE = { "next" };
  private String[] TAG_ITEM_ARRAY = { "_items" };
  private String[] TAG_ITEM_TITLE = {"title"};
  private String[] TAG_TARGET_URL = {"_links", "self", "href"};
  
  @Override
  public PagedElementListDTO<OrfOnBreadCrumsUrlDTO> deserialize(
      final JsonElement jsonElement, final Type typeOfT, final JsonDeserializationContext context)
      throws JsonParseException {
    //
    PagedElementListDTO<OrfOnBreadCrumsUrlDTO> page = new PagedElementListDTO<>();
    page.setNextPage(JsonUtils.getElementValueAsString(jsonElement, TAG_NEXT_PAGE));
    //
    Optional<JsonElement> itemArrayTop = JsonUtils.getElement(jsonElement, TAG_ITEM_ARRAY);
    if (itemArrayTop.isPresent() && itemArrayTop.get().isJsonArray()) {
      for (JsonElement item : itemArrayTop.get().getAsJsonArray()) {
        Optional<String> url = JsonUtils.getElementValueAsString(item, TAG_TARGET_URL);
        if (url.isPresent()) {
          page.addElement(new OrfOnBreadCrumsUrlDTO(
              JsonUtils.getElementValueAsString(item, TAG_ITEM_TITLE).get(),
              JsonUtils.getElementValueAsString(item, TAG_TARGET_URL).get()
          ));
        }
      }
    }
    //
    return page;
  }
  
  
  
}
