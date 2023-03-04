package org.devopology.test.engine.test.example;

import org.devopology.test.engine.api.Named;
import org.devopology.test.engine.api.TestEngine;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Example test
 */
public class ArrayTest {

    @TestEngine.ParameterInject
    public String[] parameter;

    @TestEngine.ParameterSupplier
    public static Collection<Named> parameters() {
        Collection<Named> collection = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            collection.add(
                    Named.of(
                            "Array [" + i + "]",
                            new String[] { String.valueOf(i), String.valueOf(i * 2) }));
        }

        return collection;
    }

    @TestEngine.BeforeClass
    public static void beforeClass() {
        System.out.println("beforeClass()");
    }

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
        System.out.println("test1(" + parameter[0] + ", " + parameter[1] + ")");
    }

    @TestEngine.Test
    public void test2() {
        System.out.println("test2(" + parameter[0] + ", " + parameter[1] + ")");
    }

    @TestEngine.Test
    public void test3() {
        System.out.println("test3(" + parameter[0] + ", " + parameter[1] + ")");
    }

    @TestEngine.AfterEach
    public void afterEach() {
        System.out.println("afterEach()");
    }

    @TestEngine.AfterAll
    public void afterAll() {
        System.out.println("afterAll()");
    }

    @TestEngine.AfterClass
    public static void afterClass() {
        System.out.println("afterClass()");
    }
}
