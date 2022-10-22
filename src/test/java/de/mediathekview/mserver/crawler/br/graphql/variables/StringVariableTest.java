/*
 * StringVariableTest.java
 *
 * Projekt    : MServer
 * erstellt am: 07.12.2017
 * Autor      : Sascha
 *
 */
package de.mediathekview.mserver.crawler.br.graphql.variables;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class StringVariableTest {

  @Test
  void testNormalString() {
    final StringVariable graphQLVariable =
        new StringVariable("broadcasterId", "av:http://ard.de/ontologies/ard#BR_Fernsehen");
    assertThat(graphQLVariable.getJSONFromVariableOrDefaulNull())
        .isEqualTo("\"broadcasterId\":\"av:http://ard.de/ontologies/ard#BR_Fernsehen\"");
  }

  @Test
  void testStringWithQuotes() {
    final StringVariable graphQLVariable =
        new StringVariable("term", "\"Fit - auch ohne Sport!\". Wie das geht");
    assertThat(graphQLVariable.getJSONFromVariableOrDefaulNull())
        .isEqualTo("\"term\":\"\\\"Fit - auch ohne Sport!\\\". Wie das geht\"");
  }

  @Test
  void testNullString() {
    final StringVariable graphQLVariable = new StringVariable("term", null);
    assertThat(graphQLVariable.getJSONFromVariableOrDefaulNull()).isEqualTo("\"term\":null");
  }
}
