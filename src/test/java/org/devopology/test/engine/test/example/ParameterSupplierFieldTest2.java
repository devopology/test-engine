package org.devopology.test.engine.test.example;

import org.devopology.test.engine.api.AfterAll;
import org.devopology.test.engine.api.AfterEach;
import org.devopology.test.engine.api.BeforeAll;
import org.devopology.test.engine.api.BeforeEach;
import org.devopology.test.engine.api.Named;
import org.devopology.test.engine.api.Parameter;
import org.devopology.test.engine.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

/**
 * Example test
 */
public class ParameterSupplierFieldTest2 {

    @Parameter
    public String[] parameter;

    @Parameter.Supplier
    public static Stream<Named> PARAMETERS = TestParameterSupplier.values();

    @BeforeAll
    public void beforeAll() {
        System.out.println("beforeAll()");
    }

    @BeforeEach
    public void beforeEach() {
        System.out.println("beforeEach()");
    }

    @Test
    public void test1() {
        System.out.println("test1(" + toString(parameter) + ")");
    }

    @Test
    public void test2() {
        System.out.println("test2(" + toString(parameter) + ")");
    }

    @AfterEach
    public void afterEach() {
        System.out.println("afterEach()");
    }

    @AfterAll
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
