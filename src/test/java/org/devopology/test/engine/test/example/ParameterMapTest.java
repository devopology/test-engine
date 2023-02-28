package org.devopology.test.engine.test.example;

import org.devopology.test.engine.api.AfterAllTests;
import org.devopology.test.engine.api.BeforeAllTests;
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

    @BeforeAllTests
    public void beforeAllTests() {
        System.out.println("beforeAllTests()");
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

    @AfterAllTests
    public void afterAllTests() {
        System.out.println("afterAllTests()");
    }
}
