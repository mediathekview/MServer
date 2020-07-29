package de.mediathekview.mserver.crawler.srf.parser;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import de.mediathekview.mserver.base.utils.JsonUtils;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.srf.SrfConstants;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class SrfTopicsDeserializer implements JsonDeserializer<Set<CrawlerUrlDTO>> {

  private static final String ELEMENT_DATA = "data";
  private static final String ATTRIBUTE_ID = "id";

  @Override
  public Set<CrawlerUrlDTO> deserialize(
      JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) {
    final Set<CrawlerUrlDTO> results = new HashSet<>();

    if (!jsonElement.getAsJsonObject().has(ELEMENT_DATA)
        || !jsonElement.getAsJsonObject().get(ELEMENT_DATA).isJsonArray()) {
      return results;
    }

    final JsonArray data = jsonElement.getAsJsonObject().getAsJsonArray(ELEMENT_DATA);

    data.forEach(
        entry -> {
          final Optional<String> id =
              JsonUtils.getAttributeAsString(entry.getAsJsonObject(), ATTRIBUTE_ID);

          id.ifPresent(s -> results.add(
              new CrawlerUrlDTO(String.format(SrfConstants.SHOW_OVERVIEW_PAGE_URL, s))));
        });

    return results;
  }
}
