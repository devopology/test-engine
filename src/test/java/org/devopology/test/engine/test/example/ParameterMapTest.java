package org.devopology.test.engine.test.example;

import org.devopology.test.engine.api.Parameter;
import org.devopology.test.engine.api.ParameterMap;
import org.devopology.test.engine.api.TestEngine;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Example test
 */
public class ParameterMapTest {

    @TestEngine.ParameterInject
    public Parameter parameter;

    @TestEngine.ParameterSupplier
    public static Collection<Parameter> parameters() {
        Collection<Parameter> collection = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            ParameterMap parameterMap = new ParameterMap();
            parameterMap.put("key1", "value1");
            collection.add(Parameter.of("ParameterMap[" + i + "]", parameterMap));
        }

        return collection;
    }

    @TestEngine.BeforeAll
    public void beforeAll() {
        System.out.println("beforeAll()");
    }

    @TestEngine.Test
    public void test1() {
        String value = ((ParameterMap) parameter.value()).get("key1");
        System.out.println("test1(" + value + ")");
    }

    @TestEngine.Test
    public void test2() {
        String value = ((ParameterMap) parameter.value()).get("key1");
        System.out.println("test2(" + value + ")");
    }

    @TestEngine.AfterAll
    public void afterAll() {
        System.out.println("afterAll()");
    }
}
