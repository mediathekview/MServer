/*
 * BrClipType.java
 * 
 * Projekt    : MServer
 * erstellt am: 12.12.2017
 * Autor      : Sascha
 * 
 * (c) 2017 by Sascha Wiegandt
 */
package de.mediathekview.mserver.crawler.br.data;

import java.util.Arrays;
import java.util.Optional;

public enum BrClipType {

  PROGRAMME("Programme"),
  ITEM("Item");
  
  private final String graphQLName;
  
  BrClipType(String graphQLName) {
    this.graphQLName = graphQLName;
  }
 
  public static BrClipType getInstanceByName(String name) {
    Optional<BrClipType> value = Arrays.stream(BrClipType.values()).filter(v -> v.getGraphQLName().equals(name)).findFirst();
    return value.orElse(null);
    
  }
  
  public String getGraphQLName() {
    return this.graphQLName;
  }
  
}
