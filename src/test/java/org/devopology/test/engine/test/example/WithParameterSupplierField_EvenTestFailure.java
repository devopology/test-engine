package org.devopology.test.engine.test.example;

import org.devopology.test.engine.api.AfterAll;
import org.devopology.test.engine.api.AfterEach;
import org.devopology.test.engine.api.BeforeAll;
import org.devopology.test.engine.api.BeforeEach;
import org.devopology.test.engine.api.Parameter;
import org.devopology.test.engine.api.ParameterSupplier;
import org.devopology.test.engine.api.Test;

import java.util.ArrayList;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.fail;

/**
 * Example test engine test... only runs from an IDE or via TestEnginRunner
 */
public class WithParameterSupplierField_EvenTestFailure {

    private static class StringParameterSupplier {
        public static Collection<Integer> values() {
            Collection<Integer> collection = new ArrayList<>();
            for (int i = 0; i < 10; i++) {
                collection.add(i);
            }
            return collection;
        }
    }

    @ParameterSupplier
    public static Collection<Integer> VALUES = StringParameterSupplier.values();

    @Parameter
    public Integer value;

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
        System.out.println("test1(" + value + ")");
    }

    @Test
    public void test2() {
        if ((value % 2) == 0) {
            fail("forced failure");
        } else {
            System.out.println("test1(" + value + ")");
        }
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
