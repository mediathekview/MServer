/*
 * VariableListTest.java
 *
 * Projekt    : MServer
 * erstellt am: 08.12.2017
 * Autor      : Sascha
 *
 * (c) 2017 by Sascha Wiegandt
 */
package de.mediathekview.mserver.crawler.br.graphql.variables;

import de.mediathekview.mserver.crawler.br.graphql.AbstractVariable;
import org.junit.jupiter.api.Test;

import java.util.LinkedList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class VariableListTest {

  @Test
  void testListWithOneSimpleElementConstructorWithOneParam() {
    final BooleanVariable triggerSearchTrue = new BooleanVariable("triggerSearch", true);
    final List<AbstractVariable> simpleList = new LinkedList<>();
    simpleList.add(triggerSearchTrue);
    final VariableList simpleListContainer = new VariableList(simpleList);

    assertThat(simpleListContainer.getJSONFromVariableOrDefaulNull())
        .isEqualTo("\"variables\":{\"triggerSearch\":true}");
  }

  @Test
  void testNullList() {
    final VariableList nullList = new VariableList("nullVariable", null);
    assertThat(nullList.getJSONFromVariableOrDefaulNull()).isEqualTo("\"nullVariable\":null");
  }

  @Test
  void testThatSublistsNotMarkedAsRootList() {
    final BooleanVariable bv = new BooleanVariable("bv", false);
    final IntegerVariable iv = new IntegerVariable("iv", 50);
    final List<AbstractVariable> subList = new LinkedList<>();
    subList.add(bv);
    subList.add(iv);
    final VariableList subVl = new VariableList("SubList", subList);

    assertThat(subVl.isRootElement()).isTrue(); // Till now the Sublist is a RootList

    final StringVariable sv = new StringVariable("sv", "text");
    final FloatVariable fv = new FloatVariable("fv", 0.001);
    final List<AbstractVariable> rootList = new LinkedList<>();
    rootList.add(sv);
    rootList.add(subVl);
    rootList.add(fv);

    final VariableList rootVl = new VariableList(rootList);
    assertThat(rootVl.isRootElement()).isTrue();
    rootVl.getValue().stream()
        .filter(VariableList.class::isInstance)
        .map(VariableList.class::cast)
        .forEach(v -> assertThat(v.isRootElement()).isFalse());
  }

  @Test
  void testRealBRExample() {

    final BooleanVariable eqFalse = new BooleanVariable("eq", false);
    final RecursiveAbstractVariable emptyEqFalse = new RecursiveAbstractVariable("empty", eqFalse);
    final RecursiveAbstractVariable essencesEmptyEqFalse =
        new RecursiveAbstractVariable("essences", emptyEqFalse);

    final RecursiveAbstractVariable audioOnlyEqFalse =
        new RecursiveAbstractVariable("audioOnly", eqFalse);

    final List<AbstractVariable> clipFilterList = new LinkedList<>();
    clipFilterList.add(audioOnlyEqFalse);
    clipFilterList.add(essencesEmptyEqFalse);
    final VariableList clipFilter = new VariableList("clipFilter", clipFilterList);

    final IntegerVariable clipCount = new IntegerVariable("clipCount", 5000);
    final BooleanVariable triggerSearch = new BooleanVariable("triggerSearch", true);

    final List<AbstractVariable> variablesList = new LinkedList<>();
    variablesList.add(triggerSearch);
    variablesList.add(clipCount);
    variablesList.add(clipFilter);

    final VariableList variables = new VariableList(variablesList);

    assertThat(variables.getJSONFromVariableOrDefaulNull())
        .isEqualTo(
            "\"variables\":{\"triggerSearch\":true,\"clipCount\":5000,\"clipFilter\":{\"audioOnly\":{\"eq\":false},\"essences\":{\"empty\":{\"eq\":false}}}}");
  }
}
