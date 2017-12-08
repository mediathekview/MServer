/*
 * FloatVariable.java
 * 
 * Projekt    : MServer
 * erstellt am: 07.12.2017
 * Autor      : Sascha
 * 
 * (c) 2017 by Sascha Wiegandt
 */
package de.mediathekview.mserver.crawler.br.graphql.variables;

import de.mediathekview.mserver.crawler.br.graphql.AbstractVariable;

public class FloatVariable extends AbstractVariable<Double> {

  public FloatVariable(String name, Double value) {
    super(name, value);
  }

  @Override
  protected String getVariable() {
    return getAsJSONWithoutValue() + this.value.toString();
  }
  
  
  
}
