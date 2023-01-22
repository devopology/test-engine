/*
 * Copyright 2022-2023 Douglas Hoard
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.devopology.test.engine;

import org.devopology.test.engine.internal.EngineExecutionContext;
import org.devopology.test.engine.internal.TestEngineUtils;
import org.devopology.test.engine.internal.ThrowableCollector;
import org.devopology.test.engine.internal.descriptor.TestClassTestDescriptor;
import org.devopology.test.engine.internal.descriptor.TestMethodTestDescriptor;
import org.devopology.test.engine.internal.descriptor.TestParameterTestDescriptor;
import org.devopology.test.engine.internal.util.Switch;
import org.devopology.test.engine.internal.logger.Logger;
import org.devopology.test.engine.internal.logger.LoggerFactory;
import org.junit.platform.engine.EngineExecutionListener;
import org.junit.platform.engine.ExecutionRequest;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.support.descriptor.EngineDescriptor;

import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;

public class TestEngineExecutor {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestEngineExecutor.class);

    /**
     * Method to execute the execution request
     *
     * @param executionRequest
     */
    public void execute(ExecutionRequest executionRequest) {
        LOGGER.debug("execute()");

        EngineExecutionListener engineExecutionListener = executionRequest.getEngineExecutionListener();

        /*
        if (mode == Mode.CONSOLE) {
            engineExecutionListener = new PrintStreamEngineExecutionListener(engineExecutionListener, System.out);
        }
        */

        TestDescriptor rootTestDescriptor = executionRequest.getRootTestDescriptor();
        ThrowableCollector throwableCollector = new ThrowableCollector();

        logTestHierarchy(rootTestDescriptor, 0);

        EngineExecutionContext testEngineExecutionContext =
                new EngineExecutionContext(engineExecutionListener, throwableCollector);

        if (rootTestDescriptor instanceof EngineDescriptor) {
            for (TestDescriptor testDescriptor : rootTestDescriptor.getChildren()) {
                execute((TestClassTestDescriptor) testDescriptor, testEngineExecutionContext);
            }
        } else if (rootTestDescriptor instanceof TestClassTestDescriptor) {
            execute((TestClassTestDescriptor) rootTestDescriptor, testEngineExecutionContext);
        }
    }

    /**
     * Method to execute a TestClassTestDescriptor
     *
     * @param testClassTestDescriptor
     * @param testEngineExecutionContext
     */
    private void execute(
            TestClassTestDescriptor testClassTestDescriptor,
            EngineExecutionContext testEngineExecutionContext) {

        // If test class descriptor is part of a hierarchy (has siblings) notify listeners
        if (TestEngineUtils.hasSiblings(testClassTestDescriptor)) {
            testEngineExecutionContext.getEngineExecutionListener().executionStarted(testClassTestDescriptor);
        }

        ThrowableCollector throwableCollector = new ThrowableCollector();

        try {
            Class<?> testClass = testClassTestDescriptor.getTestClass();
            Constructor<?> testClassConstructor = testClass.getDeclaredConstructor((Class<?>[]) null);
            Object testInstance = testClassConstructor.newInstance((Object[]) null);
            testEngineExecutionContext.setTestInstance(testInstance);

            // Execute each TestParameterTestDescriptor
            Set<? extends TestDescriptor> children = testClassTestDescriptor.getChildren();
            for (TestDescriptor testDescriptor : children) {
                if (testDescriptor instanceof TestParameterTestDescriptor) {
                    execute((TestParameterTestDescriptor) testDescriptor, testEngineExecutionContext);
                }
            }

            // Remove the test instance to allow garbage collection
            testEngineExecutionContext.setTestInstance(null);
        } catch (Throwable t) {
            t = resolve(t);
            printStackTrace(t, System.err);
            throwableCollector.add(t);
        } finally {
            flush();

            if (throwableCollector.isEmpty()) {
                // If test class descriptor is part of a hierarchy (has siblings) notify listeners
                if (TestEngineUtils.hasSiblings(testClassTestDescriptor)) {
                    testEngineExecutionContext.getEngineExecutionListener().executionFinished(
                            testClassTestDescriptor, TestExecutionResult.successful());
                }
            } else {
                // If test class descriptor is part of a hierarchy (has siblings) notify listeners
                if (TestEngineUtils.hasSiblings(testClassTestDescriptor)) {
                    testEngineExecutionContext.getEngineExecutionListener().executionFinished(
                            testClassTestDescriptor,
                            TestExecutionResult.failed(throwableCollector.getFirstThrowable()));
                }
            }

            testEngineExecutionContext.getThrowableCollector().addAll(throwableCollector);
        }
    }

    /**
     * Method to execute a TestParameterTestDescriptor
     *
     * @param testParameterTestDescriptor
     * @param testEngineExecutionContext
     */
    private void execute(
            TestParameterTestDescriptor testParameterTestDescriptor,
            EngineExecutionContext testEngineExecutionContext) {
        testEngineExecutionContext.getEngineExecutionListener().executionStarted(testParameterTestDescriptor);

        ThrowableCollector throwableCollector = new ThrowableCollector();
        Class<?> testClass = testParameterTestDescriptor.getTestClass();
        Object testInstance = testEngineExecutionContext.getTestInstance();
        Object testParameter = testParameterTestDescriptor.getTestParameter();
        List<Field> testParameterfields = TestEngineUtils.getParameterFields(testClass);

        try {
            testParameterfields.get(0).set(testInstance, testParameter);

            for (Method beforeAllMethod : TestEngineUtils.getBeforeAllMethods(testClass)) {
                beforeAllMethod.invoke(testInstance, (Object[]) null);
            }
        } catch (Throwable t) {
            t = resolve(t);
            printStackTrace(t, System.err);
            throwableCollector.add(t);
        } finally {
            flush();
        }

        if (throwableCollector.isEmpty()) {
            Set<? extends TestDescriptor> children = testParameterTestDescriptor.getChildren();
            for (TestDescriptor testDescriptor : children) {
                if (testDescriptor instanceof TestMethodTestDescriptor) {
                    execute((TestMethodTestDescriptor) testDescriptor, testEngineExecutionContext);
                }
            }
        }

        try {
            for (Method afterAllMethod : TestEngineUtils.getAfterAllMethods(testClass)) {
                afterAllMethod.invoke(testInstance, (Object[]) null);
            }
        } catch (Throwable t) {
            t = resolve(t);
            printStackTrace(t, System.err);
            throwableCollector.add(t);
        } finally {
            flush();
        }

        if (throwableCollector.isEmpty()) {
            testEngineExecutionContext.getEngineExecutionListener().executionFinished(
                    testParameterTestDescriptor, TestExecutionResult.successful());
        } else {
            testEngineExecutionContext.getEngineExecutionListener().executionFinished(
                    testParameterTestDescriptor, TestExecutionResult.failed(throwableCollector.getFirstThrowable()));
        }

        testEngineExecutionContext.getThrowableCollector().addAll(throwableCollector);
    }

    /**
     * Method to execute a TestMethodTestDescriptor
     *
     * @param testMethodTestDescriptor
     * @param testEngineExecutionContext
     */
    private void execute(
            TestMethodTestDescriptor testMethodTestDescriptor,
            EngineExecutionContext testEngineExecutionContext) {
        testEngineExecutionContext.getEngineExecutionListener().executionStarted(testMethodTestDescriptor);

        ThrowableCollector throwableCollector = new ThrowableCollector();
        Class<?> testClass = testMethodTestDescriptor.getTestClass();
        Object testInstance = testEngineExecutionContext.getTestInstance();

        try {
            for (Method beforeEachMethod : TestEngineUtils.getBeforeEachMethods(testClass)) {
                beforeEachMethod.invoke(testInstance, (Object[]) null);
            }
        } catch (Throwable t) {
            t = resolve(t);
            printStackTrace(t, System.err);
            throwableCollector.add(t);
        } finally {
            flush();
        }

        try {
            Method testMethod = testMethodTestDescriptor.getTestMethod();
            testMethod.invoke(testInstance, (Object[]) null);
        } catch (Throwable t) {
            t = resolve(t);
            printStackTrace(t, System.err);
            throwableCollector.add(t);
        } finally {
            flush();
        }

        try {
            for (Method afterEachMethod : TestEngineUtils.getAfterEachMethods(testClass)) {
                afterEachMethod.invoke(testInstance, (Object[]) null);
            }
        } catch (Throwable t) {
            t = resolve(t);
            printStackTrace(t, System.err);
            throwableCollector.add(t);
        } finally {
            flush();
        }

        if (throwableCollector.isEmpty()) {
            testEngineExecutionContext.getEngineExecutionListener().executionFinished(
                    testMethodTestDescriptor, TestExecutionResult.successful());
        } else {
            testEngineExecutionContext.getEngineExecutionListener().executionFinished(
                    testMethodTestDescriptor, TestExecutionResult.failed(throwableCollector.getFirstThrowable()));
        }

        testEngineExecutionContext.getThrowableCollector().addAll(throwableCollector);
    }

    /**
     * Method to log the test hierarchy
     *
     * @param testDescriptor
     * @param indent
     */
    private static void logTestHierarchy(TestDescriptor testDescriptor, int indent) {
        if (indent == 0) {
            LOGGER.debug("Test hierarchy...");
        }

        final StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < indent; i++) {
            stringBuilder.append(" ");
        }

        Switch.switchType(testDescriptor,
                Switch.switchCase(
                        TestMethodTestDescriptor.class,
                        testMethodTestDescriptor ->
                                stringBuilder
                                        .append("method -> ")
                                        .append(testMethodTestDescriptor.getDisplayName())
                                        .append("()")),
                Switch.switchCase(
                        TestParameterTestDescriptor.class,
                        testParameterTestDescriptor ->
                                stringBuilder
                                        .append("parameter -> ")
                                        .append(testParameterTestDescriptor.getTestParameter())),
                Switch.switchCase(
                        TestClassTestDescriptor.class,
                        testClassTestDescriptor ->
                                stringBuilder.append("class -> " + testDescriptor.getDisplayName())),
                Switch.switchCase(
                        EngineDescriptor.class,
                        engineDescriptor -> stringBuilder.append("engine -> " + testDescriptor.getDisplayName())));

        LOGGER.debug(stringBuilder.toString());

        for (TestDescriptor child : testDescriptor.getChildren()) {
            logTestHierarchy(child, indent + 2);
        }
    }

    private static Throwable resolve(Throwable t) {
        if (t instanceof InvocationTargetException) {
            return t.getCause();
        } else {
            return t;
        }
    }

    public static void printStackTrace(Throwable t, PrintStream printStream) {
        printStream.println(t.getClass().getName() + ": " + t.getMessage());

        StackTraceElement[] stackTraceElements = t.getStackTrace();
        if (stackTraceElements != null) {
            for (StackTraceElement stackTraceElement : stackTraceElements) {
                if (stackTraceElement.getClassName().startsWith("org.devopology.test.engine")) {
                    break;
                } else {
                    printStream.println("    at " + stackTraceElement);
                }
            }
        }
    }

    /**
     * Method to flush the System.err stream, which seems to flush the System.out stream
     * Without the flush, IntelliJ seems to "miss" System.out.println() calls in test methods
     */
    private static void flush() {
        System.err.flush();
    }
}
