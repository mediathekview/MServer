package de.mediathekview.mserver.crawler.orfon.json;

import com.google.gson.*;

import de.mediathekview.mserver.base.utils.JsonUtils;
import de.mediathekview.mserver.crawler.basic.PagedElementListDTO;
import de.mediathekview.mserver.crawler.orfon.OrfOnBreadCrumsUrlDTO;
import de.mediathekview.mserver.crawler.orfon.OrfOnConstants;

import java.lang.reflect.Type;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class OrfOnHistoryChildrenDeserializer implements JsonDeserializer<PagedElementListDTO<OrfOnBreadCrumsUrlDTO>> {
  private static final Logger LOG = LogManager.getLogger(OrfOnHistoryChildrenDeserializer.class);
  private static final String[] TAG_NEXT_PAGE = { "next" };
  private static final String[] TAG_ITEM_ARRAY = { "_items" };
  private static final String[] TAG_ITEM_TITLE = {"title"};
  private static final String[] TAG_TARGET_URL = {"_links", "video_items", "href"};
  private static final String[] TAG_TARGET_URL2 = {"_links", "children", "href"};
  
  @Override
  public PagedElementListDTO<OrfOnBreadCrumsUrlDTO> deserialize(
      final JsonElement jsonElement, final Type typeOfT, final JsonDeserializationContext context)
      throws JsonParseException {
    //
    PagedElementListDTO<OrfOnBreadCrumsUrlDTO> page = new PagedElementListDTO<>();
    page.setNextPage(JsonUtils.getElementValueAsString(jsonElement, TAG_NEXT_PAGE));
    //
    Optional<JsonElement> itemArray = JsonUtils.getElement(jsonElement, TAG_ITEM_ARRAY);
    if (itemArray.isPresent() && itemArray.get().isJsonArray()) {
      for (JsonElement item : itemArray.get().getAsJsonArray()) {
        final Optional<String> videoItemUrl = JsonUtils.getElementValueAsString(item, TAG_TARGET_URL);
        final Optional<String> childrenUrl = JsonUtils.getElementValueAsString(item, TAG_TARGET_URL2);
        final Optional<String> title = JsonUtils.getElementValueAsString(item, TAG_ITEM_TITLE);
        if (videoItemUrl.isPresent()) {
          page.addElement(new OrfOnBreadCrumsUrlDTO(
              title.orElse("MISSING TITLE"),
              OrfOnConstants.createMaxLimmitUrl(videoItemUrl.get())
          ));
        } else if (childrenUrl.isPresent()) {
          page.addElement(new OrfOnBreadCrumsUrlDTO(
              title.orElse("MISSING TITLE"),
              OrfOnConstants.createMaxLimmitUrl(childrenUrl.get())
          ));
        } else {
          LOG.info("No video_items or children tag found {}",JsonUtils.getElementValueAsString(item, TAG_ITEM_TITLE) );
        }
      }
    }
    //
    return page;
  }

}
