package mServer.crawler.sender.ard.json;

import com.google.gson.*;
import mServer.crawler.sender.ard.ArdFilmInfoDto;
import mServer.crawler.sender.ard.ArdTopicInfoDto;
import mServer.crawler.sender.base.JsonUtils;

import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class ArdTopicPageDeserializer extends ArdTeasersDeserializer
        implements JsonDeserializer<ArdTopicInfoDto> {

  private static final String ELEMENT_ID = "id";
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
    if (showPageObject.has(ELEMENT_TEASERS)) {
      final JsonArray teasers = showPageObject.get(ELEMENT_TEASERS).getAsJsonArray();
      results.addAll(parseTeasers(teasers));
    }
    final Optional<String> id = JsonUtils.getAttributeAsString(showPageObject, ELEMENT_ID);
    id.ifPresent(ardTopicInfoDto::setId);

    final JsonElement paginationElement = showPageObject.get(ELEMENT_PAGINATION);
    ardTopicInfoDto.setSubPageNumber(
            getChildElementAsIntOrNullIfNotExist(paginationElement, ELEMENT_PAGE_NUMBER));
    final int totalElements = getChildElementAsIntOrNullIfNotExist(paginationElement, ELEMENT_TOTAL_ELEMENTS);
    final int pageSize = getChildElementAsIntOrNullIfNotExist(paginationElement, ELEMENT_PAGE_SIZE);
    ardTopicInfoDto.setMaxSubPageNumber(pageSize == 0 ? 0 :
            (totalElements + pageSize - 1) / pageSize);

    return ardTopicInfoDto;
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
