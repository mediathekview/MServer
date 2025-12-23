package de.mediathekview.mserver.crawler.dw.parser;

import com.google.gson.*;
import de.mediathekview.mserver.base.utils.JsonUtils;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.basic.PagedElementListDTO;
import de.mediathekview.mserver.crawler.basic.TopicUrlDTO;

import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class DWSendungOverviewDeserializer
    implements JsonDeserializer<Optional<PagedElementListDTO<TopicUrlDTO>>> {

  private static final String ELEMENT_ITEMS = "items";
  private static final String ELEMENT_REFERENCE = "reference";
  private static final String ELEMENT_REFERENCE_URL = "url";
  private static final String ELEMENT_REFERENCE_TYPE = "type";
  private static final String ELEMENT_REFERENCE_ID = "id";
  
  private static final String ELEMENT_PAGINATION = "paginationInfo";
  private static final String ELEMENT_PAGINATION_NEXT = "nextPageUrl";
  

  private static Optional<String> parseNextUrl(final JsonObject contentObject) {
	if (!JsonUtils.checkTreePath(contentObject, null, ELEMENT_PAGINATION, ELEMENT_PAGINATION_NEXT)) {
		return Optional.empty();
	}
    final JsonElement paginationElement = contentObject.get(ELEMENT_PAGINATION);
    return JsonUtils.getAttributeAsString(paginationElement.getAsJsonObject(), ELEMENT_PAGINATION_NEXT);
  }

  private static Set<TopicUrlDTO> parseItems(final JsonObject aContentObject) {
    final Set<TopicUrlDTO> items = new HashSet<>();

    if (aContentObject.has(ELEMENT_ITEMS)) {
      final JsonArray itemArray = aContentObject.get(ELEMENT_ITEMS).getAsJsonArray();
      for (final JsonElement itemElement : itemArray) {
        final Optional<JsonObject> reference = Optional.of(itemElement.getAsJsonObject().get(ELEMENT_REFERENCE).getAsJsonObject());
        final Optional<String> url = JsonUtils.getAttributeAsString(reference.get(), ELEMENT_REFERENCE_URL);
        final Optional<String> type = JsonUtils.getAttributeAsString(reference.get(), ELEMENT_REFERENCE_TYPE);
        if (url.isPresent() && !url.get().isEmpty() && type.orElse("empty").equalsIgnoreCase("VideoRef")) {
          final Optional<String> id = JsonUtils.getAttributeAsString(reference.get(), ELEMENT_REFERENCE_ID);
          items.add(new TopicUrlDTO(id.get(), url.get()));
        }
      }
    }

    return items;
  }

  @Override
  public Optional<PagedElementListDTO<TopicUrlDTO>> deserialize(
      final JsonElement aJsonElement, final Type aType, final JsonDeserializationContext aContext) {
    final JsonObject jsonObject = aJsonElement.getAsJsonObject();

    if (!jsonObject.has(ELEMENT_ITEMS)) {
      return Optional.empty();
    }

    final Set<TopicUrlDTO> itemIds = parseItems(jsonObject);
    final Optional<String> nextUrl = parseNextUrl(jsonObject);

    final PagedElementListDTO<TopicUrlDTO> dto = new PagedElementListDTO<>();
    dto.setNextPage(nextUrl);
    dto.addElements(itemIds);
    return Optional.of(dto);
  }
}
