package de.mediathekview.mserver.crawler.hr.parser;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import de.mediathekview.mserver.base.utils.JsonUtils;
import java.lang.reflect.Type;
import java.util.Optional;

public class HrVideoJsonDeserializer implements JsonDeserializer<Optional<String>> {

  private static final String ATTRIBUTE_VIDEO_URL = "videoUrl";

  @Override
  public Optional<String> deserialize(JsonElement aJsonElement, Type aType, JsonDeserializationContext aContext) throws JsonParseException {

    JsonObject aJsonObject = aJsonElement.getAsJsonObject();
    return JsonUtils.getAttributeAsString(aJsonObject, ATTRIBUTE_VIDEO_URL);
  }
}
