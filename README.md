[![Build](https://github.com/devopology/test-engine/actions/workflows/build.yml/badge.svg)](https://github.com/devopology/test-engine/actions/workflows/build.yml)

# Devopology Test Engine

The Devopology Test Engine is a JUnit 5 based test engine that allows for parameterized testing at the test class level.

## Why ?

Currently, JUnit 5 does not support parameterized tests at the test class level
- https://github.com/junit-team/junit5/issues/878

## Usage
 
Add the Devopology Maven repository to your `pom.xml` file...

```
<repositories>
    <repository>
        <id>devopology-test-engine</id>
        <url>https://repository.devopology.org/test-engine</url>
        <releases>
            <enabled>true</enabled>
        </releases>
        <snapshots>
            <enabled>true</enabled>
        </snapshots>
    </repository>
</repositories>
```

Add the Junit 5 and Devopology test engine jar dependencies...

```
<dependency>
    <groupId>org.junit.jupiter</groupId>
    <artifactId>junit-jupiter-api</artifactId>
    <version>5.9.2</version>
</dependency>
<dependency>
    <groupId>org.junit.platform</groupId>
    <artifactId>junit-platform-launcher</artifactId>
    <version>1.9.2</version>
</dependency>
<dependency>
    <groupId>org.junit.jupiter</groupId>
    <artifactId>junit-jupiter-engine</artifactId>
    <version>5.9.2</version>
</dependency>
<dependency>
    <groupId>org.devopology</groupId>
    <artifactId>test-engine</artifactId>
    <version>0.0.4</version>
</dependency>
```

Note: The test engine uses core JUnit 5 jars

Write a test...

Example 1 - `@ParameterSupplier` field

```
package org.devopology.test.engine.test.example;

import org.devopology.test.engine.api.AfterAll;
import org.devopology.test.engine.api.AfterEach;
import org.devopology.test.engine.api.BeforeAll;
import org.devopology.test.engine.api.BeforeEach;
import org.devopology.test.engine.api.Parameter;
import org.devopology.test.engine.api.ParameterSupplier;
import org.devopology.test.engine.api.Test;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Example test engine test... only runs from an IDE or via TestEnginRunner
 */
public class WithParameterSupplierField {

    private static class StringParameterSupplier {
        public static Collection<String> values() {
            Collection<String> collection = new ArrayList<>();
            for (int i = 0; i < 10; i++) {
                collection.add(String.valueOf(i));
            }
            return collection;
        }
    }

    // public be pubic and static - required
    // void return type
    // must return a Collection of the parameter type
    @ParameterSupplier
    public static Collection<String> VALUES = StringParameterSupplier.values();

    // must be public (non-static) - required
    @Parameter
    public String value;

    // must be public (non-static)
    // no arguments, void return type
    @BeforeAll
    public void beforeAll() {
        System.out.println("beforeAll()");
    }

    // must be public (non-static)
    // no arguments, void return type
    @BeforeEach
    public void beforeEach() {
        System.out.println("beforeEach()");
    }

    // must be public (non-static)
    // no arguments, void return type
    @Test
    public void test1() {
        System.out.println("test1(" + value + ")");
    }

    // must be public (non-static)
    // no arguments, void return type
    @Test
    public void test2() {
        System.out.println("test2(" + value + ")");
    }

    // must be public (non-static)
    // no arguments, void return type
    @AfterEach
    public void afterEach() {
        System.out.println("afterEach()");
    }

    // must be public (non-static)
    // no arguments, void return type
    @AfterAll
    public void afterAll() {
        System.out.println("afterAll()");
    }
}
```

Example 2 - `@ParameterSupplier` method

```
package org.devopology.test.engine.test.example;

import org.devopology.test.engine.api.AfterAll;
import org.devopology.test.engine.api.BeforeAll;
import org.devopology.test.engine.api.Parameter;
import org.devopology.test.engine.api.ParameterSupplier;
import org.devopology.test.engine.api.Test;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Example test engine test... only runs from an IDE or via the test engine ConsoleRunner
 */
public class WithParameterSupplierMethod {

    // must be public and static - required
    // no arguments
    // must return a Collection of the parameter type
    @ParameterSupplier
    public static Collection<String> values() {
        Collection<String> collection = new ArrayList<>();
        for(int i = 0; i < 10; i++) {
            collection.add(String.valueOf(i));
        }
        return collection;
    }

    // must be public (non-static) - required
    @Parameter
    public String value;

    // must be public (non-static)
    // no arguments, void return type
    @BeforeAll
    public void beforeAll() {
        System.out.println("beforeAll()");
    }

    // must be public (non-static)
    // no arguments, void return type
    @Test
    public void test1() {
        System.out.println("test1(" + value + ")");
    }

    // must be public (non-static)
    // no arguments, void return type
    @Test
    public void test2() {
        System.out.println("test2(" + value + ")");
    }

    // must be public (non-static)
    // no arguments, void return type
    @AfterAll
    public void afterAll() {
        System.out.println("afterAll()");
    }
}
```

Notes:

- While the annotation names are similar to standard JUnit 5 annotations, they are specific to the test engine. Use the correct imports.

Other examples

https://github.com/devopology/test-engine/tree/main/src/test/java/org/devopology/test/engine/test/example

---

# Design

TBD
