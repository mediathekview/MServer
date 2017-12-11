/*
 * IDVariable.java
 * 
 * Projekt    : MServer
 * erstellt am: 10.12.2017
 * Autor      : Sascha
 * 
 */
package de.mediathekview.mserver.crawler.br.graphql.variables;

/**
 * A Special Type of StringVariable, where at GraphQL Headers the Type will be "ID"
 * @author Sascha
 *
 */
public class IDVariable extends StringVariable {

  public IDVariable(String name, String value) {
    super(name, value);
  }
  
}
