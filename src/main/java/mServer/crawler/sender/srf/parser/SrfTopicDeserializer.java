package mServer.crawler.sender.srf.parser;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.lang.reflect.Type;
import java.util.Optional;
import mServer.crawler.sender.base.CrawlerUrlDTO;
import mServer.crawler.sender.base.JsonUtils;
import mServer.crawler.sender.base.PagedElementListDTO;
import mServer.crawler.sender.srf.SrfConstants;

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
            || !jsonElement
                    .getAsJsonObject()
                    .get(ELEMENT_DATA)
                    .getAsJsonObject()
                    .get(ELEMENT_DATA)
                    .isJsonArray()) {
      return results;
    }

    final JsonObject dataObject = jsonElement.getAsJsonObject().get(ELEMENT_DATA).getAsJsonObject();

    results.setNextPage(parseNextPage(dataObject));

    final JsonArray data = dataObject.getAsJsonArray(ELEMENT_DATA);
    data.forEach(
            entry -> {
              final Optional<String> id
              = JsonUtils.getAttributeAsString(entry.getAsJsonObject(), ATTRIBUTE_ID);

              id.ifPresent(
                      s
                      -> results.addElement(
                              new CrawlerUrlDTO((SrfConstants.SHOW_DETAIL_PAGE_URL).formatted(s))));
            });

    return results;
  }

  private Optional<String> parseNextPage(final JsonObject dataObject) {
    Optional<String> next = JsonUtils.getAttributeAsString(dataObject, ATTRIBUTE_NEXT);
    // ignore empty string value of next
    if (next.isPresent() && !next.get().isEmpty()) {
      return next;
    }

    return Optional.empty();
  }
}
