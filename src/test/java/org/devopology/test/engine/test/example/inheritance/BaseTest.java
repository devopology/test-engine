package org.devopology.test.engine.test.example.inheritance;

import org.devopology.test.engine.api.Named;
import org.devopology.test.engine.api.TestEngine;

import java.util.ArrayList;
import java.util.Collection;

public abstract class BaseTest {

    @TestEngine.ParameterInject
    protected int parameter;

    @TestEngine.ParameterSupplier
    protected static Collection<Named> parameters() {
        Collection<Named> collection = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            collection.add(Named.of("Array [" + i + "]", i));
        }

        return collection;
    }

    @TestEngine.BeforeClass
    protected static void _beforeClass() {
        System.out.println("_beforeClass()");
    }

    @TestEngine.BeforeClass
    protected static void beforeClass() {
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
    protected static void afterClass() {
        System.out.println("afterClass()");
    }

    @TestEngine.AfterClass
    protected static void afterClass_() {
        System.out.println("afterClass_()");
    }
}
