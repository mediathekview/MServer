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

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class IntegerVariableTest {

  @Test
  void testIntegerVariableWithoutSign() {
    final IntegerVariable graphQLVariable = new IntegerVariable("clipCount", 24);
    assertThat(graphQLVariable.getJSONFromVariableOrDefaulNull()).isEqualTo("\"clipCount\":24");
  }

  @Test
  void testIntegerVariableWithNegativeSign() {
    final IntegerVariable graphQLVariable = new IntegerVariable("seriesCount", -12);
    assertThat(graphQLVariable.getJSONFromVariableOrDefaulNull()).isEqualTo("\"seriesCount\":-12");
  }

  @Test
  void testIntegerVariableWithPostiveSign() {
    final IntegerVariable graphQLVariable = new IntegerVariable("height", +180);
    assertThat(graphQLVariable.getJSONFromVariableOrDefaulNull()).isEqualTo("\"height\":180");
  }

  @Test
  void testIntegerVariableWithNull() {
    final IntegerVariable graphQLVariable = new IntegerVariable("height", null);
    assertThat(graphQLVariable.getJSONFromVariableOrDefaulNull()).isEqualTo("\"height\":null");
  }
}
