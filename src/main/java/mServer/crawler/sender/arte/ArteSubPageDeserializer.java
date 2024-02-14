package mServer.crawler.sender.arte;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.util.Optional;

public class ArteSubPageDeserializer extends ArteListBaseDeserializer implements JsonDeserializer<ArteCategoryFilmsDTO> {
  private static final String JSON_ELEMENT_VALUE = "value";

  @Override
  public ArteCategoryFilmsDTO deserialize(JsonElement aJsonElement, Type aType, JsonDeserializationContext aContext) throws JsonParseException {
    final ArteCategoryFilmsDTO dto = new ArteCategoryFilmsDTO();

    JsonElement rootElement = aJsonElement;
    if (aJsonElement.getAsJsonObject().has(JSON_ELEMENT_VALUE)) {
      rootElement = aJsonElement.getAsJsonObject().get(JSON_ELEMENT_VALUE);
    }

    JsonObject rootObject = rootElement.getAsJsonObject();
    extractProgramIdFromData(rootObject, dto);

    Optional<String> url = parsePagination(rootObject);
    url.ifPresent(dto::setNextPageUrl);

    return dto;
  }
}
