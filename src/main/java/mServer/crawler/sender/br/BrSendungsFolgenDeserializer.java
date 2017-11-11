package mServer.crawler.sender.br;

import java.lang.reflect.Type;
import java.util.Optional;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import mServer.crawler.sender.MediathekReader;

public class BrSendungsFolgenDeserializer implements JsonDeserializer<BrIdsDTO> {
  private static final String JSON_ELEMENT_EDGES = "edges";
  private static final String JSON_ELEMENT_CLIPS_ONLY = "clipsOnly";
  private static final String JSON_ELEMENT_PREVIOUS_EPISODES = "previousEpisodes";
  private static final String JSON_ELEMENT_SERIES = "series";
  private static final String JSON_ELEMENT_VIEWER = "viewer";
  private static final String JSON_ELEMENT_DATA = "data";
  private static final String JSON_ELEMENT_ID = "id";
  private static final String JSON_ELEMENT_NODE = "node";
  private final MediathekReader crawler;

  public BrSendungsFolgenDeserializer(final MediathekReader aCrawler) {
    crawler = aCrawler;
  }

  /**
   * Resolves the Sendung ids which are needed to get the Sendung details.<br>
   * The data has these two structures:
   * <code>data -> viewer -> series -> clipsOnly -> edges[] -> node -> id</code><br>
   * <code>data -> viewer -> series -> previousEpisodes -> edges[] -> node -> id</code>
   */
  @Override
  public BrIdsDTO deserialize(final JsonElement aElement, final Type aType,
      final JsonDeserializationContext aContext) {
    final BrIdsDTO results = new BrIdsDTO();

    final JsonObject baseObject = aElement.getAsJsonObject();

    final Optional<JsonObject> series = getSeries(baseObject);

    if (series.isPresent()) {
      final Optional<JsonArray> clipsEdges = getClipsEdges(series.get());
      addToResult(results, clipsEdges);

      final Optional<JsonArray> previosEpisodesEdges = getPreviousEpisodesEdges(series.get());
      addToResult(results, previosEpisodesEdges);
    }

    return results;
  }

  private void addToResult(final BrIdsDTO results, final Optional<JsonArray> aEdges) {
    if (aEdges.isPresent()) {
      for (final JsonElement edge : aEdges.get()) {
        final JsonObject ebdgeObj = edge.getAsJsonObject();
        if (ebdgeObj.has(JSON_ELEMENT_NODE)) {
          final JsonObject node = ebdgeObj.getAsJsonObject(JSON_ELEMENT_NODE);
          if (node.has(JSON_ELEMENT_ID)) {
            results.add(node.get(JSON_ELEMENT_ID).getAsString());
          }
        }
      }
    }
  }

  private Optional<JsonArray> getClipsEdges(final JsonObject aSeries) {
    return getEdges(aSeries, JSON_ELEMENT_CLIPS_ONLY);
  }

  private Optional<JsonArray> getEdges(final JsonObject aSeries,
      final String aEdgesParentElementId) {
    if (!aSeries.has(aEdgesParentElementId)) {
      return Optional.empty();
    }

    final JsonObject edgesParent = aSeries.getAsJsonObject(aEdgesParentElementId);
    if (!edgesParent.has(JSON_ELEMENT_EDGES)) {
      return Optional.empty();
    }
    return Optional.of(edgesParent.getAsJsonArray(JSON_ELEMENT_EDGES));
  }

  private Optional<JsonArray> getPreviousEpisodesEdges(final JsonObject aSeries) {
    return getEdges(aSeries, JSON_ELEMENT_PREVIOUS_EPISODES);
  }

  private Optional<JsonObject> getSeries(final JsonObject aBaseObject) {
    if (!aBaseObject.has(JSON_ELEMENT_DATA)) {
      return Optional.empty();
    }

    final JsonObject data = aBaseObject.getAsJsonObject(JSON_ELEMENT_DATA);
    if (!data.has(JSON_ELEMENT_VIEWER)) {
      return Optional.empty();
    }

    final JsonObject viewer = data.getAsJsonObject(JSON_ELEMENT_VIEWER);
    if (!viewer.has(JSON_ELEMENT_SERIES)) {
      return Optional.empty();
    }

    return Optional.of(viewer.getAsJsonObject(JSON_ELEMENT_SERIES));

  }
}
