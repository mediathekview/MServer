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
import org.apache.commons.lang3.StringUtils;
import de.mediathekview.mserver.crawler.br.graphql.AbstractVariable;

public class VariableList extends AbstractVariable<List<AbstractVariable>> {

  private boolean isRootNode = false;
  
  public VariableList(String name, List<AbstractVariable> values) {
    super(name, values);
    if(StringUtils.isBlank(name)) { 
      this.name = "variables";
    }
    this.isRootNode = true;
    changeAllChildElementsToBeNoRootElement();
  }

  public VariableList(List<AbstractVariable> values) {
    this(null, values);
  }
  
  @Override
  protected String getVariable() {
    return getAsJSONWithoutValue() + getVariableWithCurlyBracketsSurrounding(this.value.stream().map(variableListElement -> variableListElement.getJSONFromVariableOrDefaulNull()).collect(Collectors.joining(",")));
  }
  
  private void changeAllChildElementsToBeNoRootElement() {
    if(null != this.value) {
      this.value.stream()
                .filter(VariableList.class::isInstance)
                .map(VariableList.class::cast)
                .forEach((VariableList v) -> v.setNodeType2NotRoot());
    }
  }
  
  protected void setNodeType2NotRoot() {
    this.isRootNode = false;
  }
  

  public boolean isRootElement() {
    return this.isRootNode;
  }
  
}