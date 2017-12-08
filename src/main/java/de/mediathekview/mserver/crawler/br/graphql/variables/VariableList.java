/*
 * VariableList.java
 * 
 * Projekt    : MServer
 * erstellt am: 07.12.2017
 * Autor      : Sascha
 * 
 * (c) 2017 by Sascha Wiegandt
 */
package de.mediathekview.mserver.crawler.br.graphql.variables;

import java.util.List;
import java.util.stream.Collectors;
import org.apache.logging.log4j.util.Strings;
import de.mediathekview.mserver.crawler.br.graphql.AbstractVariable;

public class VariableList extends AbstractVariable<List<AbstractVariable>> {

  public VariableList(String name, List<AbstractVariable> values) {
    super(name, values);
    if(Strings.isBlank(name))
      this.name = "variables";
  }

  public VariableList(List<AbstractVariable> values) {
    this(null, values);
  }
  
  @Override
  protected String getVariable() {
    return getAsJSONWithoutValue() + getVariableWithCurlyBracketsSurrounding(this.value.stream().map(variableListElement -> variableListElement.getVariableOrDefaulNull()).collect(Collectors.joining(",")));
  }
  
  
  
}
