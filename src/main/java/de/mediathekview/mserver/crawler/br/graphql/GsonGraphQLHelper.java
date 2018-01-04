/*
 * GsonGraphQLHelper.java
 * 
 * Projekt    : MServer
 * erstellt am: 02.01.2018
 * Autor      : Sascha
 * 
 */
package de.mediathekview.mserver.crawler.br.graphql;

import java.util.Optional;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import de.mediathekview.mserver.crawler.br.data.BrGraphQLNodeNames;

public class GsonGraphQLHelper {
  
  private GsonGraphQLHelper() {
    
  }

  public static Optional<JsonObject> getChildObjectIfExists(JsonObject parentNode, String childNodeName) {
    return Optional.of(getElementIfExists(parentNode, childNodeName).filter(JsonElement::isJsonObject).map(JsonElement::getAsJsonObject)).orElse(Optional.empty());
  }
  
  public static Optional<JsonArray> getChildArrayIfExists(JsonObject parentNode, String childNodeName) {
    return Optional.of(getElementIfExists(parentNode, childNodeName).filter(JsonElement::isJsonArray).map(JsonElement::getAsJsonArray)).orElse(Optional.empty());
  }
  
  public static Optional<JsonPrimitive> getChildPrimitiveIfExists(JsonObject parentNode, String childNodeName) {
    return Optional.of(getElementIfExists(parentNode, childNodeName).filter(JsonElement::isJsonPrimitive).map(JsonElement::getAsJsonPrimitive)).orElse(Optional.empty());
  }
  
  public static boolean checkForErrors(JsonObject rootObject) {
    return getChildObjectIfExists(rootObject, BrGraphQLNodeNames.RESULT_ERRORS_NODE.getName()).isPresent();
  }
  
  private static Optional<JsonElement> getElementIfExists(JsonObject parentNode, String childNodeName) {
    if(!parentNode.has(childNodeName))
      return Optional.empty();
    
    return Optional.of(parentNode.get(childNodeName));
  }
  
  
  
}
