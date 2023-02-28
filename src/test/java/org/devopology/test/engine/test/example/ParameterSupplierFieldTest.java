package org.devopology.test.engine.test.example;

import org.devopology.test.engine.api.AfterAllTests;
import org.devopology.test.engine.api.AfterEachTest;
import org.devopology.test.engine.api.BeforeAllTests;
import org.devopology.test.engine.api.BeforeEachTest;
import org.devopology.test.engine.api.Parameter;
import org.devopology.test.engine.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Stream;

/**
 * Example test
 */
public class ParameterSupplierFieldTest {

    @Parameter
    public String parameter;

    @Parameter.Supplier
    public static Stream<String> PARAMETERS = TestParameterSupplier.values();

    @BeforeAllTests
    public void beforeAllTests() {
        System.out.println("beforeAllTests()");
    }

    @BeforeEachTest
    public void beforeEach() {
        System.out.println("beforeEach()");
    }

    @Test
    public void test1() {
        System.out.println("test1(" + parameter + ")");
    }

    @Test
    public void test2() {
        System.out.println("test2(" + parameter + ")");
    }

    @AfterEachTest
    public void afterEach() {
        System.out.println("afterEach()");
    }

    @AfterAllTests
    public void afterAllTests() {
        System.out.println("afterAllTests()");
    }

    private static class TestParameterSupplier {

        public static Stream<String> values() {
            Collection<String> collection = new ArrayList<>();

            for (int i = 0; i < 10; i++) {
                collection.add(String.valueOf(i));
            }

            return collection.stream();
        }
    }
}
