package org.devopology.test.engine.test.example;

import org.devopology.test.engine.api.AfterAll;
import org.devopology.test.engine.api.BeforeAll;
import org.devopology.test.engine.api.Parameter;
import org.devopology.test.engine.api.ParameterMap;
import org.devopology.test.engine.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Stream;

/**
 * Example test
 */
public class ParameterMapTest {

    @Parameter
    public ParameterMap parameter;

    @Parameter.Supplier
    public static Stream<ParameterMap> parameters() {
        Collection<ParameterMap> collection = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            collection.add(ParameterMap.of("parameter map [" + i + "]").put("value", String.valueOf(i)));
        }

        return collection.stream();
    }

    @BeforeAll
    public void beforeAll() {
        System.out.println("beforeAll()");
    }

    @Test
    public void test1() {
        String value = parameter.get("value");
        System.out.println("test1(" + value + ")");
    }

    @Test
    public void test2() {
        String value = parameter.get("value");
        System.out.println("test2(" + value + ")");
    }

    @AfterAll
    public void afterAll() {
        System.out.println("afterAll()");
    }
}
