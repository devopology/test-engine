package org.devopology.test.engine.test.example;

import org.devopology.test.engine.api.AfterAllTests;
import org.devopology.test.engine.api.BeforeAllTests;
import org.devopology.test.engine.api.Parameter;
import org.devopology.test.engine.api.Test;

import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Stream;

/**
 * Example test
 */
public class ParameterSupplierMethodTest2 {

    @Parameter
    public String parameter;

    @Parameter.Supplier
    public static Stream<String> parameters() {
        Set<String> collection = new TreeSet<>();

        for (int i = 0; i < 10; i++) {
            collection.add(String.valueOf(i));
        }

        return collection.stream();
    }

    @BeforeAllTests
    public void beforeAllTests() {
        System.out.println("beforeAllTests()");
    }

    @Test
    public void test1() {
        System.out.println("test1(" + parameter + ")");
    }

    @Test
    public void test2() {
        System.out.println("test2(" + parameter + ")");
    }

    @AfterAllTests
    public void afterAllTests() {
        System.out.println("afterAllTests()");
    }
}
