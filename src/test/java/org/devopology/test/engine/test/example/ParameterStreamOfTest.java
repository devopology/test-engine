package org.devopology.test.engine.test.example;

import org.devopology.test.engine.api.Parameter;
import org.devopology.test.engine.api.TestEngine;

import java.util.stream.Stream;

/**
 * Example test
 */
public class ParameterStreamOfTest {

    private Parameter parameter;

    @TestEngine.ParameterSupplier
    public static Stream<Parameter> parameters() {
        return Stream.of(
                Parameter.of(String.valueOf(1)),
                Parameter.of(String.valueOf("String")),
                Parameter.of(String.valueOf(3.0f)));
    }

    @TestEngine.ParameterSetter
    public void setParameter(Parameter parameter) {
        this.parameter = parameter;
    }

    @TestEngine.BeforeAll
    public void beforeAll() {
        System.out.println("beforeAll()");
    }

    @TestEngine.Test
    public void test1() {
        System.out.println("test1(" + parameter.value() + ")");
    }

    @TestEngine.Test
    public void test2() {
        System.out.println("test2(" + parameter.value() + ")");
    }

    @TestEngine.AfterAll
    public void afterAll() {
        System.out.println("afterAll()");
    }
}
