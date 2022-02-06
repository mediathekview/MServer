package mServer.crawler.sender.arte.json;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import mServer.crawler.sender.arte.ArteConstants;
import mServer.crawler.sender.arte.ArteSubcategoryUrlDto;
import mServer.crawler.sender.base.JsonUtils;
import mServer.crawler.sender.base.TopicUrlDTO;

import java.lang.reflect.Type;
import java.util.Optional;

public class ArteSubcategoryDeserializer extends ArteDeserializerBase implements JsonDeserializer<ArteSubcategoryUrlDto> {

  private static final String ELEMENT_SUBCATEGORIES = "subcategories";
  private static final String ATTRIBUTE_CODE = "code";
  private static final String ATTRIBUTE_LANGUAGE = "language";

  @Override
  public ArteSubcategoryUrlDto deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context) {
    ArteSubcategoryUrlDto dto = new ArteSubcategoryUrlDto();

    JsonObject jsonObject = jsonElement.getAsJsonObject();

    parseSubcategories(jsonObject, dto);
    dto.setNextPageId(getNextPageLink(jsonObject));

    return dto;
  }

  private void parseSubcategories(final JsonObject jsonObject, final ArteSubcategoryUrlDto dto) {
    if (jsonObject.has(ELEMENT_SUBCATEGORIES) && jsonObject.get(ELEMENT_SUBCATEGORIES).isJsonArray()) {
      for (JsonElement subcategoryElement : jsonObject.get(ELEMENT_SUBCATEGORIES).getAsJsonArray()) {
        Optional<String> code = JsonUtils.getAttributeAsString(subcategoryElement.getAsJsonObject(), ATTRIBUTE_CODE);
        Optional<String> language = JsonUtils.getAttributeAsString(subcategoryElement.getAsJsonObject(), ATTRIBUTE_LANGUAGE);
        if (code.isPresent() && language.isPresent()) {
          dto.addUrl(new TopicUrlDTO(code.get(), buildSubcategoryVideoUrl(language.get(), code.get())));
        }
      }
    }
  }

  private String buildSubcategoryVideoUrl(final String language, final String subcategoryCode) {
    return String.format(ArteConstants.URL_SUBCATEGORY_VIDEOS, ArteConstants.BASE_URL_WWW, language, subcategoryCode, 1);
  }

  @Override
  protected String getBaseElementName() {
    return ELEMENT_SUBCATEGORIES;
  }
}
