/*
 * AbstraceVariable.java
 * 
 * Projekt    : MServer
 * erstellt am: 03.12.2017
 * Autor      : Sascha
 * 
 */
package de.mediathekview.mserver.crawler.br.graphql;

public abstract class AbstractVariable<T> {
  
  protected String name;
  protected T value;
  
  public AbstractVariable(String name, T value) {
    this.name = name;
    this.value = value;
  }

  protected abstract String getVariable();
  
  protected String getVariableWithDoubleQuoteSurrounding(String content) {
    return "\"" + content + "\"";
  }
  
  protected String getVariableWithCurlyBracketsSurrounding(String content) {
    return "{" + content + "}";
  }
  
  protected String getAsJSONWithoutValue() {
    return getVariableWithDoubleQuoteSurrounding(this.name) + ":";
  }
  
  public String getVariableOrDefaulNull() {
    return null == this.value ? getAsJSONWithoutValue() + "null" : getVariable();
  }
  
}
