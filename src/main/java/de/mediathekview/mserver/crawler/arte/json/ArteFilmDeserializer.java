package de.mediathekview.mserver.crawler.arte.json;

import java.lang.reflect.Type;
import java.util.Optional;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;

public class ArteFilmDeserializer implements JsonDeserializer<Optional<Film>> {
  private final AbstractCrawler crawler;

  public ArteFilmDeserializer(final AbstractCrawler aCrawler) {
    crawler = aCrawler;
  }

  @Override
  public Optional<Film> deserialize(final JsonElement aArg0, final Type aArg1,
      final JsonDeserializationContext aArg2) throws JsonParseException {
    // TODO Auto-generated method stub
    return null;
  }

}
