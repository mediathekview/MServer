package de.mediathekview.mserver.crawler.br.json;

import com.google.gson.*;
import de.mediathekview.mserver.base.utils.JsonUtils;
import de.mediathekview.mserver.crawler.br.data.*;
import de.mediathekview.mserver.crawler.br.graphql.GsonGraphQLHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Type;
import java.util.Optional;

public class BrProgramIdsDeserializer implements JsonDeserializer<BrClipCollectIDResult> {

  private static final Logger LOG = LogManager.getLogger(BrProgramIdsDeserializer.class);

  @Override
  public BrClipCollectIDResult deserialize(
      final JsonElement json, final Type typeOfT, final JsonDeserializationContext context)
      throws JsonParseException {

    final BrClipCollectIDResult idCollectResult = new BrClipCollectIDResult();

    final Optional<JsonObject> searchAllClips = getClipIDsBaseNode(json);
    if (searchAllClips.isPresent()) {

      final boolean hasNextClipPage = clipIdResultsHasNextPage(searchAllClips.get());
      if (hasNextClipPage) {
        idCollectResult.setHasNextPage();
      } else {
        idCollectResult.setHasNonNextPage();
      }

      final Optional<Integer> resultSize = getResultSize(searchAllClips.get());
      resultSize.ifPresent(idCollectResult::setResultSize);

      final Optional<JsonArray> edges = getClipIdEdges(searchAllClips.get());
      edges.ifPresent(
          jsonElements ->
              jsonElements.forEach(
                  (JsonElement element) -> {
                    if (element.isJsonObject()) {
                      final JsonObject singleEdge = element.getAsJsonObject();

                      if (getVideoCount(singleEdge) > 0) {
                        final Optional<String> cursor = getCursor(singleEdge);
                        cursor.ifPresent(idCollectResult::setCursor);

                        final Optional<BrID> brId = getBrId(singleEdge);
                        brId.ifPresent(brID -> idCollectResult.getClipList().add(brID));
                      }
                    }
                  }));
    }

    return idCollectResult;
  }

  private int getVideoCount(final JsonElement json) {
    if (!JsonUtils.checkTreePath(
        json,
        null,
        BrGraphQLNodeNames.RESULT_NODE.getName(),
        BrGraphQLNodeNames.RESULT_CLIP_VIDEO_FILES.getName())) {
      return 0;
    }

    final JsonObject videoFiles =
        json.getAsJsonObject()
            .get(BrGraphQLNodeNames.RESULT_NODE.getName())
            .getAsJsonObject()
            .get(BrGraphQLNodeNames.RESULT_CLIP_VIDEO_FILES.getName())
            .getAsJsonObject();
    if (videoFiles.has(BrGraphQLElementNames.INT_COUNTER_ELEMENT.getName())) {
      return videoFiles.get(BrGraphQLElementNames.INT_COUNTER_ELEMENT.getName()).getAsInt();
    }
    return 0;
  }

  private Optional<JsonObject> getClipIDsBaseNode(final JsonElement json) {

    if (!JsonUtils.checkTreePath(
        json,
        null,
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

  private Optional<Integer> getResultSize(final JsonObject searchAllClipsNode) {
    final Optional<JsonPrimitive> searchAllClipsNodeOptional =
        GsonGraphQLHelper.getChildPrimitiveIfExists(
            searchAllClipsNode, BrGraphQLElementNames.INT_COUNTER_ELEMENT.getName());
    if (searchAllClipsNodeOptional.isEmpty()) {
      return Optional.empty();
    }
    final JsonPrimitive elementCount = searchAllClipsNodeOptional.get();

    if (!elementCount.isNumber()) {
      return Optional.empty();
    }

    return Optional.of(elementCount.getAsInt());
  }

  private boolean clipIdResultsHasNextPage(final JsonObject searchAllClipsNode) {
    final Optional<JsonObject> searchAllClipsNodeOptional =
        GsonGraphQLHelper.getChildObjectIfExists(
            searchAllClipsNode, BrGraphQLNodeNames.RESULT_PAGE_INFO.getName());
    if (searchAllClipsNodeOptional.isEmpty()) {
      return false;
    }
    final JsonObject resultPageInfo = searchAllClipsNodeOptional.get();

    final Optional<JsonPrimitive> hasNextPageOptional =
        GsonGraphQLHelper.getChildPrimitiveIfExists(
            resultPageInfo, BrGraphQLElementNames.BOOLEAN_HAS_NEXT_PAGE.getName());
    if (hasNextPageOptional.isEmpty()) {
      return false;
    }

    final JsonPrimitive hasNextPage = hasNextPageOptional.get();
    if (!hasNextPage.isBoolean()) {
      return false;
    }

    return hasNextPage.getAsBoolean();
  }

  private Optional<JsonArray> getClipIdEdges(final JsonObject searchAllClipsNode) {
    return GsonGraphQLHelper.getChildArrayIfExists(
        searchAllClipsNode, BrGraphQLNodeNames.RESULT_NODE_EDGES.getName());
  }

  private Optional<BrID> getBrId(final JsonObject singleEdge) {
    if (!singleEdge.has(BrGraphQLNodeNames.RESULT_NODE.getName())) {
      return Optional.empty();
    }
    final JsonObject node = singleEdge.getAsJsonObject(BrGraphQLNodeNames.RESULT_NODE.getName());

    if (!node.has(BrGraphQLElementNames.GRAPHQL_TYPE_ELEMENT.getName())
        && !node.has(BrGraphQLElementNames.ID_ELEMENT.getName())) {
      return Optional.empty();
    }
    final String type =
        node.getAsJsonPrimitive(BrGraphQLElementNames.GRAPHQL_TYPE_ELEMENT.getName()).getAsString();
    final String id =
        node.getAsJsonPrimitive(BrGraphQLElementNames.ID_ELEMENT.getName()).getAsString();

    return Optional.of(new BrID(BrClipType.getInstanceByName(type), id));
  }

  private Optional<String> getCursor(final JsonObject singleEdge) {
    if (!singleEdge.has(BrGraphQLElementNames.STRING_CURSOR_ELEMENT.getName())) {
      return Optional.empty();
    }
    final JsonElement cursor =
        singleEdge.get(BrGraphQLElementNames.STRING_CURSOR_ELEMENT.getName());

    return Optional.of(cursor.getAsString());
  }
}
