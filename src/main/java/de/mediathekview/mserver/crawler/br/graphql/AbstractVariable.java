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
  protected boolean notNullType = false; // Default = false: http://facebook.github.io/graphql/October2016/#sec-Types.Non-Null 
  
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
  
  public String getJSONFromVariableOrDefaulNull() {
    return null == this.value ? getAsJSONWithoutValue() + "null" : getVariable();
  }

  public synchronized String getName() {
    return name;
  }

  public synchronized T getValue() {
    return value;
  }

  public void setAsNotNullableType() {
    this.notNullType = true;
  }
  
  public void setAsNullableType() {
    this.notNullType = false;
  }
  
  public boolean isNullableType() {
    return !this.notNullType;
  }
  
  public boolean isNotNullableType() {
    return this.notNullType;
  }
  
}
