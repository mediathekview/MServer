/*
 * IntegerVariableTest.java
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

public class IntegerVariableTest {

  @After
  public void tearDown() throws Exception {}

  @Test
  public void testIntegerVariableWithoutSign() {
    IntegerVariable graphQLVariable = new IntegerVariable("clipCount", 24);
    assertEquals("\"clipCount\":24", graphQLVariable.getJSONFromVariableOrDefaulNull());
  }

  @Test
  public void testIntegerVariableWithNegativeSign() {
    IntegerVariable graphQLVariable = new IntegerVariable("seriesCount", -12);
    assertEquals("\"seriesCount\":-12", graphQLVariable.getJSONFromVariableOrDefaulNull());
  }

  @Test
  public void testIntegerVariableWithPostiveSign() {
    IntegerVariable graphQLVariable = new IntegerVariable("height", +180);
    assertEquals("\"height\":180", graphQLVariable.getJSONFromVariableOrDefaulNull());
  }

  @Test
  public void testIntegerVariableWithNull() {
    IntegerVariable graphQLVariable = new IntegerVariable("height", (Integer)null);
    assertEquals("\"height\":null", graphQLVariable.getJSONFromVariableOrDefaulNull());
  }
  

}
