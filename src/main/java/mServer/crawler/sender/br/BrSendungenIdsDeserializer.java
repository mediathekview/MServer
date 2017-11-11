package mServer.crawler.sender.br;

import java.lang.reflect.Type;
import java.util.Optional;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class BrSendungenIdsDeserializer implements JsonDeserializer<BrIdsDTO> {
  private static final String JSON_ELEMENT_EDGES = "edges";
  private static final String JSON_ELEMENT_SERIES_INDEX_ALL_SERIES = "seriesIndexAllSeries";
  private static final String JSON_ELEMENT_VIEWER = "viewer";
  private static final String JSON_ELEMENT_DATA = "data";
  private static final String JSON_ELEMENT_ID = "id";
  private static final String JSON_ELEMENT_NODE = "node";

  /**
   * Resolves the Sendung ids which are needed to get the Sendung details.<br>
   * The data has this structure:
   * <code>data -> viewer -> seriesIndexAllSeries -> edges[] -> node -> id</code>
   */
  @Override
  public BrIdsDTO deserialize(final JsonElement aElement, final Type aType,
      final JsonDeserializationContext aContext) {
    final BrIdsDTO results = new BrIdsDTO();

    final JsonObject baseObject = aElement.getAsJsonObject();
    final Optional<JsonArray> edges = getEdges(baseObject);
    if (edges.isPresent()) {
      for (final JsonElement edge : edges.get()) {
        final JsonObject ebdgeObj = edge.getAsJsonObject();
        if (ebdgeObj.has(JSON_ELEMENT_NODE)) {
          final JsonObject node = ebdgeObj.getAsJsonObject(JSON_ELEMENT_NODE);
          if (node.has(JSON_ELEMENT_ID)) {
            results.add(node.get(JSON_ELEMENT_ID).getAsString());
          }
        }
      }
    }

    return results;
  }

  private Optional<JsonArray> getEdges(final JsonObject aBaseObject) {
    if (!aBaseObject.has(JSON_ELEMENT_DATA)) {
      return Optional.empty();
    }

    final JsonObject data = aBaseObject.getAsJsonObject(JSON_ELEMENT_DATA);
    if (!data.has(JSON_ELEMENT_VIEWER)) {
      return Optional.empty();
    }

    final JsonObject viewer = data.getAsJsonObject(JSON_ELEMENT_VIEWER);
    if (!viewer.has(JSON_ELEMENT_SERIES_INDEX_ALL_SERIES)) {
      return Optional.empty();
    }

    final JsonObject seriesIndexAllSeries =
        viewer.getAsJsonObject(JSON_ELEMENT_SERIES_INDEX_ALL_SERIES);
    if (!seriesIndexAllSeries.has(JSON_ELEMENT_EDGES)) {
      return Optional.empty();
    }

    return Optional.of(seriesIndexAllSeries.getAsJsonArray(JSON_ELEMENT_EDGES));
  }

}
