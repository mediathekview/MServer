package de.mediathekview.mserver.crawler.ard.json;

import com.google.gson.*;
import de.mediathekview.mserver.crawler.ard.ArdFilmInfoDto;
import de.mediathekview.mserver.crawler.ard.ArdTopicInfoDto;

import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

public class ArdTopicPageDeserializer extends ArdTeasersDeserializer
    implements JsonDeserializer<ArdTopicInfoDto> {

  private static final String ELEMENT_TEASERS = "teasers";
  private static final String ELEMENT_PAGE_NUMBER = "pageNumber";
  private static final String ELEMENT_TOTAL_ELEMENTS = "totalElements";
  private static final String ELEMENT_PAGINATION = "pagination";

  @Override
  public ArdTopicInfoDto deserialize(
      JsonElement showPageElement, Type type, JsonDeserializationContext context) {
     Set<ArdFilmInfoDto> results = new HashSet<>();
    final ArdTopicInfoDto ardTopicInfoDto = new ArdTopicInfoDto(results);

    JsonObject showPageObject = showPageElement.getAsJsonObject();
    if (showPageObject.has(ELEMENT_TEASERS)) {
      JsonArray teasers = showPageObject.get(ELEMENT_TEASERS).getAsJsonArray();
      results.addAll(parseTeasers(teasers));
    }

    final JsonElement paginationElement = showPageObject.get(ELEMENT_PAGINATION);
    ardTopicInfoDto.setSubPageNumber(
            getChildElementAsIntOrNullIfNotExist(paginationElement, ELEMENT_PAGE_NUMBER));
    ardTopicInfoDto.setMaxSubPageNumber(
            getChildElementAsIntOrNullIfNotExist(paginationElement, ELEMENT_TOTAL_ELEMENTS));

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
