package de.mediathekview.mserver.crawler.arte.json;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.mediathekview.mserver.base.utils.JsonUtils;
import de.mediathekview.mserver.crawler.arte.ArteConstants;
import de.mediathekview.mserver.crawler.basic.SendungOverviewDto;
import java.lang.reflect.Type;
import java.util.Optional;

public class ArteSubcategoryDeserializer extends ArteDeserializerBase implements JsonDeserializer<SendungOverviewDto> {

  private static final String ELEMENT_SUBCATEGORIES = "subcategories";
  private static final String ATTRIBUTE_CODE = "code";
  private static final String ATTRIBUTE_LANGUAGE = "language";

  @Override
  public SendungOverviewDto deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context) {
    SendungOverviewDto dto = new SendungOverviewDto();

    JsonObject jsonObject = jsonElement.getAsJsonObject();

    parseSubcategories(jsonObject, dto);
    dto.setNextPageId(getNextPageLink(jsonObject));

    return dto;
  }

  private void parseSubcategories(final JsonObject jsonObject, final SendungOverviewDto dto) {
    if (jsonObject.has(ELEMENT_SUBCATEGORIES) && jsonObject.get(ELEMENT_SUBCATEGORIES).isJsonArray()) {
      for (JsonElement subcategoryElement : jsonObject.get(ELEMENT_SUBCATEGORIES).getAsJsonArray()) {
        Optional<String> code = JsonUtils.getAttributeAsString(subcategoryElement.getAsJsonObject(), ATTRIBUTE_CODE);
        Optional<String> language = JsonUtils.getAttributeAsString(subcategoryElement.getAsJsonObject(), ATTRIBUTE_LANGUAGE);
        if (code.isPresent() && language.isPresent()) {
          dto.addUrl(buildSubcategoryVideoUrl(language.get(), code.get()));
        }
      }
    }
  }

  private String buildSubcategoryVideoUrl(final String language, final String subcategoryCode) {
    return String.format(ArteConstants.URL_SUBCATEGORY_VIDEOS, language, subcategoryCode);
  }

  @Override
  protected String getBaseElementName() {
    return ELEMENT_SUBCATEGORIES;
  }
}
