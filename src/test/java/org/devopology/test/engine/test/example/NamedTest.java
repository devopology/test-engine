package org.devopology.test.engine.test.example;

import org.devopology.test.engine.api.AfterAll;
import org.devopology.test.engine.api.BeforeAll;
import org.devopology.test.engine.api.Named;
import org.devopology.test.engine.api.Parameter;
import org.devopology.test.engine.api.Test;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Example test
 */
public class NamedTest {

    // The test engine automatically extracts the payload from a Named parameter
    @Parameter
    public String parameter;

    @Parameter.Supplier
    public static Collection<Named> parameters() {
        Collection<Named> collection = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            int value = i * 3;
            collection.add(Named.of("[" + i + "] " + value, String.valueOf(value)));
        }

        return collection;
    }

    @BeforeAll
    public void beforeAll() {
        System.out.println("beforeAll()");
    }

    @Test
    public void test1() {
        System.out.println("test1(" + parameter + ")");
    }

    @Test
    public void test2() {
        System.out.println("test2(" + parameter + ")");
    }

    @AfterAll
    public void afterAll() {
        System.out.println("afterAll()");
    }
}
