/*
 * RecursiveAbstractVariable.java
 * 
 * Projekt    : MServer
 * erstellt am: 08.12.2017
 * Autor      : Sascha
 * 
 * (c) 2017 by Sascha Wiegandt
 */
package de.mediathekview.mserver.crawler.br.graphql.variables;

import de.mediathekview.mserver.crawler.br.graphql.AbstractVariable;

public class RecursiveAbstractVariable extends AbstractVariable<AbstractVariable> {
  
  public RecursiveAbstractVariable(String name, AbstractVariable object) {
    super(name, object);
  }

  @Override
  protected String getVariable() {
    return getAsJSONWithoutValue() + getVariableWithCurlyBracketsSurrounding(this.value.getVariableOrDefaulNull());
  }

  
  
}
