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

import org.junit.After;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class BooleanVariableTest {

  @After
  public void tearDown() throws Exception {}

  @Test
  public void testBooleanVariableIsTrue() {
      final BooleanVariable graphQLVariable = new BooleanVariable("triggerSearch", true);
    assertEquals("\"triggerSearch\":true", graphQLVariable.getJSONFromVariableOrDefaulNull());
  }

  @Test
  public void testBooleanVariableIsFalse() {
      final BooleanVariable graphQLVariable = new BooleanVariable("eq", false);
    assertEquals("\"eq\":false", graphQLVariable.getJSONFromVariableOrDefaulNull());
  }

  @Test
  public void testBooleanVariableIsNull() {
      final BooleanVariable graphQLVariable = new BooleanVariable("eq", null);
    assertEquals("\"eq\":null", graphQLVariable.getJSONFromVariableOrDefaulNull());
  }
}
