package org.devopology.test.engine.test.example;

import org.devopology.test.engine.api.*;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Example test engine test... only runs from an IDE or via the test engine ConsoleRunner
 */
@Disabled
public class DisabledTest {

    private static class StringParameterSupplier {

        public static Collection<String> values() {
            Collection<String> collection = new ArrayList<>();

            for (int i = 0; i < 10; i++) {
                collection.add(String.valueOf(i));
            }

            return collection;
        }
    }

    @ParameterSupplier
    public static Collection<String> PARAMETERS = StringParameterSupplier.values();

    @Parameter
    public String parameter;

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
        System.out.println("test1(" + parameter + ")");
    }

    @Test
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
}
