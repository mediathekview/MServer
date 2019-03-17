/*
 * RecursiveAbstractVariableTest.java
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

public class RecursiveAbstractVariableTest {

  @After
  public void tearDown() throws Exception {}

  @Test
  public void testOneRecursion() {
      final BooleanVariable eqFalseVariable = new BooleanVariable("eq", false);
      final RecursiveAbstractVariable emptyEqFalseVariable =
              new RecursiveAbstractVariable("empty", eqFalseVariable);
      assertEquals(
              "\"empty\":{\"eq\":false}", emptyEqFalseVariable.getJSONFromVariableOrDefaulNull());
  }

  @Test
  public void testDoubleRecursion() {
      final BooleanVariable eqFalseVariable = new BooleanVariable("eq", false);
      final RecursiveAbstractVariable emptyEqFalseVariable =
              new RecursiveAbstractVariable("empty", eqFalseVariable);
      final RecursiveAbstractVariable essencesEmptyEqFalseVariable =
              new RecursiveAbstractVariable("essences", emptyEqFalseVariable);
      assertEquals(
              "\"essences\":{\"empty\":{\"eq\":false}}",
              essencesEmptyEqFalseVariable.getJSONFromVariableOrDefaulNull());
  }

  @Test
  public void testNullObject() {
      final BooleanVariable booleanVariable = null;
      final RecursiveAbstractVariable variableWithBooleanNull =
              new RecursiveAbstractVariable("eq", booleanVariable);
    assertEquals("\"eq\":null", variableWithBooleanNull.getJSONFromVariableOrDefaulNull());
  }

  @Test
  public void testSubListSetsStatusRootNodeToFalse() {
      final BooleanVariable bv = new BooleanVariable("bv", false);
      final StringVariable sv = new StringVariable("sv", "text");
      final List<AbstractVariable> subList = new LinkedList<>();
    subList.add(bv);
    subList.add(sv);

      final VariableList vl = new VariableList("SubList", subList);

      assertTrue(vl.isRootElement());

      final RecursiveAbstractVariable rv = new RecursiveAbstractVariable("father", vl);

      if (rv.getValue() instanceof VariableList) {
          final VariableList sub = (VariableList) rv.getValue();
          sub.getValue().stream()
                  .filter(VariableList.class::isInstance)
                  .map(VariableList.class::cast)
                  .forEach(v -> assertFalse(v.isRootElement()));
    }
  }
}
