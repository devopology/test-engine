package org.devopology.test.engine.test.example;

import org.devopology.test.engine.api.AfterAll;
import org.devopology.test.engine.api.AfterEach;
import org.devopology.test.engine.api.BeforeAll;
import org.devopology.test.engine.api.BeforeEach;
import org.devopology.test.engine.api.DisplayName;
import org.devopology.test.engine.api.Parameter;
import org.devopology.test.engine.api.ParameterSupplier;
import org.devopology.test.engine.api.Test;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Example test
 */
@DisplayName("Test class with @ParameterSupplier field and display names")
public class ParameterSupplierFieldWithDisplayNamesTest {

    @Parameter
    public String parameter;

    @ParameterSupplier
    public static Collection<String> PARAMETERS = TestParameterSupplier.values();

    @BeforeAll
    public void beforeAll() {
        System.out.println("beforeAll()");
    }

    @BeforeEach
    public void beforeEach() {
        System.out.println("beforeEach()");
    }

    @Test
    @DisplayName("Test 1")
    public void test1() {
        System.out.println("test1(" + parameter + ")");
    }

    @Test
    @DisplayName("Test 2")
    public void test2() {
        System.out.println("test2(" + parameter + ")");
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

        public static Collection<String> values() {
            Collection<String> collection = new ArrayList<>();

            for (int i = 0; i < 10; i++) {
                collection.add(String.valueOf(i));
            }

            return collection;
        }
    }
}
