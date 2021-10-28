package de.mediathekview.mserver.crawler.ard.json;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.mediathekview.mserver.base.utils.JsonUtils;
import de.mediathekview.mserver.crawler.ard.ArdConstants;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;

import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

public class ArdTopicsOverviewDeserializer implements JsonDeserializer<Set<CrawlerUrlDTO>> {

  private static final String ELEMENT_COMPILATIONS = "compilations";
  private static final String ELEMENT_TEASERS = "teasers";
  private static final String ELEMENT_LINKS = "links";
  private static final String ELEMENT_TARGET = "target";
  private static final String ELEMENT_WIDGETS = "widgets";

  private static final String ATTRIBUTE_ID = "id";

  @Override
  public Set<CrawlerUrlDTO> deserialize(
      final JsonElement jsonElement, final Type type, final JsonDeserializationContext context) {
    final Set<CrawlerUrlDTO> results = new HashSet<>();

    if (!jsonElement.getAsJsonObject().has(ELEMENT_WIDGETS)
        || !jsonElement.getAsJsonObject().get(ELEMENT_WIDGETS).isJsonArray()
        || jsonElement.getAsJsonObject().getAsJsonArray(ELEMENT_WIDGETS).size() == 0
        || !jsonElement
            .getAsJsonObject()
            .getAsJsonArray(ELEMENT_WIDGETS)
            .get(0)
            .getAsJsonObject()
            .has(ELEMENT_COMPILATIONS)) {
      return results;
    }

    final JsonObject compilationObject =
        jsonElement
            .getAsJsonObject()
            .getAsJsonArray(ELEMENT_WIDGETS)
            .get(0)
            .getAsJsonObject()
            .get(ELEMENT_COMPILATIONS)
            .getAsJsonObject();

    for (final Entry<String, JsonElement> letterEntry : compilationObject.entrySet()) {
      results.addAll(parseLetter(letterEntry.getValue().getAsJsonObject()));
    }

    return results;
  }

  private Set<CrawlerUrlDTO> parseLetter(final JsonObject letterObject) {
    final Set<CrawlerUrlDTO> results = new HashSet<>();

    if (!letterObject.getAsJsonObject().has(ELEMENT_TEASERS)
        || !letterObject.getAsJsonObject().get(ELEMENT_TEASERS).isJsonArray()) {
      return results;
    }

    for (final JsonElement teaserElement :
        letterObject.getAsJsonObject().getAsJsonArray(ELEMENT_TEASERS)) {
      final JsonObject teaserObject = teaserElement.getAsJsonObject();
      final Optional<String> id;

      if (JsonUtils.checkTreePath(teaserObject, null, ELEMENT_LINKS, ELEMENT_TARGET)) {
        final JsonObject targetObject =
            teaserObject.get(ELEMENT_LINKS).getAsJsonObject().get(ELEMENT_TARGET).getAsJsonObject();
        id = JsonUtils.getAttributeAsString(targetObject, ATTRIBUTE_ID);
      } else {
        id = JsonUtils.getAttributeAsString(teaserObject, ATTRIBUTE_ID);
      }

      id.ifPresent(
          s ->
              results.add(
                  new CrawlerUrlDTO(
                      String.format(ArdConstants.TOPIC_URL, s, ArdConstants.TOPIC_PAGE_SIZE))));
    }

    return results;
  }
}
