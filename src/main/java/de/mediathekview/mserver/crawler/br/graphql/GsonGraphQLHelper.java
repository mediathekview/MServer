/*
 * GsonGraphQLHelper.java
 * 
 * Projekt    : MServer
 * erstellt am: 02.01.2018
 * Autor      : Sascha
 * 
 * (c) 2018 by Sascha Wiegandt
 */
package de.mediathekview.mserver.crawler.br.graphql;

import java.util.Optional;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class GsonGraphQLHelper {

  public static Optional<JsonObject> getChildObjectIfExists(JsonObject parentNode, String childNodeName) {
    return Optional.of(getElementIfExists(parentNode, childNodeName).filter(JsonElement::isJsonObject).map(JsonElement::getAsJsonObject)).orElse(Optional.empty());
  }
  
  public static Optional<JsonArray> getChildArrayIfExists(JsonObject parentNode, String childNodeName) {
    return Optional.of(getElementIfExists(parentNode, childNodeName).filter(JsonElement::isJsonArray).map(JsonElement::getAsJsonArray)).orElse(Optional.empty());
  }
  
  private static Optional<JsonElement> getElementIfExists(JsonObject parentNode, String childNodeName) {
    if(!parentNode.has(childNodeName))
      return Optional.empty();
    
    return Optional.of(parentNode.get(childNodeName));
  }
  
}
