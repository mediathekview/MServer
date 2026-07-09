package mServer.crawler.sender.ard.json;

import com.google.gson.*;
import mServer.crawler.sender.ard.ArdFilmInfoDto;
import mServer.crawler.sender.ard.ArdTopicInfoDto;

import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

public class ArdTopicCompilationDeserializer extends ArdTeasersDeserializer
        implements JsonDeserializer<ArdTopicInfoDto> {

  private static final String ELEMENT_WIDGETS = "widgets";
  private static final String ELEMENT_TEASERS = "teasers";
  private static final String ELEMENT_PAGE_NUMBER = "pageNumber";
  private static final String ELEMENT_TOTAL_ELEMENTS = "totalElements";
  private static final String ELEMENT_PAGE_SIZE = "pageSize";
  private static final String ELEMENT_PAGINATION = "pagination";

  @Override
  public ArdTopicInfoDto deserialize(
          final JsonElement showPageElement, final Type type, final JsonDeserializationContext context) {
    final Set<ArdFilmInfoDto> results = new HashSet<>();
    final ArdTopicInfoDto ardTopicInfoDto = new ArdTopicInfoDto(results);

    final JsonObject showPageObject = showPageElement.getAsJsonObject();
    if (showPageObject.has(ELEMENT_WIDGETS)) {
      final JsonArray widgets = showPageObject.get(ELEMENT_WIDGETS).getAsJsonArray();
      widgets.forEach(widget -> {
        if (widget.getAsJsonObject().has(ELEMENT_TEASERS)) {
          final JsonArray teasers = widget.getAsJsonObject().get(ELEMENT_TEASERS).getAsJsonArray();
          results.addAll(parseTeasers(teasers));
        }
      });
    } else if (showPageObject.getAsJsonObject().has(ELEMENT_TEASERS)) {
      final JsonArray teasers = showPageObject.getAsJsonObject().get(ELEMENT_TEASERS).getAsJsonArray();
      results.addAll(parseTeasers(teasers));
    }

    final JsonElement paginationElement = findPaginationElement(showPageObject);
    final int pageNumber = getChildElementAsIntOrNullIfNotExist(paginationElement, ELEMENT_PAGE_NUMBER);
    final int totalElements = getChildElementAsIntOrNullIfNotExist(paginationElement, ELEMENT_TOTAL_ELEMENTS);
    ardTopicInfoDto.setTotalElements(totalElements);
    final int pageSize = getChildElementAsIntOrNullIfNotExist(paginationElement, ELEMENT_PAGE_SIZE);
    ardTopicInfoDto.setPageSize(pageSize);
    ardTopicInfoDto.setMaxSubPageNumber(pageSize == 0 ? 0 :
            (totalElements + pageSize - 1) / pageSize);
    ardTopicInfoDto.setPageNumber(pageNumber);

    return ardTopicInfoDto;
  }

  private JsonElement findPaginationElement(JsonObject showPageObject) {
    if (showPageObject.has(ELEMENT_WIDGETS)) {
      final JsonElement widgetElement = showPageObject.get(ELEMENT_WIDGETS);
      if (widgetElement.isJsonArray()) {
        final JsonArray widgetArray = widgetElement.getAsJsonArray();
        if (!widgetArray.isEmpty()) {
          return widgetArray.get(0).getAsJsonObject().get(ELEMENT_PAGINATION);
        }
      }
    } else if (showPageObject.has(ELEMENT_PAGINATION)) {
      return showPageObject.get(ELEMENT_PAGINATION);
    }
    return null;
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
}
