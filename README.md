[![Build](https://github.com/devopology/test-engine/actions/workflows/build.yml/badge.svg)](https://github.com/devopology/test-engine/actions/workflows/build.yml)
[![Code Grade](https://api.codiga.io/project/35659/status/svg)](https://app.codiga.io/hub/project/35659/test-engine)
[![Code Quality](https://api.codiga.io/project/35659/score/svg)](https://app.codiga.io/hub/project/35659/test-engine)

# Devopology Test Engine

The Devopology Test Engine is a JUnit 5 based test engine that allows for parameterized testing at the test class level.

## Why ?

Currently, JUnit 5 does not support parameterized tests at the test class level
- https://github.com/junit-team/junit5/issues/878

## Latest Releases

- General Availability (GA): [Devopology Test Engine v2.0.1](https://github.com/devopology/test-engine/releases/tag/v2.0.1) (TBD)

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
    <version>2.0.1</version>
  </dependency>
</dependencies>
```

To use the Test Engine `CsvSource`, you need to also include the uniVocity parsers jar

```xml
  <dependency>
    <groupId>com.univocity</groupId>
    <artifactId>univocity-parsers</artifactId>
    <version>2.9.1</version>
  </dependency>
```

**Notes**

- The Devopology Test Engine requires core JUnit 5 jars as dependencies

## Common Annotations

| Annotation                      | Scope  |  Required | Static | Example                                          |
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


- `@TestEngine.Order` can be used to control method order
  - Methods are sorted by the annotation value first, then alphabetically by the test method name
  - Method order is relative to other methods with the same annotation

## Additional Annotations

| Annotation                 | Scope  | Required | Usage                                                                               |
|----------------------------|--------|----------|-------------------------------------------------------------------------------------|
| `@TestEngine.BaseClass`    | class  | no       | Marks a test class as being a base class (skips direct execution)                   |
| `@TestEngine.Order(<int>)` | method | no       | Provides a way to order methods  relative to other methods with the same annotation |

## What is a `Parameter` ?

`Parameter` is an interface all parameter objects must implement to allow for parameter name and value resolution

- `@TestEngine.ParameterSupplier` must return a `Stream<Parameter>`


- `@TestEngine.ParameterSetter` requires single `Parameter` object


- The `Parameter` interface defines various static methods to wrap basic Java types, using the value as the name 
  - `boolean`
  - `byte`
  - `char`
  - `short`
  - `int`
  - `long`
  - `float`
  - `double`
  - `String`

The `Parameter` interface also has methods to wrap an Object

Example

```java
    @TestEngine.ParameterSupplier
    public static Stream<Parameter> parameters() {
        Collection<Parameter> collection = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            collection.add(
                    Parameter.of(
                            "Array [" + i + "]",
                            new String[] { String.valueOf(i), String.valueOf(i * 2) }));
        }
        return collection.stream();
    }
```

The value of the `Parameter` is a String[] array

```java
String[] values = paramater.value();
```

**Notes**

## Configuration values

The Devopology Test Engine has 5 configuration parameters

- thread count
  - Java system property `devopology.test.engine.thread.count`
  - Environment variable `DEVOPOLOGY_TEST_ENGINE_THREAD_COUNT`


- test class name include filter (regex)
  - Java system property `devopology.test.engine.test.class.include`
  - Environment variable `DEVOPOLOGY_TEST_ENGINE_TEST_CLASS_INCLUDE`


- test class name exclude filter (regex)
  - Java system property `devopology.test.engine.test.class.exclude`
  - Environment variable `DEVOPOLOGY_TEST_ENGINE_TEST_CLASS_EXCLUDE`


- test method name include filter (regex)
  - Java system property `devopology.test.engine.test.method.include`
  - Environment variable `DEVOPOLOGY_TEST_ENGINE_TEST_METHOD_INCLUDE`

- test method name exclude filter (regex)
  - Java system property `devopology.test.engine.test.method.exclude`
  - Environment variable `DEVOPOLOGY_TEST_ENGINE_TEST_METHOD_EXCLUDE`

Using a combination of the properties allows for running individual test classes / test methods

**Notes**

- Java system properties take precedence over environment variables


- If all test methods are excluded, then the test class will be excluded

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

GitHub's discussions is the current mechanism for help/support

## Contributing

Contributions to the Devopology Test Engine are both welcomed and appreciated.

The project uses a simplified GitFlow branching strategy
 - `main` is the latest release
 - `development` is the next release

For changes, you should...
- Create a branch based on `development`
- Make your changes
- Open a PR against `development`

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
  - The thread count can be changed by using a Java system property or environment variable

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