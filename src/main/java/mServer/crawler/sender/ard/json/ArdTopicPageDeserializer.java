package mServer.crawler.sender.ard.json;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;

import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;
import mServer.crawler.sender.ard.ArdFilmInfoDto;
import mServer.crawler.sender.base.JsonUtils;

public class ArdTopicPageDeserializer extends ArdTeasersDeserializer
        implements JsonDeserializer<Set<ArdFilmInfoDto>> {

  private static final String ELEMENT_DATA = "data";
  private static final String ELEMENT_SHOW_PAGE = "showPage";
  private static final String ELEMENT_TEASERS = "teasers";

  @Override
  public Set<ArdFilmInfoDto> deserialize(
          final JsonElement jsonElement, final Type type, final JsonDeserializationContext context) {
    final Set<ArdFilmInfoDto> results = new HashSet<>();

    if (JsonUtils.checkTreePath(
            jsonElement, ELEMENT_DATA, ELEMENT_SHOW_PAGE, ELEMENT_TEASERS)) {
      final JsonElement teasersElement
              = jsonElement
                      .getAsJsonObject()
                      .get(ELEMENT_DATA)
                      .getAsJsonObject()
                      .get(ELEMENT_SHOW_PAGE)
                      .getAsJsonObject()
                      .get(ELEMENT_TEASERS);
      if (!teasersElement.isJsonNull()) {
        results.addAll(parseTeasers(teasersElement.getAsJsonArray()));
      }
    }

    return results;
  }
}
