package de.mediathekview.mserver.crawler.srf.parser;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import de.mediathekview.mserver.base.utils.JsonUtils;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.basic.PagedElementListDTO;
import de.mediathekview.mserver.crawler.srf.SrfConstants;
import java.lang.reflect.Type;
import java.util.Optional;
import java.util.Set;

public class SrfTopicDeserializer implements JsonDeserializer<PagedElementListDTO<CrawlerUrlDTO>> {

  private static final String ELEMENT_DATA = "data";
  private static final String ATTRIBUTE_ID = "id";
  private static final String ATTRIBUTE_NEXT = "next";

  @Override
  public PagedElementListDTO<CrawlerUrlDTO> deserialize(
      JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) {
    final PagedElementListDTO<CrawlerUrlDTO> results = new PagedElementListDTO<>();

    if (!jsonElement.getAsJsonObject().has(ELEMENT_DATA)
        || !jsonElement.getAsJsonObject().get(ELEMENT_DATA).getAsJsonObject().has(ELEMENT_DATA)
        || !jsonElement.getAsJsonObject().get(ELEMENT_DATA).getAsJsonObject().get(ELEMENT_DATA).isJsonArray()) {
      return results;
    }

    final JsonObject dataObject = jsonElement.getAsJsonObject().get(ELEMENT_DATA).getAsJsonObject();

    results.setNextPage(JsonUtils.getAttributeAsString(dataObject, ATTRIBUTE_NEXT));

    final JsonArray data = dataObject.getAsJsonArray(ELEMENT_DATA);
    data.forEach(
        entry -> {
          final Optional<String> id =
              JsonUtils.getAttributeAsString(entry.getAsJsonObject(), ATTRIBUTE_ID);

          id.ifPresent(s -> results.addElement(
              new CrawlerUrlDTO(String.format(SrfConstants.SHOW_DETAIL_PAGE_URL, s))));
        });

    return results;
  }
}
