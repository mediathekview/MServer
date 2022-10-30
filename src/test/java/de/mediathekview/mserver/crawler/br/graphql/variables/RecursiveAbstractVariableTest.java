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
import org.junit.jupiter.api.Test;

import java.util.LinkedList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RecursiveAbstractVariableTest {


    @Test
    void testOneRecursion() {
        final BooleanVariable eqFalseVariable = new BooleanVariable("eq", false);
        final RecursiveAbstractVariable emptyEqFalseVariable = new RecursiveAbstractVariable("empty", eqFalseVariable);
        assertThat(emptyEqFalseVariable.getJSONFromVariableOrDefaulNull()).isEqualTo("\"empty\":{\"eq\":false}");
    }

    @Test
    void testDoubleRecursion() {
        final BooleanVariable eqFalseVariable = new BooleanVariable("eq", false);
        final RecursiveAbstractVariable emptyEqFalseVariable = new RecursiveAbstractVariable("empty", eqFalseVariable);
        final RecursiveAbstractVariable essencesEmptyEqFalseVariable = new RecursiveAbstractVariable("essences", emptyEqFalseVariable);
        assertThat(essencesEmptyEqFalseVariable.getJSONFromVariableOrDefaulNull()).isEqualTo("\"essences\":{\"empty\":{\"eq\":false}}");
    }

    @Test
    void testNullObject() {
        final RecursiveAbstractVariable variableWithBooleanNull =
                new RecursiveAbstractVariable("eq", null);
        assertThat(variableWithBooleanNull.getJSONFromVariableOrDefaulNull()).isEqualTo("\"eq\":null");
    }

    @Test
    void testSubListSetsStatusRootNodeToFalse() {
        final BooleanVariable bv = new BooleanVariable("bv", false);
        final StringVariable sv = new StringVariable("sv", "text");
        final List<AbstractVariable> subList = new LinkedList<>();
        subList.add(bv);
        subList.add(sv);

        final VariableList vl = new VariableList("SubList", subList);

        assertThat(vl.isRootElement()).isTrue();

        final RecursiveAbstractVariable rv = new RecursiveAbstractVariable("father", vl);

        if (rv.getValue() instanceof final VariableList sub) {
            sub.getValue().stream()
                    .filter(VariableList.class::isInstance)
                    .map(VariableList.class::cast)
                    .forEach(v -> assertThat(v.isRootElement()).isFalse());
        }
    }
}
