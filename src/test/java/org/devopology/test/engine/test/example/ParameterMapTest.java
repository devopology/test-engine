package org.devopology.test.engine.test.example;

import org.devopology.test.engine.api.ParameterMap;
import org.devopology.test.engine.api.TestEngine;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Example test
 */
public class ParameterMapTest {

    @TestEngine.ParameterInject
    public ParameterMap parameter;

    @TestEngine.ParameterSupplier
    public static Collection<ParameterMap> parameters() {
        Collection<ParameterMap> collection = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            collection.add(ParameterMap.of("parameter map [" + i + "]").put("value", String.valueOf(i)));
        }

        return collection;
    }

    @TestEngine.BeforeAll
    public void beforeAll() {
        System.out.println("beforeAll()");
    }

    @TestEngine.Test
    public void test1() {
        String value = parameter.get("value");
        System.out.println("test1(" + value + ")");
    }

    @TestEngine.Test
    public void test2() {
        String value = parameter.get("value");
        System.out.println("test2(" + value + ")");
    }

    @TestEngine.AfterAll
    public void afterAll() {
        System.out.println("afterAll()");
    }
}
