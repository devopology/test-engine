package org.devopology.test.engine.test.example.order;

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
public class ConcreteOrderTest extends BaseOrderTest {

    private static final List<String> EXPECTED_LIST =
            List.of(
                    "beforeClass2",
                    "beforeClass",
                    "beforeAll2",
                    "beforeAll",
                    "test2",
                    "test3",
                    "testA",
                    "testB",
                    "afterAll2",
                    "afterAll",
                    "afterClass2",
                    "afterClass");

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

    @TestEngine.BeforeClass
    @TestEngine.Order(2)
    public static void beforeClass() {
        System.out.println("beforeClass()");
        ACTUAL_LIST.add("beforeClass");
    }

    @TestEngine.BeforeAll
    @TestEngine.Order(2)
    public void beforeAll() {
        System.out.println("beforeAll()");
        ACTUAL_LIST.add("beforeAll");
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
    @TestEngine.Order(2)
    public void test3() {
        System.out.println("test3(" + parameter.value() + ")");
        ACTUAL_LIST.add("test3");
    }

    @TestEngine.AfterAll
    @TestEngine.Order(2)
    public void afterAll() {
        System.out.println("afterAll()");
        ACTUAL_LIST.add("afterAll");
    }

    @TestEngine.AfterClass
    @TestEngine.Order(2)
    public static void afterClass() {
        System.out.println("afterClass()");
        ACTUAL_LIST.add("afterClass");
        assertThat(ACTUAL_LIST).isEqualTo(EXPECTED_LIST);
    }

}
