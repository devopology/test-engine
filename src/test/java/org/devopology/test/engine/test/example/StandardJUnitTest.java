package org.devopology.test.engine.test.example;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class StandardJUnitTest {

    @BeforeAll
    public static void beforeAll() {
        System.out.println("beforeAll()");
    }

    @BeforeEach
    public void beforeEach() {
        System.out.println("beforeEach()");
    }

    @Test
    public void test1() {
        System.out.println("test1()");
    }

    @Test
    public void test2() {
        System.out.println("test2()");
    }

    @AfterEach
    public void afterEach() {
        System.out.println("afterEach()");
    }

    @AfterAll
    public static void afterAll() {
        System.out.println("afterAll()");
    }
}
