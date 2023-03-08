package org.devopology.test.engine.test.example;

import org.devopology.test.engine.api.Parameter;
import org.devopology.test.engine.api.TestEngine;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Example test
 */
public class ParameterSupplierFieldTest2 {

    @TestEngine.ParameterInject
    public Parameter parameter;

    private String[] values;

    @TestEngine.ParameterSupplier
    public static Collection<Parameter> PARAMETERS = TestParameterSupplier.values();

    @TestEngine.BeforeAll
    public void beforeAll() {
        System.out.println("beforeAll()");
        values = parameter.value();
    }

    @TestEngine.BeforeEach
    public void beforeEach() {
        System.out.println("beforeEach()");
    }

    @TestEngine.Test
    public void test1() {
        System.out.println("test1(" + toString(values) + ")");
    }

    @TestEngine.Test
    public void test2() {
        System.out.println("test2(" + toString(values) + ")");
    }

    @TestEngine.AfterEach
    public void afterEach() {
        System.out.println("afterEach()");
    }

    @TestEngine.AfterAll
    public void afterAll() {
        System.out.println("afterAll()");
    }

    private static class TestParameterSupplier {

        public static Collection<Parameter> values() {
            Collection<Parameter> list = new ArrayList<>();

            list.add(Parameter.of("array 0", new String[] { "1", "2" }));
            list.add(Parameter.of("array 1", new String[] { "1", "2", "3" }));

            return list;
        }
    }

    private static String toString(String ... strings) {
        StringBuilder stringBuilder = new StringBuilder();
        for (String string : strings) {
            stringBuilder.append(string);
            stringBuilder.append(", ");
        }

        String string = stringBuilder.toString();
        string = string.substring(0, string.length() - ", ".length());
        return string;
    }
}
