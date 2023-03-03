package org.devopology.test.engine.test.example.inheritance;

import org.devopology.test.engine.api.Named;
import org.devopology.test.engine.api.TestEngine;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Stream;

// Disable the test class since it's only used as a base
// for other test classes and isn't meant to be run directly
@TestEngine.Disabled
public class BaseTest {

    @TestEngine.ParameterInject
    public String[] parameter;

    @TestEngine.ParameterSupplier
    public static Stream<Named> parameters() {
        Collection<Named> collection = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            collection.add(
                    Named.of(
                            "Array [" + i + "]",
                            new String[] { String.valueOf(i), String.valueOf(i * 2) }));
        }

        return collection.stream();
    }

    @TestEngine.BeforeClass
    public static void beforeClass() {
        System.out.println("beforeClass()");
    }

    @TestEngine.BeforeEach
    public void beforeEach() {
        System.out.println("beforeEach()");
    }

    @TestEngine.Test
    public void testA() {
        System.out.println("testA()");
    }

    @TestEngine.AfterEach
    public void afterEach() {
        System.out.println("afterEach()");
    }

    @TestEngine.AfterClass
    public static void afterClass() {
        System.out.println("afterClass()");
    }
}
