package org.devopology.test.engine.test.example;

import org.devopology.test.engine.api.Parameter;
import org.devopology.test.engine.api.TestEngine;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Example test
 */
public class MethodOrderTest {

    private static final List<String> EXPECTED_LIST = List.of("test2", "test3", "testA", "testB");
    private static final List<String> ACTUAL_LIST = new ArrayList<>();

    private Parameter parameter;

    @TestEngine.ParameterSupplier
    public static Stream<Parameter> parameters() {
        Collection<Parameter> collection = new ArrayList<>();
        for (int i = 0; i < 1; i++) {
            int value = i * 3;
            collection.add(Parameter.of(String.valueOf(value)));
        }
        return collection.stream();
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
    public void testA() {
        System.out.println("testA(" + parameter.value() + ")");
        ACTUAL_LIST.add("testA");
    }

    @TestEngine.Test
    public void testB() {
        System.out.println("testB(" + parameter.value() + ")");
        ACTUAL_LIST.add("testB");
    }

    @TestEngine.Test
    @TestEngine.Test.Order(2)
    public void test3() {
        System.out.println("test3(" + parameter.value() + ")");
        ACTUAL_LIST.add("test3");
    }

    @TestEngine.Test
    @TestEngine.Test.Order(1)
    public void test2() {
        System.out.println("test2(" + parameter.value() + ")");
        ACTUAL_LIST.add("test2");
    }

    @TestEngine.AfterAll
    public void afterAll() {
        System.out.println("afterAll()");
        assertThat(ACTUAL_LIST).isEqualTo(EXPECTED_LIST);
    }
}
