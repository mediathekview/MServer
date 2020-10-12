package de.mediathekview.mserver.crawler.ard.json;

import com.google.gson.*;
import de.mediathekview.mserver.crawler.ard.ArdFilmInfoDto;

import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

public class ArdDayPageDeserializer extends ArdTeasersDeserializer
    implements JsonDeserializer<Set<ArdFilmInfoDto>> {

  private static final String ELEMENT_TEASERS = "teasers";

  @Override
  public Set<ArdFilmInfoDto> deserialize(
          final JsonElement jsonElement, final Type type, final JsonDeserializationContext context) {
    final Set<ArdFilmInfoDto> results = new HashSet<>();

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
