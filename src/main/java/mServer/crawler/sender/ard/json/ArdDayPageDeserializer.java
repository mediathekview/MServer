package mServer.crawler.sender.ard.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;
import mServer.crawler.sender.ard.ArdFilmInfoDto;
import mServer.crawler.sender.base.JsonUtils;

public class ArdDayPageDeserializer extends ArdTeasersDeserializer implements JsonDeserializer<Set<ArdFilmInfoDto>> {

  private static final String ELEMENT_TEASERS = "teasers";

  @Override
  public Set<ArdFilmInfoDto> deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context) {
    Set<ArdFilmInfoDto> results = new HashSet<>();

    if (!jsonElement.isJsonArray()) {
      return results;
    }

    JsonObject element0 = jsonElement.getAsJsonArray().get(0).getAsJsonObject();

    if (element0.has(ELEMENT_TEASERS)) {
      JsonArray teasers = element0.get(ELEMENT_TEASERS).getAsJsonArray();
      results.addAll(parseTeasers(teasers));
    }

    return results;
  }

}
