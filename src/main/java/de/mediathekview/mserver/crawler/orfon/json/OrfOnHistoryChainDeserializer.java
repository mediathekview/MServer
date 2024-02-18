package de.mediathekview.mserver.crawler.orfon.json;

import com.google.gson.*;

import de.mediathekview.mserver.base.utils.JsonUtils;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.basic.PagedElementListDTO;
import de.mediathekview.mserver.crawler.orfon.OrfOnVideoInfoDTO;

import java.lang.reflect.Type;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class OrfOnHistoryChainDeserializer implements JsonDeserializer<PagedElementListDTO<CrawlerUrlDTO>> {
  private static final Logger LOG = LogManager.getLogger(OrfOnHistoryChainDeserializer.class);
  private final String[] TAG_NEXT_PAGE;
  private final String[] TAG_ITEM_ARRAY;
  private final String[] TAG_TARGET_URL;
  
  
  
  public OrfOnHistoryChainDeserializer(String[] TAG_NEXT_PAGE,String[] TAG_ITEM_ARRAY, String[] TAG_TARGET_URL ) {
    this.TAG_NEXT_PAGE = TAG_NEXT_PAGE;
    this.TAG_ITEM_ARRAY = TAG_ITEM_ARRAY;
    this.TAG_TARGET_URL = TAG_TARGET_URL;
  }
  
  @Override
  public PagedElementListDTO<CrawlerUrlDTO> deserialize(
      final JsonElement jsonElement, final Type typeOfT, final JsonDeserializationContext context)
      throws JsonParseException {
    //
    PagedElementListDTO<CrawlerUrlDTO> page = new PagedElementListDTO<>();
    page.setNextPage(JsonUtils.getElementValueAsString(jsonElement, TAG_NEXT_PAGE));
    //
    final Optional<JsonElement> itemArray = JsonUtils.getElement(jsonElement, TAG_ITEM_ARRAY);
    if (itemArray.isPresent() && itemArray.get().isJsonArray()) {
      for (JsonElement item : itemArray.get().getAsJsonArray()) {
        final Optional<String> url = JsonUtils.getElementValueAsString(item, TAG_TARGET_URL);
        if (url.isPresent()) {
          page.addElement(new CrawlerUrlDTO(JsonUtils.getElementValueAsString(item, TAG_TARGET_URL).get()));
        }
      }
    }
    //
    return page;
  }
  
}
