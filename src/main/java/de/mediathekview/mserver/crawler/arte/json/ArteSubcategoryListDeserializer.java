package de.mediathekview.mserver.crawler.arte.json;

import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import de.mediathekview.mserver.base.utils.JsonUtils;
import de.mediathekview.mserver.crawler.arte.ArteLanguage;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;

public class ArteSubcategoryListDeserializer implements JsonDeserializer<Set<CrawlerUrlDTO>> {
  private static final String JSON_ELEMENT_CODE = "code";
  /**
   * <ul>
   * <li>1. Parameter the language code</li>
   * <li>2. Parameter the subcategory code</li>
   * </ul>
   */
  private static final String SUBCATEGORY_VIDEOS_URL_PATTERN =
      "https://www.arte.tv/guide/api/api/zones/%s/videos_subcategory_%s/";
  private static final String JSON_ELEMENT_SUBCATEGORIES = "subcategories";
  private final AbstractCrawler crawler;
  private final ArteLanguage language;


  public ArteSubcategoryListDeserializer(final AbstractCrawler aCrawler,
      final ArteLanguage aLanguage) {
    crawler = aCrawler;
    language = aLanguage;
  }


  @Override
  public Set<CrawlerUrlDTO> deserialize(final JsonElement aJsonElement, final Type aType,
      final JsonDeserializationContext aJsonDeserializationContext) throws JsonParseException {
    final Set<CrawlerUrlDTO> result = new HashSet<>();
    if (aJsonElement.isJsonObject()) {
      final JsonObject mainObj = aJsonElement.getAsJsonObject();
      if (JsonUtils.checkTreePath(mainObj, Optional.of(crawler), JSON_ELEMENT_SUBCATEGORIES)) {
        for (final JsonElement subcategory : mainObj.get(JSON_ELEMENT_SUBCATEGORIES)
            .getAsJsonArray()) {
          final Optional<CrawlerUrlDTO> crawlerUrlDto = createCrawlerUrlDto(subcategory);
          if (crawlerUrlDto.isPresent()) {
            result.add(crawlerUrlDto.get());
          }
        }
      }
    }
    return result;
  }


  private Optional<CrawlerUrlDTO> createCrawlerUrlDto(final JsonElement subcategory) {
    final Optional<String> subcategoryCode = subcategoryToCode(subcategory);
    if (subcategoryCode.isPresent()) {
      return Optional.of(new CrawlerUrlDTO(String.format(SUBCATEGORY_VIDEOS_URL_PATTERN,
          language.getLanguageCode().toLowerCase(), subcategoryCode.get())));
    }
    return Optional.empty();

  }


  private Optional<String> subcategoryToCode(final JsonElement subcategory) {
    if (JsonUtils.hasElements(subcategory, Optional.of(crawler), JSON_ELEMENT_CODE)) {
      return Optional.of(subcategory.getAsJsonObject().get(JSON_ELEMENT_CODE).getAsString());
    }
    return Optional.empty();
  }

}
