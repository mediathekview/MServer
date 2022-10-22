/*
 * FloatVariableTest.java
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

class FloatVariableTest {

  @Test
  void testMaxFloatValue() {
    final FloatVariable graphQLVariable = new FloatVariable("degress", Double.MAX_VALUE);
    assertThat(graphQLVariable.getJSONFromVariableOrDefaulNull())
        .isEqualTo("\"degress\":1.7976931348623157E308");
  }

  @Test
  void testMinFloatValue() {
    final FloatVariable graphQLVariable = new FloatVariable("negativeDegress", Double.MIN_VALUE);
    assertThat(graphQLVariable.getJSONFromVariableOrDefaulNull())
        .isEqualTo("\"negativeDegress\":4.9E-324");
  }

  @Test
  void testNullFloatValue() {
    final FloatVariable graphQLVariable = new FloatVariable("lenght", null);
    assertThat(graphQLVariable.getJSONFromVariableOrDefaulNull()).isEqualTo("\"lenght\":null");
  }
}
