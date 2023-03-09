package org.devopology.test.engine.test.example;

import org.devopology.test.engine.api.Parameter;
import org.devopology.test.engine.api.TestEngine;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Stream;

/**
 * Example test
 */
@TestEngine.Disabled
public class DisabledTest {

    private Parameter parameter;

    @TestEngine.ParameterSupplier
    public static Stream<Parameter> parameters = StringParameterSupplier.values();

    @TestEngine.ParameterSetter
    public void setParameter(Parameter parameter) {
        this.parameter = parameter;
    }

    @TestEngine.BeforeAll
    public void beforeAll() {
        System.out.println("beforeAll()");
    }

    @TestEngine.BeforeEach
    public void beforeEach() {
        System.out.println("beforeEach()");
    }

    @TestEngine.Test
    public void test1() {
        System.out.println("test1(" + parameter + ")");
    }

    @TestEngine.Test
    public void test2() {
        System.out.println("test2(" + parameter + ")");
    }

    @TestEngine.AfterEach
    public void afterEach() {
        System.out.println("afterEach()");
    }

    @TestEngine.AfterAll
    public void afterAll() {
        System.out.println("afterAll()");
    }

    private static class StringParameterSupplier {

        public static Stream<Parameter> values() {
            Collection<Parameter> collection = new ArrayList<>();

            for (int i = 0; i < 10; i++) {
                collection.add(Parameter.of(String.valueOf(i)));
            }

            return collection.stream();
        }
    }
}
