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

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BooleanVariableTest {

  @Test
  void testBooleanVariableIsTrue() {
    final BooleanVariable graphQLVariable = new BooleanVariable("triggerSearch", true);
    assertThat(graphQLVariable.getJSONFromVariableOrDefaulNull())
        .isEqualTo("\"triggerSearch\":true");
  }

  @Test
  void testBooleanVariableIsFalse() {
    final BooleanVariable graphQLVariable = new BooleanVariable("eq", false);
    assertThat(graphQLVariable.getJSONFromVariableOrDefaulNull()).isEqualTo("\"eq\":false");
  }

  @Test
  void testBooleanVariableIsNull() {
    final BooleanVariable graphQLVariable = new BooleanVariable("eq", null);
    assertThat(graphQLVariable.getJSONFromVariableOrDefaulNull()).isEqualTo("\"eq\":null");
  }
}
