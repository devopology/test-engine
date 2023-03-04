package org.devopology.test.engine.test.example;

import org.devopology.test.engine.api.Named;
import org.devopology.test.engine.api.TestEngine;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Example test
 */
public class ArrayTestProtectedMethods {

    @TestEngine.ParameterInject
    protected String[] parameter;

    @TestEngine.ParameterSupplier
    protected static Collection<Named> parameters() {
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
    protected static void beforeClass() {
        System.out.println("beforeClass()");
    }

    @TestEngine.BeforeAll
    protected void beforeAll() {
        System.out.println("beforeAll()");
    }

    @TestEngine.BeforeEach
    protected void beforeEach() {
        System.out.println("beforeEach()");
    }

    @TestEngine.Test
    protected void test1() {
        System.out.println("test1(" + parameter[0] + ", " + parameter[1] + ")");
    }

    @TestEngine.Test
    protected void test2() {
        System.out.println("test2(" + parameter[0] + ", " + parameter[1] + ")");
    }

    @TestEngine.Test
    protected void test3() {
        System.out.println("test3(" + parameter[0] + ", " + parameter[1] + ")");
    }

    @TestEngine.AfterEach
    protected void afterEach() {
        System.out.println("afterEach()");
    }

    @TestEngine.AfterAll
    protected void afterAll() {
        System.out.println("afterAll()");
    }

    @TestEngine.AfterClass
    protected static void afterClass() {
        System.out.println("afterClass()");
    }
}
