package de.mediathekview.mserver.crawler.funk.json;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import de.mediathekview.mserver.base.utils.JsonUtils;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;

import java.lang.reflect.Type;

public class NexxCloudSessionInitDeserializer implements JsonDeserializer<Long> {
  private static final String TAG_RESULT = "result";
  private static final String TAG_GENERAL = "general";
  private static final String ATTRIBUTE_CID = "cid";
  private final AbstractCrawler crawler;

  public NexxCloudSessionInitDeserializer(final AbstractCrawler aCrawler) {
    crawler = aCrawler;
  }

  @Override
  public Long deserialize(
      final JsonElement jsonElement, final Type typeOfT, final JsonDeserializationContext context)
      throws JsonParseException {
    if (JsonUtils.checkTreePath(jsonElement, crawler, TAG_RESULT, TAG_GENERAL, ATTRIBUTE_CID)) {
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
