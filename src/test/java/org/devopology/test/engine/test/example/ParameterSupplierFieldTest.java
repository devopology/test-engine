package org.devopology.test.engine.test.example;

import org.devopology.test.engine.api.Parameter;
import org.devopology.test.engine.api.TestEngine;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Example test
 */
public class ParameterSupplierFieldTest {

    @TestEngine.ParameterInject
    public Parameter parameter;

    @TestEngine.ParameterSupplier
    public static Collection<Parameter> PARAMETERS = TestParameterSupplier.values();

    @TestEngine.BeforeClass
    public static void beforeClas() {
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
        System.out.println("test1(" + parameter.value() + ")");
    }

    @TestEngine.Test
    public void test2() {
        System.out.println("test2(" + parameter.value() + ")");
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

    private static class TestParameterSupplier {

        public static Collection<Parameter> values() {
            Collection<Parameter> collection = new ArrayList<>();

            for (int i = 0; i < 10; i++) {
                collection.add(Parameter.of(String.valueOf(i)));
            }

            return collection;
        }
    }
}
