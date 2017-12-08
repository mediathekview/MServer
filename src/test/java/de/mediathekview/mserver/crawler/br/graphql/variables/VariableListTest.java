/*
 * VariableListTest.java
 * 
 * Projekt    : MServer
 * erstellt am: 08.12.2017
 * Autor      : Sascha
 * 
 * (c) 2017 by Sascha Wiegandt
 */
package de.mediathekview.mserver.crawler.br.graphql.variables;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.junit.After;
import org.junit.Test;
import de.mediathekview.mserver.crawler.br.graphql.AbstractVariable;

public class VariableListTest {

  @After
  public void tearDown() throws Exception {}

  @Test
  public void testListWithOneSimpleElementConstructorWithOneParam() {
    BooleanVariable triggerSearchTrue = new BooleanVariable("triggerSearch", true);
    List<AbstractVariable> simpleList = new LinkedList<>();
    simpleList.add(triggerSearchTrue);
    VariableList simpleListContainer = new VariableList(simpleList);
    
    assertEquals("\"variables\":{\"triggerSearch\":true}", simpleListContainer.getVariableOrDefaulNull());
  }
  
  @Test
  public void testNullList() {
    VariableList nullList = new VariableList("nullVariable", (List<AbstractVariable>) null);
    assertEquals("\"nullVariable\":null", nullList.getVariableOrDefaulNull());
  }
  
  
  @Test
  public void testRealBRExample() {
    
    BooleanVariable eqFalse = new BooleanVariable("eq", false);
    RecursiveAbstractVariable emptyEqFalse = new RecursiveAbstractVariable("empty", eqFalse);
    RecursiveAbstractVariable essencesEmptyEqFalse = new RecursiveAbstractVariable("essences", emptyEqFalse);
    
    RecursiveAbstractVariable audioOnlyEqFalse = new RecursiveAbstractVariable("audioOnly", eqFalse);
    
    List<AbstractVariable> clipFilterList = new LinkedList<>();
    clipFilterList.add(audioOnlyEqFalse);
    clipFilterList.add(essencesEmptyEqFalse);
    VariableList clipFilter = new VariableList("clipFilter", clipFilterList);
    
    IntegerVariable clipCount = new IntegerVariable("clipCount", 5000);
    BooleanVariable triggerSearch = new BooleanVariable("triggerSearch", true);
    
    List<AbstractVariable> variablesList = new LinkedList<>();
    variablesList.add(triggerSearch);
    variablesList.add(clipCount);
    variablesList.add(clipFilter);
    
    VariableList variables = new VariableList(variablesList);
    
    assertEquals("\"variables\":{\"triggerSearch\":true,\"clipCount\":5000,\"clipFilter\":{\"audioOnly\":{\"eq\":false},\"essences\":{\"empty\":{\"eq\":false}}}}", variables.getVariableOrDefaulNull());
    
  }

}
