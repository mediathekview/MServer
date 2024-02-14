package mServer.crawler.sender.ard.json;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import mServer.crawler.sender.ard.ArdConstants;
import mServer.crawler.sender.ard.PaginationUrlDto;
import mServer.crawler.sender.base.CrawlerUrlDTO;
import mServer.crawler.sender.base.JsonUtils;

import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class ArdTopicsLetterDeserializer implements JsonDeserializer<PaginationUrlDto> {

  private static final String ELEMENT_TEASERS = "teasers";
  private static final String ELEMENT_LINKS = "links";
  private static final String ELEMENT_TARGET = "target";
  private static final String ELEMENT_PAGE_NUMBER = "pageNumber";
  private static final String ELEMENT_TOTAL_ELEMENTS = "totalElements";
  private static final String ELEMENT_PAGE_SIZE = "pageSize";
  private static final String ELEMENT_PAGINATION = "pagination";

  private static final String ATTRIBUTE_ID = "id";

  @Override
  public PaginationUrlDto deserialize(
          final JsonElement jsonElement, final Type type, final JsonDeserializationContext context) {
    final PaginationUrlDto results = new PaginationUrlDto();

    if (!jsonElement.getAsJsonObject().has(ELEMENT_TEASERS)
            || !jsonElement.getAsJsonObject().get(ELEMENT_TEASERS).isJsonArray()
            || jsonElement.getAsJsonObject().getAsJsonArray(ELEMENT_TEASERS).isEmpty()) {
      return results;
    }

    jsonElement.getAsJsonObject().getAsJsonArray(ELEMENT_TEASERS).forEach(teaser -> results.addAll(parseTeaser(teaser.getAsJsonObject())));

    final JsonElement paginationElement = jsonElement.getAsJsonObject().get(ELEMENT_PAGINATION);
    results.setActualPage(getChildElementAsIntOrNullIfNotExist(paginationElement, ELEMENT_PAGE_NUMBER));
    final int totalElements = getChildElementAsIntOrNullIfNotExist(paginationElement, ELEMENT_TOTAL_ELEMENTS);
    final int pageSize = getChildElementAsIntOrNullIfNotExist(paginationElement, ELEMENT_PAGE_SIZE);
    int maxPageSize = pageSize == 0 ? 0 :
            (totalElements+pageSize-1)/pageSize;
    results.setMaxPages(maxPageSize);

    return results;
  }

  private int getChildElementAsIntOrNullIfNotExist(
          final JsonElement parentElement, final String childElementName) {
    if (parentElement == null || parentElement.isJsonNull()) {
      return 0;
    }
    return getJsonElementAsIntOrNullIfNotExist(
            parentElement.getAsJsonObject().get(childElementName));
  }

  private int getJsonElementAsIntOrNullIfNotExist(final JsonElement element) {
    if (element.isJsonNull()) {
      return 0;
    }
    return element.getAsInt();
  }

  private Set<CrawlerUrlDTO> parseTeaser(final JsonObject teaserObject) {
    final Set<CrawlerUrlDTO> results = new HashSet<>();

    final Optional<String> id;

    if (JsonUtils.checkTreePath(teaserObject, ELEMENT_LINKS, ELEMENT_TARGET)) {
      final JsonObject targetObject =
              teaserObject.get(ELEMENT_LINKS).getAsJsonObject().get(ELEMENT_TARGET).getAsJsonObject();
      id = JsonUtils.getAttributeAsString(targetObject, ATTRIBUTE_ID);
    } else {
      id = JsonUtils.getAttributeAsString(teaserObject, ATTRIBUTE_ID);
    }

    id.ifPresent(
            nonNullId ->
                    results.add(
                            new CrawlerUrlDTO(
                                    String.format(
                                            ArdConstants.TOPIC_URL, nonNullId, ArdConstants.TOPIC_PAGE_SIZE))));

    return results;
  }
}
