package de.mediathekview.mserver.crawler.phoenix.parser;

import com.google.gson.*;
import de.mediathekview.mserver.base.utils.JsonUtils;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.basic.PagedElementListDTO;
import de.mediathekview.mserver.crawler.phoenix.PhoenixConstants;

import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class PhoenixSendungOverviewDeserializer
    implements JsonDeserializer<Optional<PagedElementListDTO<CrawlerUrlDTO>>> {

  private static final String ELEMENT_CONTENT = "content";
  private static final String ELEMENT_ITEMS = "items";

  private static final String ATTRIBUTE_LINK = "link";
  private static final String ATTRIBUTE_NEXT_URL = "next_url";

  private static PagedElementListDTO<CrawlerUrlDTO> createDto(
      final Set<String> itemIds, final Optional<String> nextUrl) {
    final PagedElementListDTO<CrawlerUrlDTO> dto = new PagedElementListDTO<>();
    dto.setNextPage(nextUrl);

    for (final String itemId : itemIds) {
      dto.addElement(new CrawlerUrlDTO(PhoenixConstants.URL_FILM_DETAIL_JSON + itemId));
    }

    return dto;
  }

  private static Optional<String> parseNextUrl(final JsonObject contentObject) {
    final Optional<String> nextUrl =
        JsonUtils.getAttributeAsString(contentObject, ATTRIBUTE_NEXT_URL);
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

        final Optional<String> htmlUrl =
            JsonUtils.getAttributeAsString(itemElement.getAsJsonObject(), ATTRIBUTE_LINK);
        
        final Optional<String> hasVideo = JsonUtils.getElementValueAsString(itemElement, "inhalt_video");
        if (htmlUrl.isPresent() && !htmlUrl.get().isEmpty() && (hasVideo.orElse("true").equalsIgnoreCase("true"))) {
          items.add(extractIdFromHtmlUrl(htmlUrl.get()));
        }
      }
    }

    return items;
  }

  private static String extractIdFromHtmlUrl(final String aHtmlUrl) {
    final int indexBegin = aHtmlUrl.lastIndexOf('-') + 1;
    final int indexEnd = aHtmlUrl.lastIndexOf('.');

    try {
      return aHtmlUrl.substring(indexBegin, indexEnd);
    } catch (final StringIndexOutOfBoundsException ex) {
      return "";
    }
  }

  @Override
  public Optional<PagedElementListDTO<CrawlerUrlDTO>> deserialize(
      final JsonElement aJsonElement, final Type aType, final JsonDeserializationContext aContext) {
    final JsonObject jsonObject = aJsonElement.getAsJsonObject();

    if (!jsonObject.has(ELEMENT_CONTENT)) {
      return Optional.empty();
    }

    final JsonObject contentObject = jsonObject.get(ELEMENT_CONTENT).getAsJsonObject();

    final Set<String> itemIds = parseItems(contentObject);
    final Optional<String> nextUrl = parseNextUrl(contentObject);

    final PagedElementListDTO<CrawlerUrlDTO> dto = createDto(itemIds, nextUrl);
    return Optional.of(dto);
  }
}
