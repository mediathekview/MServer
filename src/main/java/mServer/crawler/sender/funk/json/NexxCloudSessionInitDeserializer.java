package mServer.crawler.sender.funk.json;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import mServer.crawler.sender.base.JsonUtils;

import java.lang.reflect.Type;

public class NexxCloudSessionInitDeserializer implements JsonDeserializer<Long> {
  private static final String TAG_RESULT = "result";
  private static final String TAG_GENERAL = "general";
  private static final String ATTRIBUTE_CID = "cid";

  @Override
  public Long deserialize(
          final JsonElement jsonElement, final Type typeOfT, final JsonDeserializationContext context)
          throws JsonParseException {
    if (JsonUtils.checkTreePath(jsonElement, TAG_RESULT, TAG_GENERAL, ATTRIBUTE_CID)) {
      return jsonElement
              .getAsJsonObject()
              .getAsJsonObject(TAG_RESULT)
              .getAsJsonObject(TAG_GENERAL)
              .get(ATTRIBUTE_CID)
              .getAsLong();
    }

    return null;
  }
}
