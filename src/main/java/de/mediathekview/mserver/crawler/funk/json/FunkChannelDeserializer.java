package de.mediathekview.mserver.crawler.funk.json;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import de.mediathekview.mserver.crawler.funk.FunkChannelDTO;

import java.lang.reflect.Type;

public class FunkChannelDeserializer implements JsonDeserializer<FunkChannelDTO> {
  @Override
  public FunkChannelDTO deserialize(
      final JsonElement json, final Type typeOfT, final JsonDeserializationContext context)
      throws JsonParseException {
    return null;
  }
}
