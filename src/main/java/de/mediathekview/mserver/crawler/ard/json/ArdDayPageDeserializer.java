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

    final JsonObject firstElement = jsonElement.getAsJsonArray().get(0).getAsJsonObject();

    if (firstElement.has(ELEMENT_TEASERS)) {
      final JsonArray teasers = firstElement.get(ELEMENT_TEASERS).getAsJsonArray();
      results.addAll(parseTeasers(teasers));
    }

    return results;
  }
}
