/*
 * BooleanVariableTest.java
 * 
 * Projekt    : MServer
 * erstellt am: 07.12.2017
 * Autor      : Sascha
 * 
 * (c) 2017 by Sascha Wiegandt
 */
package de.mediathekview.mserver.crawler.br.graphql.variables;

import static org.junit.Assert.*;
import org.junit.After;
import org.junit.Test;

public class BooleanVariableTest {

  @After
  public void tearDown() throws Exception {}

  @Test
  public void testBooleanVariableIsTrue() {
    BooleanVariable graphQLVariable = new BooleanVariable("triggerSearch", true);
    assertEquals("\"triggerSearch\":true", graphQLVariable.getVariableOrDefaulNull());
  }

  @Test
  public void testBooleanVariableIsFalse() {
    BooleanVariable graphQLVariable = new BooleanVariable("eq", false);
    assertEquals("\"eq\":false", graphQLVariable.getVariableOrDefaulNull());
  }

  @Test
  public void testBooleanVariableIsNull() {
    BooleanVariable graphQLVariable = new BooleanVariable("eq", (Boolean)null);
    assertEquals("\"eq\":null", graphQLVariable.getVariableOrDefaulNull());
  }

}
