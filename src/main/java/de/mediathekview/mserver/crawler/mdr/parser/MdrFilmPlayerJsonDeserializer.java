package de.mediathekview.mserver.crawler.mdr.parser;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import de.mediathekview.mserver.base.utils.JsonUtils;
import java.lang.reflect.Type;
import java.util.Optional;

public class MdrFilmPlayerJsonDeserializer implements JsonDeserializer<Optional<String>> {

  private static final String ATTRIBUTE_XML = "playerXml";

  @Override
  public Optional<String> deserialize(JsonElement aJsonElement, Type aType, JsonDeserializationContext aContext) {

    return JsonUtils.getAttributeAsString(aJsonElement.getAsJsonObject(), ATTRIBUTE_XML);
  }
}
