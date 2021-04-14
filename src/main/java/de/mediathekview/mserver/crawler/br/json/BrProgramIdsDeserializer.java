/*
 * BrClipIdsDeserializer.java
 *
 * Projekt    : MServer
 * erstellt am: 12.12.2017
 * Autor      : Sascha
 *
 * (c) 2017 by Sascha Wiegandt
 */

package de.mediathekview.mserver.crawler.br.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import de.mediathekview.mserver.base.utils.JsonUtils;
import de.mediathekview.mserver.crawler.br.data.BrClipCollectIDResult;
import de.mediathekview.mserver.crawler.br.data.BrClipType;
import de.mediathekview.mserver.crawler.br.data.BrGraphQLElementNames;
import de.mediathekview.mserver.crawler.br.data.BrGraphQLNodeNames;
import de.mediathekview.mserver.crawler.br.data.BrID;
import de.mediathekview.mserver.crawler.br.graphql.GsonGraphQLHelper;
import java.lang.reflect.Type;
import java.util.Optional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BrProgramIdsDeserializer implements JsonDeserializer<BrClipCollectIDResult> {

  private static final Logger LOG = LogManager.getLogger(BrProgramIdsDeserializer.class);

  @Override
  public BrClipCollectIDResult deserialize(
      JsonElement json, Type typeOfT, JsonDeserializationContext context)
      throws JsonParseException {

    BrClipCollectIDResult idCollectResult = new BrClipCollectIDResult();

    Optional<JsonObject> searchAllClips = getClipIDsBaseNode(json);
    if (searchAllClips.isPresent()) {

      boolean hasNextClipPage = clipIdResultsHasNextPage(searchAllClips.get());
      if (hasNextClipPage) {
        idCollectResult.setHasNextPage();
      } else {
        idCollectResult.setHasNonNextPage();
      }

      Optional<Integer> resultSize = getResultSize(searchAllClips.get());
      resultSize.ifPresent(idCollectResult::setResultSize);

      Optional<JsonArray> edges = getClipIdEdges(searchAllClips.get());
      edges.ifPresent(jsonElements -> jsonElements
          .forEach(
              (JsonElement element) -> {
                if (element.isJsonObject()) {
                  JsonObject singleEdge = element.getAsJsonObject();

                  Optional<String> cursor = getCursor(singleEdge);
                  cursor.ifPresent(idCollectResult::setCursor);

                  Optional<BrID> brId = getBrId(singleEdge);
                  brId.ifPresent(brID -> idCollectResult.getClipList().add(brID));
                }
              }));
    }

    return idCollectResult;
  }

  private Optional<JsonObject> getClipIDsBaseNode(JsonElement json) {

    if (!JsonUtils.checkTreePath(
        json,
        Optional.empty(),
        BrGraphQLNodeNames.RESULT_ROOT_NODE.getName(),
        BrGraphQLNodeNames.RESULT_ROOT_BR_NODE.getName(),
        BrGraphQLNodeNames.RESULT_CLIP_BROADCASTSERVICE_ROOT.getName(),
        BrGraphQLNodeNames.RESULT_CLIP_PROGRAMMES_ROOT.getName())) {
      LOG.error(
          "one of the following elements is missing {}, {}, {}, {}",
          BrGraphQLNodeNames.RESULT_ROOT_NODE.getName(),
          BrGraphQLNodeNames.RESULT_ROOT_BR_NODE.getName(),
          BrGraphQLNodeNames.RESULT_CLIP_BROADCASTSERVICE_ROOT.getName(),
          BrGraphQLNodeNames.RESULT_CLIP_PROGRAMMES_ROOT.getName());
      return Optional.empty();
    }

    final JsonObject programmesObject =
        json.getAsJsonObject()
            .get(BrGraphQLNodeNames.RESULT_ROOT_NODE.getName())
            .getAsJsonObject()
            .get(BrGraphQLNodeNames.RESULT_ROOT_BR_NODE.getName())
            .getAsJsonObject()
            .get(BrGraphQLNodeNames.RESULT_CLIP_BROADCASTSERVICE_ROOT.getName())
            .getAsJsonObject()
            .get(BrGraphQLNodeNames.RESULT_CLIP_PROGRAMMES_ROOT.getName())
            .getAsJsonObject();

    if (programmesObject.isJsonNull()) {
      return Optional.empty();
    }
    return Optional.of(programmesObject);
  }

  private Optional<Integer> getResultSize(JsonObject searchAllClipsNode) {
    Optional<JsonPrimitive> searchAllClipsNodeOptional =
        GsonGraphQLHelper.getChildPrimitiveIfExists(
            searchAllClipsNode, BrGraphQLElementNames.INT_COUNTER_ELEMENT.getName());
    if (searchAllClipsNodeOptional.isEmpty()) {
      return Optional.empty();
    }
    JsonPrimitive elementCount = searchAllClipsNodeOptional.get();

    if (!elementCount.isNumber()) {
      return Optional.empty();
    }

    return Optional.of(elementCount.getAsInt());
  }

  private boolean clipIdResultsHasNextPage(JsonObject searchAllClipsNode) {
    Optional<JsonObject> searchAllClipsNodeOptional =
        GsonGraphQLHelper.getChildObjectIfExists(
            searchAllClipsNode, BrGraphQLNodeNames.RESULT_PAGE_INFO.getName());
    if (searchAllClipsNodeOptional.isEmpty()) {
      return false;
    }
    JsonObject resultPageInfo = searchAllClipsNodeOptional.get();

    Optional<JsonPrimitive> hasNextPageOptional =
        GsonGraphQLHelper.getChildPrimitiveIfExists(
            resultPageInfo, BrGraphQLElementNames.BOOLEAN_HAS_NEXT_PAGE.getName());
    if (hasNextPageOptional.isEmpty()) {
      return false;
    }

    JsonPrimitive hasNextPage = hasNextPageOptional.get();
    if (!hasNextPage.isBoolean()) {
      return false;
    }

    return hasNextPage.getAsBoolean();
  }

  private Optional<JsonArray> getClipIdEdges(JsonObject searchAllClipsNode) {
    return GsonGraphQLHelper.getChildArrayIfExists(
            searchAllClipsNode, BrGraphQLNodeNames.RESULT_NODE_EDGES.getName());
  }

  private Optional<BrID> getBrId(JsonObject singleEdge) {
    if (!singleEdge.has(BrGraphQLNodeNames.RESULT_NODE.getName())) {
      return Optional.empty();
    }
    JsonObject node = singleEdge.getAsJsonObject(BrGraphQLNodeNames.RESULT_NODE.getName());

    if (!node.has(BrGraphQLElementNames.GRAPHQL_TYPE_ELEMENT.getName())
        && !node.has(BrGraphQLElementNames.ID_ELEMENT.getName())) {
      return Optional.empty();
    }
    String type =
        node.getAsJsonPrimitive(BrGraphQLElementNames.GRAPHQL_TYPE_ELEMENT.getName()).getAsString();
    String id = node.getAsJsonPrimitive(BrGraphQLElementNames.ID_ELEMENT.getName()).getAsString();

    return Optional.of(new BrID(BrClipType.getInstanceByName(type), id));
  }

  private Optional<String> getCursor(JsonObject singleEdge) {
    if (!singleEdge.has(BrGraphQLElementNames.STRING_CURSOR_ELEMENT.getName())) {
      return Optional.empty();
    }
    JsonElement cursor = singleEdge.get(BrGraphQLElementNames.STRING_CURSOR_ELEMENT.getName());

    return Optional.of(cursor.getAsString());
  }
}
