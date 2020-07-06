package mServer.crawler.sender.ard.json;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import mServer.crawler.sender.ard.ArdConstants;
import mServer.crawler.sender.ard.ArdUrlBuilder;
import mServer.crawler.sender.base.CrawlerUrlDTO;
import mServer.crawler.sender.base.JsonUtils;

public class ArdTopicsOverviewDeserializer implements JsonDeserializer<Set<CrawlerUrlDTO>> {

  private static final String ELEMENT_COMPILATIONS = "compilations";
  private static final String ELEMENT_TEASERS = "teasers";
  private static final String ELEMENT_LINKS = "links";
  private static final String ELEMENT_TARGET = "target";
  private static final String ELEMENT_WIDGETS = "widgets";

  private static final String ATTRIBUTE_ID = "id";

  @Override
  public Set<CrawlerUrlDTO> deserialize(JsonElement jsonElement, Type type,
      JsonDeserializationContext context) {
    Set<CrawlerUrlDTO> results = new HashSet<>();

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

    final JsonObject compilationObject = jsonElement.getAsJsonObject()
        .getAsJsonArray(ELEMENT_WIDGETS).get(0)
        .getAsJsonObject().get(ELEMENT_COMPILATIONS).getAsJsonObject();

    for (Entry<String, JsonElement> letterEntry : compilationObject.entrySet()) {
      results.addAll(parseLetter(letterEntry.getValue().getAsJsonObject()));
    }

    return results;
  }

  private Set<CrawlerUrlDTO> parseLetter(final JsonObject letterObject) {
    Set<CrawlerUrlDTO> results = new HashSet<>();

    if (!letterObject.getAsJsonObject().has(ELEMENT_TEASERS)
        || !letterObject.getAsJsonObject().get(ELEMENT_TEASERS).isJsonArray()) {
      return results;
    }

    for (JsonElement teaserElement :
        letterObject.getAsJsonObject().getAsJsonArray(ELEMENT_TEASERS)) {
      JsonObject teaserObject = teaserElement.getAsJsonObject();
      Optional<String> id;
      if (JsonUtils.checkTreePath(teaserObject, ELEMENT_LINKS, ELEMENT_TARGET)) {
        JsonObject targetObject =
            teaserObject.get(ELEMENT_LINKS).getAsJsonObject().get(ELEMENT_TARGET).getAsJsonObject();
        id = JsonUtils.getAttributeAsString(targetObject, ATTRIBUTE_ID);
      } else {
        id = JsonUtils.getAttributeAsString(teaserObject, ATTRIBUTE_ID);
      }

      id.ifPresent(s -> results.add(new CrawlerUrlDTO(
          String.format(ArdConstants.TOPIC_URL, s, ArdConstants.TOPIC_PAGE_SIZE))));
    }

    return results;
  }
}
