package de.mediathekview.mserver.crawler.ard.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import de.mediathekview.mserver.base.utils.JsonUtils;
import de.mediathekview.mserver.crawler.ard.ArdFilmInfoDto;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class ArdTopicPageDeserializer extends ArdTeasersDeserializer implements JsonDeserializer<Set<ArdFilmInfoDto>> {

  private static final String ELEMENT_DATA = "data";
  private static final String ELEMENT_SHOW_PAGE = "showPage";
  private static final String ELEMENT_TEASERS = "teasers";

  @Override
  public Set<ArdFilmInfoDto> deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context) {
    Set<ArdFilmInfoDto> results = new HashSet<>();

    if (JsonUtils.checkTreePath(jsonElement, Optional.empty(), ELEMENT_DATA, ELEMENT_SHOW_PAGE, ELEMENT_TEASERS)) {
      JsonArray teasers = jsonElement.getAsJsonObject()
          .get(ELEMENT_DATA).getAsJsonObject()
          .get(ELEMENT_SHOW_PAGE).getAsJsonObject()
          .get(ELEMENT_TEASERS).getAsJsonArray();
      results.addAll(parseTeasers(teasers));
    }

    return results;
  }
}
