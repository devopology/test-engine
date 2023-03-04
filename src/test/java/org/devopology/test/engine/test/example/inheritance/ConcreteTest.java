package org.devopology.test.engine.test.example.inheritance;

import org.devopology.test.engine.api.TestEngine;

public class ConcreteTest extends BaseTest {

    @TestEngine.Test
    public void testB() {
        System.out.println("testB()");
    }
}
