package org.devopology.test.engine.test.example;

import org.devopology.test.engine.api.Named;
import org.devopology.test.engine.api.TestEngine;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Example test
 */
public class NamedTest {

    // The test engine automatically extracts the payload from a Named parameter
    @TestEngine.ParameterInject
    public String parameter;

    @TestEngine.ParameterSupplier
    public static Collection<Named> parameters() {
        Collection<Named> collection = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            int value = i * 3;
            collection.add(Named.of("[" + i + "] " + value, String.valueOf(value)));
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
