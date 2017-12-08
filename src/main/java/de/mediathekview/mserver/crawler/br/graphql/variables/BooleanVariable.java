/*
 * BooleanVariable.java
 * 
 * Projekt    : MServer
 * erstellt am: 03.12.2017
 * Autor      : Sascha
 * 
 */
package de.mediathekview.mserver.crawler.br.graphql.variables;

import de.mediathekview.mserver.crawler.br.graphql.AbstractVariable;

public class BooleanVariable extends AbstractVariable<Boolean> {

  public BooleanVariable(String name, Boolean value) {
    super(name, value);
  }
  
  
  @Override
  protected String getVariable() {
    return getAsJSONWithoutValue() + this.value.toString();
  }
  
}
