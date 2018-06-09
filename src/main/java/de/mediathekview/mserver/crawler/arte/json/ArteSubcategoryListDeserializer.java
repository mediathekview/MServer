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
import de.mediathekview.mserver.crawler.arte.ArteCrawlerUrlDto;
import de.mediathekview.mserver.crawler.arte.ArteLanguage;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;

public class ArteSubcategoryListDeserializer implements JsonDeserializer<Set<ArteCrawlerUrlDto>> {
  private static final String JSON_ELEMENT_LABEL = "label";
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
  public Set<ArteCrawlerUrlDto> deserialize(final JsonElement aJsonElement, final Type aType,
      final JsonDeserializationContext aJsonDeserializationContext) throws JsonParseException {
    final Set<ArteCrawlerUrlDto> result = new HashSet<>();
    if (aJsonElement.isJsonObject()) {
      final JsonObject mainObj = aJsonElement.getAsJsonObject();
      if (JsonUtils.checkTreePath(mainObj, Optional.of(crawler), JSON_ELEMENT_SUBCATEGORIES)) {
        for (final JsonElement subcategory : mainObj.get(JSON_ELEMENT_SUBCATEGORIES)
            .getAsJsonArray()) {
          final Optional<ArteCrawlerUrlDto> crawlerUrlDto = createCrawlerUrlDto(subcategory);
          if (crawlerUrlDto.isPresent()) {
            result.add(crawlerUrlDto.get());
          }
        }
      }
    }
    return result;
  }


  private Optional<ArteCrawlerUrlDto> createCrawlerUrlDto(final JsonElement subcategory) {
    final Optional<String> subcategoryCode = subcategoryToCode(subcategory);
    if (subcategoryCode.isPresent()) {
      final ArteCrawlerUrlDto arteCrawlerUrlDto =
          new ArteCrawlerUrlDto(String.format(SUBCATEGORY_VIDEOS_URL_PATTERN,
              language.getLanguageCode().toLowerCase(), subcategoryCode.get()));

      arteCrawlerUrlDto.setCategory(getSubcategoryName(subcategory));
      return Optional.of(arteCrawlerUrlDto);
    }
    return Optional.empty();

  }


  private Optional<String> getSubcategoryName(final JsonElement subcategory) {
    if (JsonUtils.hasElements(subcategory, JSON_ELEMENT_LABEL)) {
      return Optional.of(subcategory.getAsJsonObject().get(JSON_ELEMENT_LABEL).getAsString());
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
