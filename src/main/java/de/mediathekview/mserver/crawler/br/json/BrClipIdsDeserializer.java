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

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Optional;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.br.data.BrClipCollectIDResult;
import de.mediathekview.mserver.crawler.br.data.BrClipType;
import de.mediathekview.mserver.crawler.br.data.BrGraphQLElementNames;
import de.mediathekview.mserver.crawler.br.data.BrGraphQLNodeNames;
import de.mediathekview.mserver.crawler.br.data.BrID;

public class BrClipIdsDeserializer  implements JsonDeserializer<BrClipCollectIDResult> {

  private final AbstractCrawler crawler;
  private BrClipCollectIDResult idCollectResult;

  public BrClipIdsDeserializer(AbstractCrawler crawler, BrClipCollectIDResult idCollectResult) {
    this.crawler = crawler;
    this.idCollectResult = idCollectResult;
  }

  /*
   * Example:
   * 
   * {
   *   "data": {
   *     "viewer": {
   *       "searchAllClips": {
   *         "count": 81930, <-- counter how many Clips where found
   *         "pageInfo": {
   *           "hasNextPage": true <-- could be true or false
   *         },
   *         "edges": [
   *           {
   *             "node": {
   *               "__typename": "Item", <-- could be Item or Programme
   *               "id": "av:584f7f303b4679001197f6b2" <-- Uniq Clip IDs
   *             },
   *             "cursor": "bW9uZ29kYmNvbm5lY3Rpb246MA\u003d\u003d" <-- Cursor to get following Pages. Caution only within the same HTTP-Session
   *           }
   *           
   */
  @Override
  public BrClipCollectIDResult deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
      throws JsonParseException {

    JsonObject rootObject = json.getAsJsonObject();
    
    Optional<JsonObject> searchAllClips = getClipIDsBaseNode(rootObject);
    if(searchAllClips.isPresent()) {
      
      Optional<Boolean> hasNextClipPage = clipIDResultsHasNextPage(searchAllClips.get());
      if(hasNextClipPage.isPresent() && hasNextClipPage.get()) {
        this.idCollectResult.setHasNextPage();
      } else {
        this.idCollectResult.setHasNonNextPage();
      }
      
      Optional<Integer> resultSize = getResultSize(searchAllClips.get());
      if(resultSize.isPresent()) {
        this.idCollectResult.setResultSize(resultSize.get());
      }
      
      Optional<JsonArray> edges = getClipIDEdges(searchAllClips.get());
      if(edges.isPresent()) {
        edges.get().forEach((JsonElement element) -> {
          if(element.isJsonObject()) {
            JsonObject singleEdge = element.getAsJsonObject();
            
            Optional<String> cursor = getCursor(singleEdge);
            if(cursor.isPresent()) {
              this.idCollectResult.setCursor(cursor.get());
            }
            
            Optional<BrID> brId = getBrId(singleEdge);
            if(brId.isPresent()) {
              this.idCollectResult.getClipList().add(brId.get());
            } 
          }
        });
      }      
      
    }
    
    return this.idCollectResult;
  }
  
  private Optional<JsonObject> getClipIDsBaseNode(JsonObject rootObject) {
    if(!rootObject.has(BrGraphQLNodeNames.RESULT_ROOT_NODE.getName())) {
      return Optional.empty();
    }
    JsonObject dataNode = rootObject.getAsJsonObject(BrGraphQLNodeNames.RESULT_ROOT_NODE.getName());

    if(!dataNode.has(BrGraphQLNodeNames.RESULT_ROOT_BR_NODE.getName())) {
      return Optional.empty();
    }
    JsonObject viewerNode = dataNode.getAsJsonObject(BrGraphQLNodeNames.RESULT_ROOT_BR_NODE.getName());
    
    if(!viewerNode.has(BrGraphQLNodeNames.RESULT_CLIP_ID_ROOT.getName()) || !viewerNode.isJsonObject()) {
      return Optional.empty();
    }
    return Optional.of(viewerNode.getAsJsonObject(BrGraphQLNodeNames.RESULT_CLIP_ID_ROOT.getName()));
      
  }
 
  private Optional<Integer> getResultSize(JsonObject searchAllClipsNode) {
    if(!searchAllClipsNode.has(BrGraphQLElementNames.INT_COUNTER_ELEMENT.getName())) {
      return Optional.empty();
    }
    JsonElement elementCount = searchAllClipsNode.getAsJsonPrimitive(BrGraphQLElementNames.INT_COUNTER_ELEMENT.getName());
    
    return Optional.of(elementCount.getAsInt());
    
  }
  
  private Optional<Boolean> clipIDResultsHasNextPage(JsonObject searchAllClipsNode) {
    if(!searchAllClipsNode.has(BrGraphQLNodeNames.RESULT_PAGE_INFO.getName()) ) {
      return Optional.empty();
    }
    JsonObject resultPageInfo = searchAllClipsNode.getAsJsonObject(BrGraphQLNodeNames.RESULT_PAGE_INFO.getName());
    
    if(!resultPageInfo.has(BrGraphQLElementNames.BOOLEAN_HAS_NEXT_PAGE.getName())) {
      return Optional.empty();
    }
    JsonElement hasNextPage = resultPageInfo.getAsJsonPrimitive(BrGraphQLElementNames.BOOLEAN_HAS_NEXT_PAGE.getName());
    
    return Optional.of(hasNextPage.getAsBoolean());
    
  }
  
  private Optional<JsonArray> getClipIDEdges(JsonObject searchAllClipsNode) {
    if(!searchAllClipsNode.has(BrGraphQLNodeNames.RESULT_NODE_EDGES.getName())) {
      return Optional.empty();
    }
    return Optional.of(searchAllClipsNode.getAsJsonArray(BrGraphQLNodeNames.RESULT_NODE_EDGES.getName()));
    
  }
  
  private Optional<BrID> getBrId(JsonObject singleEdge) {
    if(!singleEdge.has(BrGraphQLNodeNames.RESULT_NODE.getName())) {
      return Optional.empty();
    }
    JsonObject node = singleEdge.getAsJsonObject(BrGraphQLNodeNames.RESULT_NODE.getName());
    
    if(!node.has(BrGraphQLElementNames.GRAPHQL_TYPE_ELEMENT.getName()) && !node.has(BrGraphQLElementNames.ID_ELEMENT.getName())) {
      return Optional.empty();
    }
    String type = node.getAsJsonPrimitive(BrGraphQLElementNames.GRAPHQL_TYPE_ELEMENT.getName()).getAsString();
    String id = node.getAsJsonPrimitive(BrGraphQLElementNames.ID_ELEMENT.getName()).getAsString();
    
     return Optional.of(new BrID(BrClipType.getInstanceByName(type), id));
    
    
  }
  
  private Optional<String> getCursor(JsonObject singleEdge) {
    if(!singleEdge.has(BrGraphQLElementNames.STRING_CURSOR_ELEMENT.getName())) {
      return Optional.empty();
    }
    JsonElement cursor = singleEdge.get(BrGraphQLElementNames.STRING_CURSOR_ELEMENT.getName());
    
    return Optional.of(cursor.getAsString());
  }
  
}
