package org.devopology.test.engine.test.example;

import org.devopology.test.engine.api.TestEngine;

import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Stream;

/**
 * Example test
 */
public class ParameterSupplierMethodTest2 {

    @TestEngine.ParameterInject
    public String parameter;

    @TestEngine.ParameterSupplier
    public static Stream<String> parameters() {
        Set<String> collection = new TreeSet<>();

        for (int i = 0; i < 10; i++) {
            collection.add(String.valueOf(i));
        }

        return collection.stream();
    }

    @TestEngine.BeforeAll
    public void beforeAll() {
        System.out.println("beforeAll()");
    }

    @TestEngine.Test
    public void test1() {
        System.out.println("test1(" + parameter + ")");
    }

    @TestEngine.Test
    public void test2() {
        System.out.println("test2(" + parameter + ")");
    }

    @TestEngine.AfterAll
    public void afterAll() {
        System.out.println("afterAll()");
    }
}
