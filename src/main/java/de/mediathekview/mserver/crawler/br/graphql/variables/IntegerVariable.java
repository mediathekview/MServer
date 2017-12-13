/*
 * IntegerVariable.java
 * 
 * Projekt    : MServer
 * erstellt am: 07.12.2017
 * Autor      : Sascha
 * 
 * (c) 2017 by Sascha Wiegandt
 */
package de.mediathekview.mserver.crawler.br.graphql.variables;

import de.mediathekview.mserver.crawler.br.graphql.AbstractVariable;

public class IntegerVariable extends AbstractVariable<Integer> {
  
  public IntegerVariable(String name, Integer value) {
    super(name, value);
  }

  @Override
  protected String getVariable() {
    return getAsJSONWithoutValue() + this.value.toString();
  }
  
}
