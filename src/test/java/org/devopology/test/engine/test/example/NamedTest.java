package org.devopology.test.engine.test.example;

import org.devopology.test.engine.api.AfterAllTests;
import org.devopology.test.engine.api.BeforeAllTests;
import org.devopology.test.engine.api.Named;
import org.devopology.test.engine.api.Parameter;
import org.devopology.test.engine.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Stream;

/**
 * Example test
 */
public class NamedTest {

    // The test engine automatically extracts the payload from a Named parameter
    @Parameter
    public String parameter;

    @Parameter.Supplier
    public static Stream<Named> parameters() {
        Collection<Named> collection = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            int value = i * 3;
            collection.add(Named.of("[" + i + "] " + value, String.valueOf(value)));
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
