package mServer.crawler.sender.phoenix.parser;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import mServer.crawler.sender.orf.JsonUtils;
import mServer.crawler.sender.phoenix.PhoenixConstants;
import mServer.crawler.sender.phoenix.SendungOverviewDto;

public class PhoenixSendungOverviewDeserializer implements JsonDeserializer<Optional<SendungOverviewDto>> {

  private static final String ELEMENT_CONTENT = "content";
  private static final String ELEMENT_ITEMS = "items";

  private static final String ATTRIBUTE_LINK = "link";
  private static final String ATTRIBUTE_NEXT_URL = "next_url";

  @Override
  public Optional<SendungOverviewDto> deserialize(JsonElement aJsonElement, Type aType, JsonDeserializationContext aContext) {
    final JsonObject jsonObject = aJsonElement.getAsJsonObject();

    if (!jsonObject.has(ELEMENT_CONTENT)) {
      return Optional.empty();
    }

    final JsonObject contentObject = jsonObject.get(ELEMENT_CONTENT).getAsJsonObject();

    final Set<String> itemIds = parseItems(contentObject);
    final Optional<String> nextUrl = parseNextUrl(contentObject);

    SendungOverviewDto dto = createDto(itemIds, nextUrl);
    return Optional.of(dto);
  }

  private static SendungOverviewDto createDto(final Set<String> itemIds, final Optional<String> nextUrl) {
    SendungOverviewDto dto = new SendungOverviewDto();
    dto.setNextPageId(nextUrl);

    for (String itemId : itemIds) {
      dto.addUrl(PhoenixConstants.URL_FILM_DETAIL_JSON + itemId);
    }

    return dto;
  }

  private static Optional<String> parseNextUrl(JsonObject contentObject) {
    Optional<String> nextUrl = JsonUtils.getAttributeAsString(contentObject, ATTRIBUTE_NEXT_URL);
    if (nextUrl.isPresent() && nextUrl.get().isEmpty()) {
      return Optional.empty();
    }

    return nextUrl;
  }

  private static Set<String> parseItems(final JsonObject aContentObject) {
    final Set<String> items = new HashSet<>();

    if (aContentObject.has(ELEMENT_ITEMS)) {
      final JsonArray itemArray = aContentObject.get(ELEMENT_ITEMS).getAsJsonArray();
      for (final JsonElement itemElement : itemArray) {

        final Optional<String> htmlUrl = JsonUtils.getAttributeAsString(itemElement.getAsJsonObject(), ATTRIBUTE_LINK);
        if (htmlUrl.isPresent() && !htmlUrl.get().isEmpty()) {
          items.add(extractIdFromHtmlUrl(htmlUrl.get()));
        }
      }
    }

    return items;
  }

  private static String extractIdFromHtmlUrl(String aHtmlUrl) {
    int indexBegin = aHtmlUrl.lastIndexOf('-') + 1;
    int indexEnd = aHtmlUrl.lastIndexOf('.');

    try {
      return aHtmlUrl.substring(indexBegin, indexEnd);
    } catch (StringIndexOutOfBoundsException ex) {
      return "";
    }
  }
}
