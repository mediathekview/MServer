/*
 * StringVariable.java
 * 
 * Projekt    : MServer
 * erstellt am: 07.12.2017
 * Autor      : Sascha
 * 
 */
package de.mediathekview.mserver.crawler.br.graphql.variables;

import de.mediathekview.mserver.crawler.br.graphql.AbstractVariable;

public class StringVariable extends AbstractVariable<String> {

  public StringVariable(String name, String variable) {
    super(name, variable);
  }
  
  @Override
  protected String getVariable() {
    return getAsJSONWithoutValue() + getVariableWithDoubleQuoteSurrounding(this.value.replace("\"", "\\\""));
  }

}
