[![Build](https://github.com/devopology/test-engine/actions/workflows/build.yml/badge.svg)](https://github.com/devopology/test-engine/actions/workflows/build.yml)
[![Code Grade](https://api.codiga.io/project/35659/status/svg)](https://app.codiga.io/hub/project/35659/test-engine)
[![Code Quality](https://api.codiga.io/project/35659/score/svg)](https://app.codiga.io/hub/project/35659/test-engine)

# Devopology Test Engine

The Devopology Test Engine is a JUnit 5 based test engine that allows for parameterized testing at the test class level.

## Why ?

Currently, JUnit 5 does not support parameterized tests at the test class level
- https://github.com/junit-team/junit5/issues/878

## Latest Releases

- General Availability (GA): [Test Engine v1.0.1](https://github.com/devopology/test-engine/releases/tag/v1.0.1) (2023-03-04)

**Notes**

- **v1.0.x versions are NOT backward compatible with v0.0.Z versions**


- v0.0.Z releases are considered deprecated

## Getting Help

Github discussions is the current mechanism

## Contributing

Contributions to the Test Engine are both welcomed and appreciated.

## Common Annotations


| Annotation                      | Scope            | Required | Static | Examples                                                                                              |
|---------------------------------|------------------|----------|--------|-------------------------------------------------------------------------------------------------------|
| `@TestEngine.ParameterSupplier` | field or method  | yes      | yes    | `public static Collection<String> PARAMETERS;` <br/> `public static Collection<String> parameters();` |
| `@TestEngine.ParameterInject`   | field            | yes      | no     | `public String value;` <br/> `protected String value;`                                                |
| `@TestEngine.BeforeClass`       | method           | no       | yes    | `public static void beforeClass();`                                                                   |
| `@TestEngine.BeforeAll`         | method           | no       | no     | `public void beforeAll();`                                                                            |
| `@TestEngine.BeforeEach`        | method           | no       | no     | `public void beforeEach();`                                                                           |
| `@TestEngine.Test`              | method           | yes      | no     | `public void test();`                                                                                 |
| `@TestEngine.AfterEach`         | method           | no       | no     | `public void afterEach();`                                                                            |
| `@TestEngine.AfterAll`          | method           | no       | no     | `public void afterAll();`                                                                             |
| `@TestEngine.AfterClass`        | method           | no       | yes    | `public static void afterClass();`                                                                    |


# State Machine Flow

Basic flow...

```
 Scan all classpath jars for test classes that contains a method annotated with "@TestEngine.Test"
 
 for (each test class in the test class list) {
 
    for each test class, create a thread
    
    thread {
    
        call "@TestEngine.ParameterSupplier" field or method to get a parameter collection
    
        execute "@TestEngine.BeforeClass" methods 
     
        create a single instance of the test class
        
        for (each parameter in the parameter collection) {
        
            set the "@TestEngine.ParameterInject" field value
            
            execute "@TestEngine.BeforeAll" methods
            
            for (each "@TestEngine.Test" method in the test class) {
            
                execute "@TestEngine.BeforeEach" methods
            
                execute "@TestEngine.Test" method
                
                execute "@TestEngine.AfterEach" methods
            }
            
            execute "@TestEngine.AfterAll" methods
            
            set the "@TestEngine.ParameterInject" field to null
        }
        
        execute "@TestEngine.AfterClass" methods
    }
 }
```

**Notes**

- The type returned in the `@TestEngine.ParameterSupplier` `Collection` must match the type of the `@TestEngine.ParameterInject` field


- `Named` is a special case. The `@TestEngine.ParameterInject` field type should match the type of Object wrapped by the `Named` instance


- Each parameterized test class will be executed sequentially, but different test classes are executed in parallel threads
  - By default, thread count is equal to number of available processors as reported to Java
  - The thread count can be changed by using a Java system property `devopology.test.engine.thread.count=<THREAD COUNT>`

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

Add the Junit 5 and Devopology Test Engine jar dependencies...

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
    <version>1.0.1</version>
</dependency>
```

Set up Maven to use the test engine

**Notes**

- The Test Engine uses core JUnit 5 jars as dependencies

## Command Line (standalone) Usage

The Test Engine jar has the ability to run as a standalone executable, provided all dependencies are on the classpath

Example:

```bash
java \
  -cp "<directory of all your dependencies>/*" \
  org.devopology.test.engine.TestEngine
```

The Test Engine [POM](https://github.com/devopology/test-engine/blob/main/pom.xml) uses this approach and is typically easier than configuring the Mave Surefire plugin

Write a test...

Example:

```java
package org.devopology.test.engine.test.example;

import org.devopology.test.engine.api.TestEngine;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Example test
 */
public class ParameterSupplierFieldTest {

    @TestEngine.ParameterInject
    public String parameter;

    @TestEngine.ParameterSupplier
    public static Collection<String> PARAMETERS = TestParameterSupplier.values();

    @TestEngine.BeforeClass
    public static void beforeClas() {
        System.out.println("beforeClass()");
    }

    @TestEngine.BeforeAll
    public void beforeAll() {
        System.out.println("beforeAll()");
    }

    @TestEngine.BeforeEach
    public void beforeEach() {
        System.out.println("beforeEach()");
    }

    @TestEngine.Test
    public void test1() {
        System.out.println("test1(" + parameter + ")");
    }

    @TestEngine.Test
    public void test2() {
        System.out.println("test2(" + parameter + ")");
    }

    @TestEngine.AfterEach
    public void afterEach() {
        System.out.println("afterEach()");
    }

    @TestEngine.AfterAll
    public void afterAll() {
        System.out.println("afterAll()");
    }

    @TestEngine.AfterClass
    public static void afterClass() {
        System.out.println("afterClass()");
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

- While the annotation names are similar to standard JUnit 5 annotations, they are specific to the Test Engine. Use the correct imports

# Building

You need Java 11 or greater to build

```shell
git clone https://github.com/devopology/test-engine
cd test-engine
mvn clean package
```

To install to your local repository

```shell
mvn clean package install
```
