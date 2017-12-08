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

import static org.junit.Assert.*;
import org.junit.After;
import org.junit.Test;

public class RecursiveAbstractVariableTest {

  @After
  public void tearDown() throws Exception {}

  @Test
  public void testOneRecursion() {
    BooleanVariable eqFalseVariable = new BooleanVariable("eq", false);
    RecursiveAbstractVariable emptyEqFalseVariable = new RecursiveAbstractVariable("empty", eqFalseVariable);
    assertEquals("\"empty\":{\"eq\":false}", emptyEqFalseVariable.getVariableOrDefaulNull());
  }

  @Test
  public void testDoubleRecursion() {
    BooleanVariable eqFalseVariable = new BooleanVariable("eq", false);
    RecursiveAbstractVariable emptyEqFalseVariable = new RecursiveAbstractVariable("empty", eqFalseVariable);
    RecursiveAbstractVariable essencesEmptyEqFalseVariable = new RecursiveAbstractVariable("essences", emptyEqFalseVariable);
    assertEquals("\"essences\":{\"empty\":{\"eq\":false}}", essencesEmptyEqFalseVariable.getVariableOrDefaulNull());
  }
  
  @Test
  public void testNullObject() {
    BooleanVariable booleanVariable = null;
    RecursiveAbstractVariable variableWithBooleanNull = new RecursiveAbstractVariable("eq", booleanVariable);
    assertEquals("\"eq\":null", variableWithBooleanNull.getVariableOrDefaulNull());
  }
  
}
