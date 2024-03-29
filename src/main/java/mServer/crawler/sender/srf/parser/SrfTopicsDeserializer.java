package mServer.crawler.sender.srf.parser;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import mServer.crawler.sender.base.JsonUtils;
import mServer.crawler.sender.base.TopicUrlDTO;
import mServer.crawler.sender.srf.SrfConstants;

public class SrfTopicsDeserializer implements JsonDeserializer<Set<TopicUrlDTO>> {

  private static final String ELEMENT_DATA = "data";
  private static final String ATTRIBUTE_ID = "id";

  @Override
  public Set<TopicUrlDTO> deserialize(
          JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) {
    final Set<TopicUrlDTO> results = new HashSet<>();

    if (!jsonElement.getAsJsonObject().has(ELEMENT_DATA)
            || !jsonElement.getAsJsonObject().get(ELEMENT_DATA).isJsonArray()) {
      return results;
    }

    final JsonArray data = jsonElement.getAsJsonObject().getAsJsonArray(ELEMENT_DATA);

    data.forEach(
            entry -> {
              final Optional<String> id
              = JsonUtils.getAttributeAsString(entry.getAsJsonObject(), ATTRIBUTE_ID);

              id.ifPresent(
                      s
                      -> results.add(
                              new TopicUrlDTO(s, String.format(SrfConstants.SHOW_OVERVIEW_PAGE_URL, SrfConstants.BASE_URL, s))));
            });

    return results;
  }
}
