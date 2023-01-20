package org.devopology.test.engine.test.example;

import org.devopology.test.engine.api.AfterAll;
import org.devopology.test.engine.api.BeforeAll;
import org.devopology.test.engine.api.Parameter;
import org.devopology.test.engine.api.ParameterMap;
import org.devopology.test.engine.api.ParameterSupplier;
import org.devopology.test.engine.api.Test;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Example test engine test... only runs from an IDE or via the test engine ConsoleRunner
 */
public class WithParameterSupplierMethod2 {

    @ParameterSupplier
    public static Collection<ParameterMap> values() {
        Collection<ParameterMap> collection = new ArrayList<>();
        for(int i = 0; i < 10; i++) {
            collection.add(ParameterMap.of("value " + i).put("value", String.valueOf(i)));
        }
        return collection;
    }

    @Parameter
    public ParameterMap parameterMap;

    @BeforeAll
    public void beforeAll() {
        System.out.println("beforeAll()");
    }

    @Test
    public void test1() {
        String value = parameterMap.get("value");
        System.out.println("test1(" + value + ")");
    }

    @Test
    public void test2() {
        String value = parameterMap.get("value");
        System.out.println("test2(" + value + ")");
    }

    @AfterAll
    public void afterAll() {
        System.out.println("afterAll()");
    }
}
