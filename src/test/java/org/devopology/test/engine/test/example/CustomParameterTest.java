package org.devopology.test.engine.test.example;

import org.devopology.test.engine.api.Parameter;
import org.devopology.test.engine.api.TestEngine;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Example test
 */
public class CustomParameterTest {

    private CustomParameter customParameter;

    @TestEngine.ParameterSupplier
    public static Stream<Parameter> parameters() {
        Collection<Parameter> collection = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            int value = i * 3;
            collection.add(CustomParameter.of("CustomParameter(" + i + ") = " + value, String.valueOf(value)));
        }
        return collection.stream();
    }

    @TestEngine.ParameterSetter
    public void setParameter(Parameter parameter) {
        customParameter = (CustomParameter) parameter;
    }

    @TestEngine.BeforeAll
    public void beforeAll() {
        System.out.println("beforeAll()");
    }

    @TestEngine.Test
    public void test1() {
        System.out.println("test1(" + customParameter.value() + ")");
    }

    @TestEngine.Test
    public void test2() {
        System.out.println("test2(" + customParameter.value() + ")");
    }

    @TestEngine.AfterAll
    public void afterAll() {
        System.out.println("afterAll()");
    }

    private static class CustomParameter implements Parameter {

        private final String name;
        private final String value;

        private CustomParameter(String name, String value) {
            this.name = name;
            this.value = value;
        }

        @Override
        public String name() {
            return name;
        }

        @Override
        public <T> T value() {
            return (T) value;
        }

        @Override
        public <T> T value(Class<T> clazz) {
            return clazz.cast(value());
        }

        public static CustomParameter of(String name, String value) {
            Objects.requireNonNull(name);
            return new CustomParameter(name, value);
        }
    }
}
