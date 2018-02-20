package de.mediathekview.mserver.crawler.arte.json;

import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import de.mediathekview.mserver.base.utils.JsonUtils;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;

public class ArteFilmListDeserializer implements JsonDeserializer<Set<JsonElement>> {

  private static final String JSON_ELEMENT_HREF = "href";
  private static final String JSON_ELEMENT_NEXT = "next";
  private static final String JSON_ELEMENT_LINKS = "links";
  private static final String JSON_ELEMENT_VIDEOS = "videos";
  private static final String JSON_ELEMENT_META = "meta";
  private final AbstractCrawler crawler;

  public ArteFilmListDeserializer(final AbstractCrawler aCrawler) {
    crawler = aCrawler;
  }

  @Override
  public Set<JsonElement> deserialize(final JsonElement aJsonElement, final Type aType,
      final JsonDeserializationContext aJsonDeserializationContext) throws JsonParseException {
    final Set<JsonElement> filmListElements = new HashSet<>();
    if (aJsonElement.isJsonObject()) {
      final JsonObject mainObj = aJsonElement.getAsJsonObject();
      if (JsonUtils.checkTreePath(mainObj, Optional.empty(), JSON_ELEMENT_META, JSON_ELEMENT_VIDEOS,
          JSON_ELEMENT_LINKS, JSON_ELEMENT_NEXT, JSON_ELEMENT_HREF)) {
        final String nextPageUrl = mainObj.get(JSON_ELEMENT_META).getAsJsonObject()
            .get(JSON_ELEMENT_VIDEOS).getAsJsonObject().get(JSON_ELEMENT_LINKS).getAsJsonObject()
            .get(JSON_ELEMENT_NEXT).getAsJsonObject().get(JSON_ELEMENT_HREF).getAsString();
        // TODO crawl next page. Response obj with json Elements and nex page?
      }
    }
    return filmListElements;
  }

}
