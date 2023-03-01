package org.devopology.test.engine.test.example;

import org.devopology.test.engine.api.Named;
import org.devopology.test.engine.api.TestEngine;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Example test
 */
public class ParameterSupplierFieldTest2 {

    @TestEngine.ParameterInject
    public String[] parameter;

    @TestEngine.ParameterSupplier
    public static Stream<Named> PARAMETERS = TestParameterSupplier.values();

    @TestEngine.BeforeAll
    public void beforeAll() {
        System.out.println("beforeAll()");
    }

    @TestEngine.BeforeEach
    public void beforeEach() {
        System.out.println("beforeEach()");
    }

    @TestEngine.Test
    public void test1() {
        System.out.println("test1(" + toString(parameter) + ")");
    }

    @TestEngine.Test
    public void test2() {
        System.out.println("test2(" + toString(parameter) + ")");
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

        public static Stream<Named> values() {
            List<Named> list = new ArrayList<>();

            list.add(Named.of("array 0", new String[] { "1", "2" }));
            list.add(Named.of("array 1", new String[] { "1", "2", "3" }));

            return list.stream();
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
