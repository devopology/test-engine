package org.devopology.test.engine.test.example;

import org.devopology.test.engine.api.AfterAll;
import org.devopology.test.engine.api.BeforeAll;
import org.devopology.test.engine.api.Parameter;
import org.devopology.test.engine.api.ParameterSupplier;
import org.devopology.test.engine.api.Test;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Example test engine test... only runs from an IDE or via the test engine ConsoleRunner
 */
public class WithParameterSupplierMethod3 {

    private static class ValueContainer {

        private String value;

        public ValueContainer(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        // Optional / called via reflection
        public String getDisplayName() {
            return "ValueContainer { " + value + " }";
        }

        @Override
        public String toString() {
            return value;
        }
    }

    @ParameterSupplier
    public static Collection<ValueContainer> values() {
        Collection<ValueContainer> collection = new ArrayList<>();
        for(int i = 0; i < 10; i++) {
            collection.add(new ValueContainer(String.valueOf(i)));
        }
        return collection;
    }

    @Parameter
    public ValueContainer valueContainer;

    @BeforeAll
    public void beforeAll() {
        System.out.println("beforeAll()");
    }

    @Test
    public void test1() {
        String value = valueContainer.getValue();
        System.out.println("test1(" + value + ")");
    }

    @Test
    public void test2() {
        String value = valueContainer.getValue();
        System.out.println("test2(" + value + ")");
    }

    @AfterAll
    public void afterAll() {
        System.out.println("afterAll()");
    }
}
