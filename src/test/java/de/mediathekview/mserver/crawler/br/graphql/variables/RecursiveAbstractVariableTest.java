/*
 * RecursiveAbstractVariableTest.java
 * 
 * Projekt    : MServer
 * erstellt am: 08.12.2017
 * Autor      : Sascha
 * 
 * (c) 2017 by Sascha Wiegandt
 */
package de.mediathekview.mserver.crawler.br.graphql.variables;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.util.LinkedList;
import java.util.List;
import org.junit.After;
import org.junit.Test;
import de.mediathekview.mserver.crawler.br.graphql.AbstractVariable;

public class RecursiveAbstractVariableTest {

  @After
  public void tearDown() throws Exception {}

  @Test
  public void testOneRecursion() {
    BooleanVariable eqFalseVariable = new BooleanVariable("eq", false);
    RecursiveAbstractVariable emptyEqFalseVariable = new RecursiveAbstractVariable("empty", eqFalseVariable);
    assertEquals("\"empty\":{\"eq\":false}", emptyEqFalseVariable.getJSONFromVariableOrDefaulNull());
  }

  @Test
  public void testDoubleRecursion() {
    BooleanVariable eqFalseVariable = new BooleanVariable("eq", false);
    RecursiveAbstractVariable emptyEqFalseVariable = new RecursiveAbstractVariable("empty", eqFalseVariable);
    RecursiveAbstractVariable essencesEmptyEqFalseVariable = new RecursiveAbstractVariable("essences", emptyEqFalseVariable);
    assertEquals("\"essences\":{\"empty\":{\"eq\":false}}", essencesEmptyEqFalseVariable.getJSONFromVariableOrDefaulNull());
  }
  
  @Test
  public void testNullObject() {
    BooleanVariable booleanVariable = null;
    RecursiveAbstractVariable variableWithBooleanNull = new RecursiveAbstractVariable("eq", booleanVariable);
    assertEquals("\"eq\":null", variableWithBooleanNull.getJSONFromVariableOrDefaulNull());
  }
  
  @Test
  public void testSubListSetsStatusRootNodeToFalse() {
    BooleanVariable bv = new BooleanVariable("bv", false);
    StringVariable sv = new StringVariable("sv", "text");
    List<AbstractVariable> subList = new LinkedList<>();
    subList.add(bv);
    subList.add(sv);
    
    VariableList vl = new VariableList("SubList", subList);
    
    assertTrue(vl.isRootElement()); 
    
    RecursiveAbstractVariable rv = new RecursiveAbstractVariable("father", vl);
    
    if(rv.getValue() instanceof VariableList) {
      VariableList sub = (VariableList)rv.getValue();
      sub.getValue().stream().filter(VariableList.class::isInstance).map(VariableList.class::cast).forEach(v -> assertFalse(v.isRootElement()));
    }
    
    
    
  }
  
}
