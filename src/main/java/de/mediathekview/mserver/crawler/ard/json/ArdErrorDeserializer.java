package de.mediathekview.mserver.crawler.ard.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.mediathekview.mserver.base.utils.JsonUtils;
import java.lang.reflect.Type;
import java.util.Optional;

public class ArdErrorDeserializer implements JsonDeserializer<Optional<ArdErrorInfoDto>> {

  private static final String ATTRIBUTE_CODE = "code";
  private static final String ATTRIBUTE_MESSAGE = "message";
  private static final String ELEMENT_ERRORS = "errors";
  private static final String ELEMENT_EXTENSIONS = "extensions";

  @Override
  public Optional<ArdErrorInfoDto> deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context) {

    if (!JsonUtils.hasElements(jsonElement, ELEMENT_ERRORS)) {
      return Optional.empty();
    }


    JsonArray errors = jsonElement.getAsJsonObject().get(ELEMENT_ERRORS).getAsJsonArray();
    if (errors.size() > 0) {
      return parseError(errors.get(0).getAsJsonObject());
    }

    return Optional.empty();
  }

  private Optional<ArdErrorInfoDto> parseError(JsonObject error) {
    Optional<String> message = JsonUtils.getAttributeAsString(error, ATTRIBUTE_MESSAGE);
    Optional<String> code = Optional.empty();

    if (JsonUtils.hasElements(error, ELEMENT_EXTENSIONS)) {
      code = JsonUtils.getAttributeAsString(error.get(ELEMENT_EXTENSIONS).getAsJsonObject(), ATTRIBUTE_CODE);
    }

    ArdErrorInfoDto result = new ArdErrorInfoDto(code.orElse(""), message.orElse(""));
    return Optional.of(result);
  }
}
