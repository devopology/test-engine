package org.devopology.test.engine.test.example;

import org.devopology.test.engine.api.TestEngine;

import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

/**
 * Example test
 */
public class ParameterSupplierMethodTest2 {

    @TestEngine.ParameterInject
    public String parameter;

    @TestEngine.ParameterSupplier
    public static Collection<String> parameters() {
        Set<String> collection = new TreeSet<>();

        for (int i = 0; i < 10; i++) {
            collection.add(String.valueOf(i));
        }

        return collection;
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
