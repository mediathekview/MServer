package mServer.crawler.sender.br;

import java.lang.reflect.Type;
import java.util.Optional;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import mServer.crawler.sender.MediathekReader;

public class BrMissedSendungsFolgenDeserializer implements JsonDeserializer<BrIdsDTO> {

  private static final String JSON_ELEMENT_CONTAINER_TODAY = "containerToday";
  private static final String JSON_ELEMENT_BROADCAST_SERVICE = "broadcastService";
  private static final String JSON_ELEMENT_VIEWER = "viewer";
  private static final String JSON_ELEMENT_DATA = "data";
  private static final String JSON_ELEMENT_ID = "id";
  private static final String JSON_ELEMENT_BROADCAST_EVENT = "broadcastEvent";
  private static final String JSON_ELEMENT_PUBLICATION = "publicationOf";

  private final MediathekReader crawler;

  public BrMissedSendungsFolgenDeserializer(final MediathekReader aCrawler) {
    crawler = aCrawler;
  }

  /**
   * Resolves the Sendung ids which are needed to get the Sendung details.<br>
   * The data has this structure:
   * <code>data -> viewer -> broadcastService -> containerToday -> edges[] -> node -> id</code>
   *
   * @param aElement
   * @param aType
   * @param aContext
   * @return
   */
  @Override
  public BrIdsDTO deserialize(final JsonElement aElement, final Type aType,
          final JsonDeserializationContext aContext) {
    final BrIdsDTO results = new BrIdsDTO();

    final JsonObject baseObject = aElement.getAsJsonObject();
    final Optional<JsonArray> dayEntries = getDayEntries(baseObject);
    if (dayEntries.isPresent()) {
      for (final JsonElement dayEntry : dayEntries.get()) {
        final JsonObject dayEntryObject = dayEntry.getAsJsonObject();
        if (dayEntryObject.has(JSON_ELEMENT_BROADCAST_EVENT)
                && !dayEntryObject.get(JSON_ELEMENT_BROADCAST_EVENT).isJsonNull()) {
          final JsonObject broadcastEvent = dayEntryObject.getAsJsonObject(JSON_ELEMENT_BROADCAST_EVENT);
          if (broadcastEvent.has(JSON_ELEMENT_PUBLICATION)) {
            final JsonObject publication = broadcastEvent.getAsJsonObject(JSON_ELEMENT_PUBLICATION);
            if (publication.has(JSON_ELEMENT_ID)) {
              results.add(publication.get(JSON_ELEMENT_ID).getAsString());
            }
          }
        }
      }
    }

    return results;
  }

  private Optional<JsonArray> getDayEntries(final JsonObject aBaseObject) {
    if (!aBaseObject.has(JSON_ELEMENT_DATA)) {
      return Optional.empty();
    }

    final JsonObject data = aBaseObject.getAsJsonObject(JSON_ELEMENT_DATA);
    if (!data.has(JSON_ELEMENT_VIEWER)) {
      return Optional.empty();
    }

    final JsonObject viewer = data.getAsJsonObject(JSON_ELEMENT_VIEWER);
    if (!viewer.has(JSON_ELEMENT_BROADCAST_SERVICE)) {
      return Optional.empty();
    }

    final JsonObject broadcastService = viewer.getAsJsonObject(JSON_ELEMENT_BROADCAST_SERVICE);
    if (!broadcastService.has(JSON_ELEMENT_CONTAINER_TODAY)) {
      return Optional.empty();
    }

    return Optional.of(broadcastService.getAsJsonArray(JSON_ELEMENT_CONTAINER_TODAY));
  }

}
