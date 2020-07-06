package de.mediathekview.mserver.crawler.ard.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import de.mediathekview.mserver.crawler.ard.ArdFilmInfoDto;

import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

public class ArdTopicPageDeserializer extends ArdTeasersDeserializer
    implements JsonDeserializer<Set<ArdFilmInfoDto>> {

  private static final String ELEMENT_TEASERS = "teasers";

  @Override
  public Set<ArdFilmInfoDto> deserialize(
      JsonElement jsonElement, Type type, JsonDeserializationContext context) {
    Set<ArdFilmInfoDto> results = new HashSet<>();

    if (jsonElement.getAsJsonObject().has(ELEMENT_TEASERS)) {
      JsonArray teasers = jsonElement.getAsJsonObject().get(ELEMENT_TEASERS).getAsJsonArray();
      results.addAll(parseTeasers(teasers));
    }

    return results;
  }
}
