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
import org.junit.After;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.*;

public class VariableListTest {

  @After
  public void tearDown() throws Exception {}

  @Test
  public void testListWithOneSimpleElementConstructorWithOneParam() {
      final BooleanVariable triggerSearchTrue = new BooleanVariable("triggerSearch", true);
      final List<AbstractVariable> simpleList = new LinkedList<>();
    simpleList.add(triggerSearchTrue);
      final VariableList simpleListContainer = new VariableList(simpleList);

      assertEquals(
              "\"variables\":{\"triggerSearch\":true}",
              simpleListContainer.getJSONFromVariableOrDefaulNull());
  }

  @Test
  public void testNullList() {
      final VariableList nullList = new VariableList("nullVariable", null);
    assertEquals("\"nullVariable\":null", nullList.getJSONFromVariableOrDefaulNull());
  }

  @Test
  public void testThatSublistsNotMarkedAsRootList() {
      final BooleanVariable bv = new BooleanVariable("bv", false);
      final IntegerVariable iv = new IntegerVariable("iv", 50);
      final List<AbstractVariable> subList = new LinkedList<>();
    subList.add(bv);
    subList.add(iv);
      final VariableList subVl = new VariableList("SubList", subList);

    assertTrue(subVl.isRootElement()); // Till now the Sublist is a RootList

      final StringVariable sv = new StringVariable("sv", "text");
      final FloatVariable fv = new FloatVariable("fv", 0.001);
      final List<AbstractVariable> rootList = new LinkedList<>();
    rootList.add(sv);
    rootList.add(subVl);
    rootList.add(fv);

      final VariableList rootVl = new VariableList(rootList);
    assertTrue(rootVl.isRootElement());
      rootVl.getValue().stream()
              .filter(VariableList.class::isInstance)
              .map(VariableList.class::cast)
              .forEach(v -> assertFalse(v.isRootElement()));
  }

  @Test
  public void testRealBRExample() {

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

      assertEquals(
              "\"variables\":{\"triggerSearch\":true,\"clipCount\":5000,\"clipFilter\":{\"audioOnly\":{\"eq\":false},\"essences\":{\"empty\":{\"eq\":false}}}}",
              variables.getJSONFromVariableOrDefaulNull());
  }
}
