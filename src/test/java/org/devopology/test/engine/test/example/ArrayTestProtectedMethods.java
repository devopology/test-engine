package org.devopology.test.engine.test.example;

import org.devopology.test.engine.api.Parameter;
import org.devopology.test.engine.api.TestEngine;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Example test
 */
public class ArrayTestProtectedMethods {

    @TestEngine.ParameterInject
    protected Parameter parameter;

    private String[] values;

    @TestEngine.ParameterSupplier
    protected static Collection<Parameter> parameters() {
        Collection<Parameter> collection = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            collection.add(
                    Parameter.of(
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
        values = parameter.value();
    }

    @TestEngine.BeforeEach
    protected void beforeEach() {
        System.out.println("beforeEach()");
    }

    @TestEngine.Test
    protected void test1() {
        System.out.println("test1(" + values[0] + ", " + values[1] + ")");
    }

    @TestEngine.Test
    protected void test2() {
        System.out.println("test2(" + values[0] + ", " + values[1] + ")");
    }

    @TestEngine.Test
    protected void test3() {
        System.out.println("test3(" + values[0] + ", " + values[1] + ")");
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
