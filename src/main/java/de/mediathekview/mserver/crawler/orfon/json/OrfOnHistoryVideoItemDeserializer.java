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
  private static final String[] TAG_NEXT_PAGE = { "next" };
  private static final String[] TAG_ITEM_ARRAY = { "_items" };
  private static final String[] TAG_ITEM_TITLE = {"title"};
  private static final String[] TAG_TARGET_URL = {"_links", "self", "href"};
  private static final String[] TAG_TARGET_URL_EPISODE = {"_links", "episode", "href"};
  
  protected final Logger LOG = LogManager.getLogger(this.getClass());
  
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
        final Optional<String> urlSelf = JsonUtils.getElementValueAsString(item, TAG_TARGET_URL);
        final Optional<String> urlEpisode = JsonUtils.getElementValueAsString(item, TAG_TARGET_URL_EPISODE);
        final Optional<String> title = JsonUtils.getElementValueAsString(item, TAG_ITEM_TITLE);
        // self should be an episode but in some cases a segment - only in this cases we have an additional episode element
        if (urlSelf.isPresent() && !urlSelf.get().contains("/segment/")) {
          page.addElement(new OrfOnBreadCrumsUrlDTO(
              title.orElse("MISSING TITLE"),
              urlSelf.get()
          ));
        } else if (urlEpisode.isPresent()) {
          page.addElement(new OrfOnBreadCrumsUrlDTO(
              title.orElse("MISSING TITLE"),
              urlEpisode.get()
          ));
        }  
      }
    }
    //
    return page;
  }
  

  
  
}
