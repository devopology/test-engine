package org.devopology.test.engine.test.example;

import org.devopology.test.engine.api.AfterAll;
import org.devopology.test.engine.api.BeforeAll;
import org.devopology.test.engine.api.Parameter;
import org.devopology.test.engine.api.ParameterSupplier;
import org.devopology.test.engine.api.Test;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Example test engine test... only runs from an IDE or via TestEnginRunner
 */
public class WithParameterSupplierMethod {

    @ParameterSupplier
    public static Collection<String> values() {
        Collection<String> collection = new ArrayList<>();
        for(int i = 0; i < 10; i++) {
            collection.add(String.valueOf(i));
        }
        return collection;
    }

    @Parameter
    public String value;

    @BeforeAll
    public void beforeAll() {
        System.out.println("beforeAll()");
    }

    @Test
    public void test1() {
        System.out.println("test1(" + value + ")");
    }

    @Test
    public void test2() {
        System.out.println("test2(" + value + ")");
    }

    @AfterAll
    public void afterAll() {
        System.out.println("afterAll()");
    }
}
