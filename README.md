[![Build](https://github.com/devopology/test-engine/actions/workflows/build.yml/badge.svg)](https://github.com/devopology/test-engine/actions/workflows/build.yml)
[![Code Grade](https://api.codiga.io/project/35659/status/svg)](https://app.codiga.io/hub/project/35659/test-engine)
[![Code Quality](https://api.codiga.io/project/35659/score/svg)](https://app.codiga.io/hub/project/35659/test-engine)

# Devopology Test Engine

The Devopology Test Engine is a JUnit 5 based test engine that allows for parameterized testing at the test class level.

## Why ?

Currently, JUnit 5 does not support parameterized tests at the test class level
- https://github.com/junit-team/junit5/issues/878

## Maven Usage

Add the Devopology Maven repository to your `pom.xml` file...

```xml
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

```xml
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
    <version>0.0.10</version>
</dependency>
```

Set up Maven to use the test engine

**Notes**

- The test engine uses core JUnit 5 jars as dependencies

# Command Line (standalone) Usage

The test engine jar has the ability to run as a standalone executable, provided all dependencies are on the classpath

Example:

```bash
java \
  -cp "<directory of all your dependencies>/*" \
  org.devopology.test.engine.TestEngine
```

The test engine [POM](https://github.com/devopology/test-engine/blob/main/pom.xml) uses this approach and is typically easier than configuring the Mave Surefire plugin

---

Write a test...

Example:

```java
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
 * Example test
 */
public class ParameterSupplierFieldTest {

    @Parameter
    public String parameter;
    
    @ParameterSupplier
    public static Collection<String> PARAMETERS = TestParameterSupplier.values();

    @BeforeAll
    public void beforeAll() {
        System.out.println("beforeAll()");
    }

    @BeforeEach
    public void beforeEach() {
        System.out.println("beforeEach()");
    }

    @Test
    public void test1() {
        System.out.println("test1(" + parameter + ")");
    }

    @Test
    public void test2() {
        System.out.println("test2(" + parameter + ")");
    }

    @AfterEach
    public void afterEach() {
        System.out.println("afterEach()");
    }

    @AfterAll
    public void afterAll() {
        System.out.println("afterAll()");
    }

    private static class TestParameterSupplier {

        public static Collection<String> values() {
            Collection<String> collection = new ArrayList<>();

            for (int i = 0; i < 10; i++) {
                collection.add(String.valueOf(i));
            }

            return collection;
        }
    }
}
```

Other test examples

https://github.com/devopology/test-engine/tree/main/src/test/java/org/devopology/test/engine/test/example

**Notes**

- While the annotation names are similar to standard JUnit 5 annotations, they are specific to the test engine. Use the correct imports.


---

# Design

The basic flow...

```
 Scan all classpath jars for classes that contains a method annotated with "@Test"
 
 for (class : class list) {
 
    call "@ParameterSupplier" field or method to get a parameter collection
 
    create a single instance of the class
    
    for (parameter : parameter collection) {
    
        set the "@Parameter" field value
        
        call "@BeforeAll" methods
        
        for (method : class "@Test" method list) {
        
            call "@BeforeEach" methods
        
            call "@Test" method
            
            call "@AfterEach" methods
        }
        
        call "@AfterAll" methods
        
        set the "@Parameter" field to null
    }
 }
```

**Notes**

- The type returned in the `@ParameterSupplier` `Collection` must match the type of the `@Parameter` field


- `Named` is a special case. The `Parameter` field type should match the type of Object wrapped by the `Named` instance
