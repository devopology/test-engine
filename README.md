[![Build](https://github.com/devopology/test-engine/actions/workflows/build.yml/badge.svg)](https://github.com/devopology/test-engine/actions/workflows/build.yml)
[![Code Grade](https://api.codiga.io/project/35659/status/svg)](https://app.codiga.io/hub/project/35659/test-engine)
[![Code Quality](https://api.codiga.io/project/35659/score/svg)](https://app.codiga.io/hub/project/35659/test-engine)

# Devopology Test Engine

The Devopology Test Engine is a JUnit 5 based test engine that allows for parameterized testing at the test class level.

## Why ?

Currently, JUnit 5 does not support parameterized tests at the test class level
- https://github.com/junit-team/junit5/issues/878

## Latest Releases

- General Availability (GA): [Devopology Test Engine v2.0.0](https://github.com/devopology/test-engine/releases/tag/v2.0.0) (2023-03-08)

**Notes**

- **v2.0.x versions are NOT backward compatible with v1.Y.Z versions**

## Maven

Add the Devopology Test Engine Maven repository to your `pom.xml` file...

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
<dependencies>
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
    <version>2.0.0</version>
  </dependency>
</dependencies>
```

**Notes**

- The Devopology Test Engine requires core JUnit 5 jars as dependencies

## Common Annotations

| Annotation                      | Scope  |  Required | Static | Examples                                         |
|---------------------------------|--------|-----------|--------|--------------------------------------------------|
| `@TestEngine.ParameterSupplier` | method | yes       | yes    | `public static Stream<Parameter> parameters();`  |
| `@TestEngine.ParameterSetter`   | method | yes       | no     | `public void setParameter(Parameter parameter);` |
| `@TestEngine.BeforeClass`       | method | no        | yes    | `public static void beforeClass();`              |
| `@TestEngine.BeforeAll`         | method | no        | no     | `public void beforeAll();`                       |
| `@TestEngine.BeforeEach`        | method | no        | no     | `public void beforeEach();`                      |
| `@TestEngine.Test`              | method | yes       | no     | `public void test();`                            |
| `@TestEngine.AfterEach`         | method | no        | no     | `public void afterEach();`                       |
| `@TestEngine.AfterAll`          | method | no        | no     | `public void afterAll();`                        |
| `@TestEngine.AfterClass`        | method | no        | yes    | `public static void afterClass();`               |

**NOTES**

- `public` and `protected` methods are supported for `@TestEngine.X` annotations


- By default, methods are executed in alphabetical order based on a method name, regardless of where they are declared (class or superclasses)


- `@TestEngine.Test.Order` can be used to control **test method order**
  - Methods are sorted by the annotation value first, then alphabetically by the test method name

## Additional Annotations

| Annotation                      | Scope       | Required | Usage                                                             |
|---------------------------------|-------------|----------|-------------------------------------------------------------------|
| `@TestEngine.BaseClass`         | class       | no       | Marks a test class as being a base class (skips direct execution) |
 | `@TestEngine.Test.Order(<int>)` | test method | no       | Provides an order index for an `@TestEngine.Test` method          |

## What is a `Parameter` ?

`Parameter` is an interface all parameter objects must implement to allow for parameter name and value resolution

- `@TestEngine.ParameterSupplier` must return a `Stream<Parameter>`


- `@TestEngine.ParameterSetter` requires single `Parameter` object


- The `Parameter` interface defines various static methods to wrap basic Java types, using the value as the name 
  - `boolean`
  - `byte`
  - `short`
  - `int`
  - `long`
  - `float`
  - `double`
  - `String` 

## Example Usage

Example:

```java
package org.devopology.test.engine.test.example;

import org.devopology.test.engine.api.Parameter;
import org.devopology.test.engine.api.TestEngine;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Stream;

/**
 * Example test
 */
public class ParameterTest {

  private Parameter parameter;

  @TestEngine.ParameterSupplier
  public static Stream<Parameter> parameters() {
    Collection<Parameter> collection = new ArrayList<>();

    for (int i = 0; i < 10; i++) {
      int value = i * 3;
      collection.add(Parameter.of("argument(" + i + ") = " + value, String.valueOf(value)));
    }

    return collection.stream();
  }

  @TestEngine.ParameterSetter
  public void setParameter(Parameter parameter) {
    this.parameter = parameter;
  }

  @TestEngine.BeforeAll
  public void beforeAll() {
    System.out.println("beforeAll()");
  }

  @TestEngine.Test
  public void test1() {
    System.out.println("test1(" + parameter.value() + ")");
  }

  @TestEngine.Test
  public void test2() {
    System.out.println("test2(" + parameter.value() + ")");
  }

  @TestEngine.AfterAll
  public void afterAll() {
    System.out.println("afterAll()");
  }
}
```

Additional test examples...

https://github.com/devopology/test-engine/tree/main/src/test/java/org/devopology/test/engine/test/example


## Getting Help

Github discussions is the current mechanism for help/support

## Contributing

Contributions to the Devopology Test Engine are both welcomed and appreciated.

## Design

State Machine flow...

```
 Scan all classpath jars for test classes that contains a method annotated with "@TestEngine.Test"
 
 for (each test class in the Collection<Class>) {
 
    for each test class, create a thread
    
    thread {
    
        call "@TestEngine.ParameterSupplier" method to get a Stream<Parameter>
    
        execute "@TestEngine.BeforeClass" methods 
     
        create a single instance of the test class
        
        for (each Parameter in the Stream<Parameter>) {
        
            execute the "@TestEngine.ParameterSetter" method with the Parameter value
            
            execute "@TestEngine.BeforeAll" methods
            
            for (each "@TestEngine.Test" method in the test class) {
            
                execute "@TestEngine.BeforeEach" methods
            
                execute "@TestEngine.Test" method
                
                execute "@TestEngine.AfterEach" methods
            }
            
            execute "@TestEngine.AfterAll" method
        }
        
        execute "@TestEngine.AfterClass" methods
    }
 }
```

**Notes**

- Each parameterized test class will be executed sequentially, but different test classes are executed in parallel threads
  - By default, thread count is equal to number of available processors as reported to Java
  - The thread count can be changed by using a Java system property `devopology.test.engine.thread.count=<THREAD COUNT>`

## Command Line (standalone) Usage

The Devopology Test Engine jar has the ability to run as a standalone executable, provided all dependencies are on the classpath

Example:

```bash
java \
  -cp "<directory of all your dependencies>/*" \
  org.devopology.test.engine.TestEngine
```

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