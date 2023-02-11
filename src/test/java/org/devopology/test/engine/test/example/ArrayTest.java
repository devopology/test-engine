package org.devopology.test.engine.test.example;

import org.devopology.test.engine.api.AfterAll;
import org.devopology.test.engine.api.BeforeAll;
import org.devopology.test.engine.api.Parameter;
import org.devopology.test.engine.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Stream;

/**
 * Example test
 */
public class ArrayTest {

    @Parameter
    public String[] parameter;

    @Parameter.Supplier
    public static Stream<String[]> parameters() {
        Collection<String[]> collection = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            collection.add(new String[] { String.valueOf(i), String.valueOf(i * 2) });
        }

        return collection.stream();
    }

    @BeforeAll
    public void beforeAll() {
        System.out.println("beforeAll()");
    }

    @Test
    public void test1() {
        System.out.println("test1(" + parameter[0] + ", " + parameter[1] + ")");
    }

    @Test
    public void test2() {
        System.out.println("test2(" + parameter[0] + ", " + parameter[1] + ")");
    }

    @AfterAll
    public void afterAll() {
        System.out.println("afterAll()");
    }
}
