package de.mediathekview.mserver.crawler.arte.json;

import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;

public class ArteFilmListDeserializer implements JsonDeserializer<Set<JsonElement>> {

  private final AbstractCrawler crawler;

  public ArteFilmListDeserializer(final AbstractCrawler aCrawler) {
    crawler = aCrawler;
  }

  @Override
  public Set<JsonElement> deserialize(final JsonElement aJsonElement, final Type aType,
      final JsonDeserializationContext aJsonDeserializationContext) throws JsonParseException {
    final Set<JsonElement> filmListElements = new HashSet<>();
    return filmListElements;
  }

}
