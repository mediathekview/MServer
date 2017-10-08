package de.mediathekview.mserver.crawler.funk.parser;

import java.lang.reflect.Type;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import de.mediathekview.mlib.daten.Film;

public class FunkFilmDeserializer implements JsonDeserializer<Film> {

  @Override
  public Film deserialize(final JsonElement aJsonElement, final Type aType,
      final JsonDeserializationContext aJsonDeserializationContext) {
    return null;
  }

}
