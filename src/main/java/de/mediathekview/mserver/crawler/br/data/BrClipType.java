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
  
  private String graphQLName;
  
  private BrClipType(String graphQLName) {
    this.graphQLName = graphQLName;
  }
 
  public static BrClipType getInstanceByName(String name) {
    Optional<BrClipType> value = Arrays.asList(BrClipType.values()).stream().filter(v -> v.getGraphQLName().equals(name)).findFirst();
    if(value.isPresent()) {
      return value.get();
    } else {
      return null;
    }
    
  }
  
  public String getGraphQLName() {
    return this.graphQLName;
  }
  
}
