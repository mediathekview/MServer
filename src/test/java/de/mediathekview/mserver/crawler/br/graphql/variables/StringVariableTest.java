/*
 * StringVariableTest.java
 * 
 * Projekt    : MServer
 * erstellt am: 07.12.2017
 * Autor      : Sascha
 * 
 */
package de.mediathekview.mserver.crawler.br.graphql.variables;

import static org.junit.Assert.*;
import org.junit.After;
import org.junit.Test;

public class StringVariableTest {

  @After
  public void tearDown() throws Exception {}

  @Test
  public void testNormalString() {
    StringVariable graphQLVariable = new StringVariable("broadcasterId", "av:http://ard.de/ontologies/ard#BR_Fernsehen");
    assertEquals("\"broadcasterId\":\"av:http://ard.de/ontologies/ard#BR_Fernsehen\"", graphQLVariable.getVariableOrDefaulNull());
  }

  @Test
  public void testStringWithQuotes() {
    StringVariable graphQLVariable = new StringVariable("term", "\"Fit - auch ohne Sport!\". Wie das geht");
    assertEquals("\"term\":\"\\\"Fit - auch ohne Sport!\\\". Wie das geht\"", graphQLVariable.getVariableOrDefaulNull());
  }

  @Test
  public void testNullString() {
    StringVariable graphQLVariable = new StringVariable("term", (String)null);
    assertEquals("\"term\":null", graphQLVariable.getVariableOrDefaulNull());
  }
  
}
