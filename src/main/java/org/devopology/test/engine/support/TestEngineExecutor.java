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

package org.devopology.test.engine.support;

import org.devopology.test.engine.support.descriptor.TestEngineClassTestDescriptor;
import org.devopology.test.engine.support.descriptor.TestEngineParameterTestDescriptor;
import org.devopology.test.engine.support.descriptor.TestEngineTestMethodTestDescriptor;
import org.devopology.test.engine.support.logger.Logger;
import org.devopology.test.engine.support.logger.LoggerFactory;
import org.devopology.test.engine.support.util.Switch;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * Method to execute an ExecutionRequest
 */
public class TestEngineExecutor {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestEngineExecutor.class);

    private int threadCount;
    private ExecutorService executorService;

    public TestEngineExecutor(int threadCount) {
        this.threadCount = threadCount;
        this.executorService = Executors.newFixedThreadPool(threadCount, new NamedThreadFactory());
    }

    /**
     * Method to execute the ExecutionRequest
     *
     * @param executionRequest
     */
    public void execute(ExecutionRequest executionRequest) {
        LOGGER.debug("execute(ExecutionRequest)");

        EngineExecutionListener engineExecutionListener = executionRequest.getEngineExecutionListener();

        TestDescriptor rootTestDescriptor = executionRequest.getRootTestDescriptor();
        List<TestExecutionResult> testExecutionResultList = Collections.synchronizedList(new ArrayList<>());

        logTestHierarchy(rootTestDescriptor, 0);

        TestEngineExecutionContext testEngineExecutionContext =
                new TestEngineExecutionContext(engineExecutionListener, testExecutionResultList);

        if (rootTestDescriptor instanceof EngineDescriptor) {
            CountDownLatch countDownLatch = new CountDownLatch(rootTestDescriptor.getChildren().size());

            for (TestDescriptor testDescriptor : rootTestDescriptor.getChildren()) {
                executorService.submit(() -> {
                    TestEngineExecutionContext testEngineExecutionContext1 =
                            new TestEngineExecutionContext(engineExecutionListener, testExecutionResultList);

                    execute((TestEngineClassTestDescriptor) testDescriptor, testEngineExecutionContext1, countDownLatch);
                });
            }

            try {
                countDownLatch.await();
            } catch (InterruptedException e) {
                LOGGER.error("Exception waiting for tests", e);
            }
        } else if (rootTestDescriptor instanceof TestEngineClassTestDescriptor) {
            CountDownLatch countDownLatch = new CountDownLatch(1);
            execute((TestEngineClassTestDescriptor) rootTestDescriptor, testEngineExecutionContext, countDownLatch);
            try {
                countDownLatch.await();
            } catch (InterruptedException e) {
                LOGGER.error("Exception waiting for tests", e);
            }
        }
    }

    /**
     * Method to execute a TestEngineClassTestDescriptor
     *
     * @param testEngineClassTestDescriptor
     * @param testEngineExecutionContext
     */
    private void execute(
            TestEngineClassTestDescriptor testEngineClassTestDescriptor,
            TestEngineExecutionContext testEngineExecutionContext,
            CountDownLatch countDownLatch) {
        LOGGER.debug("execute(TestEngineClassTestDescriptor, TestEngineExecutionContext)");

        // If test class descriptor is part of a hierarchy (has siblings) notify listeners
        if (TestEngineUtils.hasSiblings(testEngineClassTestDescriptor)) {
            testEngineExecutionContext.getEngineExecutionListener().executionStarted(testEngineClassTestDescriptor);
        }

        List<TestExecutionResult> testExecutionResultList = testEngineClassTestDescriptor.getTestExecutionResultList();
        testExecutionResultList.clear();

        try {
            Class<?> testClass = testEngineClassTestDescriptor.getTestClass();
            Constructor<?> testClassConstructor = testClass.getDeclaredConstructor((Class<?>[]) null);
            Object testInstance = testClassConstructor.newInstance((Object[]) null);
            testEngineExecutionContext.setTestInstance(testInstance);

            // Execute each TestParameterTestDescriptor
            Set<? extends TestDescriptor> children = testEngineClassTestDescriptor.getChildren();
            for (TestDescriptor testDescriptor : children) {
                if (testDescriptor instanceof TestEngineParameterTestDescriptor) {
                    TestEngineParameterTestDescriptor testEngineParameterTestDescriptor = (TestEngineParameterTestDescriptor) testDescriptor;
                    execute(testEngineParameterTestDescriptor, testEngineExecutionContext);
                    testExecutionResultList.addAll(testEngineParameterTestDescriptor.getTestExecutionResultList());
                }
            }

            // Remove the test instance to allow garbage collection
            testEngineExecutionContext.setTestInstance(null);
        } catch (Throwable t) {
            t = resolve(t);
            printStackTrace(t, System.err);
            testExecutionResultList.add(TestExecutionResult.failed(t));
        } finally {
            flush();

            testEngineExecutionContext.getTestExecutionResultList().addAll(testExecutionResultList);

            // If test class descriptor is part of a hierarchy (has siblings) notify listeners
            if (TestEngineUtils.hasSiblings(testEngineClassTestDescriptor)) {
                if (testExecutionResultList.isEmpty()) {
                    testEngineExecutionContext.getEngineExecutionListener().executionFinished(
                            testEngineClassTestDescriptor, TestExecutionResult.successful());
                } else {
                    testEngineExecutionContext.getEngineExecutionListener().executionFinished(
                            testEngineClassTestDescriptor,
                            testExecutionResultList.get(0));
                }
            }
        }

        countDownLatch.countDown();
    }

    /**
     * Method to execute a TestEngineParameterTestDescriptor
     *
     * @param testEngineParameterTestDescriptor
     * @param testEngineExecutionContext
     */
    private void execute(
            TestEngineParameterTestDescriptor testEngineParameterTestDescriptor,
            TestEngineExecutionContext testEngineExecutionContext) {
        LOGGER.debug("execute(TestEngineParameterTestDescriptor, TestEngineParameterTestDescriptor)");

        testEngineExecutionContext.getEngineExecutionListener().executionStarted(testEngineParameterTestDescriptor);

        List<TestExecutionResult> testExecutionResultList = testEngineParameterTestDescriptor.getTestExecutionResultList();
        testExecutionResultList.clear();

        Class<?> testClass = testEngineParameterTestDescriptor.getTestClass();
        Object testInstance = testEngineExecutionContext.getTestInstance();
        Object testParameter = testEngineParameterTestDescriptor.getTestParameter();
        List<Field> testParameterFields = TestEngineUtils.getParameterFields(testClass);

        try {
            testParameterFields.get(0).set(testInstance, testParameter);

            for (Method beforeAllMethod : TestEngineUtils.getBeforeAllMethods(testClass)) {
                beforeAllMethod.invoke(testInstance, (Object[]) null);
            }
        } catch (Throwable t) {
            t = resolve(t);
            printStackTrace(t, System.err);
            testExecutionResultList.add(TestExecutionResult.failed(t));
        } finally {
            flush();
        }

        if (testExecutionResultList.isEmpty()) {
            Set<? extends TestDescriptor> children = testEngineParameterTestDescriptor.getChildren();
            for (TestDescriptor testDescriptor : children) {
                if (testDescriptor instanceof TestEngineTestMethodTestDescriptor) {
                    TestEngineTestMethodTestDescriptor testEngineTestMethodTestDescriptor = (TestEngineTestMethodTestDescriptor) testDescriptor;
                    execute(testEngineTestMethodTestDescriptor, testEngineExecutionContext);
                    testExecutionResultList.addAll(testEngineTestMethodTestDescriptor.getTestExecutionResultList());
                }
            }
        } else {
            Set<? extends TestDescriptor> children = testEngineParameterTestDescriptor.getChildren();
            for (TestDescriptor testDescriptor : children) {
                if (testDescriptor instanceof TestEngineTestMethodTestDescriptor) {
                    testEngineExecutionContext.getEngineExecutionListener().executionSkipped(testDescriptor, "@BeforeAll method exception");
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
            testExecutionResultList.add(TestExecutionResult.failed(t));
        } finally {
            flush();
        }

        if (testExecutionResultList.isEmpty()) {
            testEngineExecutionContext.getEngineExecutionListener().executionFinished(
                    testEngineParameterTestDescriptor, TestExecutionResult.successful());
        } else {
            testEngineExecutionContext.getEngineExecutionListener().executionFinished(
                    testEngineParameterTestDescriptor, testExecutionResultList.get(0));
        }

        testEngineExecutionContext.getTestExecutionResultList().addAll(testExecutionResultList);
    }

    /**
     * Method to execute a TestMethodTestDescriptor
     *
     * @param testEngineTestMethodTestDescriptor
     * @param testEngineExecutionContext
     */
    private void execute(
            TestEngineTestMethodTestDescriptor testEngineTestMethodTestDescriptor,
            TestEngineExecutionContext testEngineExecutionContext) {
        testEngineExecutionContext.getEngineExecutionListener().executionStarted(testEngineTestMethodTestDescriptor);

        List<TestExecutionResult> testExecutionResultList = testEngineTestMethodTestDescriptor.getTestExecutionResultList();
        testExecutionResultList.clear();

        Class<?> testClass = testEngineTestMethodTestDescriptor.getTestClass();
        Object testInstance = testEngineExecutionContext.getTestInstance();

        try {
            for (Method beforeEachMethod : TestEngineUtils.getBeforeEachMethods(testClass)) {
                beforeEachMethod.invoke(testInstance, (Object[]) null);
            }
        } catch (Throwable t) {
            t = resolve(t);
            printStackTrace(t, System.err);
            testExecutionResultList.add(TestExecutionResult.failed(t));
        } finally {
            flush();
        }

        try {
            Method testMethod = testEngineTestMethodTestDescriptor.getTestMethod();
            testMethod.invoke(testInstance, (Object[]) null);
        } catch (Throwable t) {
            t = resolve(t);
            printStackTrace(t, System.err);
            testExecutionResultList.add(TestExecutionResult.failed(t));
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
            testExecutionResultList.add(TestExecutionResult.failed(t));
        } finally {
            flush();
        }

        if (testExecutionResultList.isEmpty()) {
            testEngineExecutionContext.getEngineExecutionListener().executionFinished(
                    testEngineTestMethodTestDescriptor, TestExecutionResult.successful());
        } else {
            testEngineExecutionContext.getEngineExecutionListener().executionFinished(
                    testEngineTestMethodTestDescriptor, testExecutionResultList.get(0));
        }

        //testEngineExecutionContext.getTestExecutionResultList().addAll(testExecutionResultList);
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
                        TestEngineTestMethodTestDescriptor.class,
                        testMethodTestDescriptor ->
                                stringBuilder
                                        .append("method -> ")
                                        .append(testMethodTestDescriptor.getDisplayName())
                                        .append("()")),
                Switch.switchCase(
                        TestEngineParameterTestDescriptor.class,
                        testParameterTestDescriptor ->
                                stringBuilder
                                        .append("parameter -> ")
                                        .append(testParameterTestDescriptor.getTestParameter())),
                Switch.switchCase(
                        TestEngineClassTestDescriptor.class,
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
        System.out.flush();
    }

    private static class NamedThreadFactory implements ThreadFactory {

        private int threadId = 1;

        @Override
        public Thread newThread(Runnable r) {
            String threadName;
            synchronized (this) {
                threadName = "test-engine-" + this.threadId;
                this.threadId++;
            }

            Thread thread = new Thread(r);
            thread.setName(threadName);
            thread.setDaemon(true);
            return thread;
        }
    }
}
