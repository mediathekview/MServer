package de.mediathekview.mserver.crawler.orfon.json;

import com.google.gson.*;

import de.mediathekview.mserver.crawler.basic.TopicUrlDTO;
import de.mediathekview.mserver.crawler.orfon.OrfOnConstants;

import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class OrfOnScheduleDeserializer implements JsonDeserializer<Set<TopicUrlDTO>> {
  private static final Logger LOG = LogManager.getLogger(OrfOnScheduleDeserializer.class);
  private static final String TAG_FILM_NAME = "title";
  private static final String TAG_FILM_ID = "id";
  //
  
  @Override
  public Set<TopicUrlDTO> deserialize(
      final JsonElement jsonElement, final Type typeOfT, final JsonDeserializationContext context)
      throws JsonParseException {
    Set<TopicUrlDTO> collectIds = new HashSet<TopicUrlDTO>();
    final JsonArray elements = jsonElement.getAsJsonArray();
    for (JsonElement element : elements) {
      String name = element.getAsJsonObject().get(TAG_FILM_NAME).getAsString();
      String id = element.getAsJsonObject().get(TAG_FILM_ID).getAsString();
      String url = OrfOnConstants.EPISODE + "/" + id;
      LOG.debug("found {} {} {}", id, name, url);
      TopicUrlDTO entry = new TopicUrlDTO(id,url);
      collectIds.add(entry);
    }
    return collectIds;
  }
}
