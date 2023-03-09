package org.devopology.test.engine.test.example;

import org.devopology.test.engine.api.Parameter;
import org.devopology.test.engine.api.TestEngine;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Stream;

/**
 * Example test
 */
public class ParameterTestWithProtectedMethods {

    private Parameter parameter;

    @TestEngine.ParameterSupplier
    protected static Stream<Parameter> parameters() {
        Collection<Parameter> collection = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            int value = i * 3;
            collection.add(Parameter.of(String.valueOf(value)));
        }
        return collection.stream();
    }

    @TestEngine.ParameterSetter
    protected void setParameter(Parameter parameter) {
        this.parameter = parameter;
    }

    @TestEngine.BeforeAll
    protected void beforeAll() {
        System.out.println("beforeAll()");
    }

    @TestEngine.Test
    protected void test1() {
        System.out.println("test1(" + parameter.value() + ")");
    }

    @TestEngine.Test
    protected void test2() {
        System.out.println("test2(" + parameter.value() + ")");
    }

    @TestEngine.AfterAll
    protected void afterAll() {
        System.out.println("afterAll()");
    }
}
