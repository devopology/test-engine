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

    @Test
    public void test1() {
        System.out.println("test1()");
    }

    @Test
    public void test2() {
        System.out.println("test2()");
    }

    @Test
    public void test3() {
        System.out.println("test3()");
    }

    @Test
    public void test4() {
        System.out.println("test4()");
    }

    @AfterAll
    public static void afterAll() {
        System.out.println("afterAll()");
    }
}
