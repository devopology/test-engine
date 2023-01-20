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
import org.devopology.test.engine.internal.PrintStreamEngineExecutionListener;
import org.devopology.test.engine.internal.TestClassConfigurationException;
import org.devopology.test.engine.internal.ThrowableCollector;
import org.devopology.test.engine.internal.descriptor.TestClassTestDescriptor;
import org.devopology.test.engine.internal.descriptor.TestMethodTestDescriptor;
import org.devopology.test.engine.internal.descriptor.TestParameterTestDescriptor;
import org.devopology.test.engine.logger.Logger;
import org.devopology.test.engine.logger.LoggerFactory;
import org.devopology.test.engine.internal.util.Switch;
import org.devopology.test.engine.internal.TestEngineUtils;
import org.junit.platform.commons.support.ReflectionSupport;
import org.junit.platform.engine.DiscoverySelector;
import org.junit.platform.engine.EngineDiscoveryRequest;
import org.junit.platform.engine.EngineExecutionListener;
import org.junit.platform.engine.ExecutionRequest;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.discovery.ClassSelector;
import org.junit.platform.engine.discovery.ClasspathRootSelector;
import org.junit.platform.engine.discovery.MethodSelector;
import org.junit.platform.engine.discovery.PackageSelector;
import org.junit.platform.engine.support.descriptor.EngineDescriptor;

import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.function.Predicate;

/**
 * Test engine
 */
@SuppressWarnings("unchecked")
public class TestEngine implements org.junit.platform.engine.TestEngine {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestEngine.class);

    /**
     * Enum to indicate if we are running via the TestEngineRunner or via IDE
     */
    public enum Mode {CONSOLE, IDE}

    /**
     * Predicate to determine if a class is a test class (has @Test methods)
     */
    private static final Predicate<Class<?>> IS_TEST_CLASS = clazz -> TestEngineUtils.getTestMethods(clazz).size() > 0;

    /**
     * Predicate to determine if a method is a test method (declared class has @Test methods)
     */
    private static final Predicate<Method> IS_TEST_METHOD = method -> {
        Class<?> testClass = method.getDeclaringClass();
        return TestEngineUtils.getTestMethods(testClass).contains(method);
    };

    private Mode mode;

    /**
     * Constructor (called via IDE)
     */
    @SuppressWarnings("unused")
    public TestEngine() {
        this(Mode.IDE);
    }

    /**
     * Constructor (called via TestEngineRunner)
     *
     * @param mode
     */
    public TestEngine(Mode mode) {
        LOGGER.trace("TestEngine()");
        LOGGER.trace("mode = [%s]", mode);

        this.mode = mode;
    }

    /**
     * Method to get the test engine id
     *
     * @return
     */
    @Override
    public String getId() {
        return TestEngine.class.getName();
    }

    /**
     * Method to discover tests
     *
     * @param engineDiscoveryRequest
     * @param uniqueId
     * @return
     */
    @Override
    public TestDescriptor discover(EngineDiscoveryRequest engineDiscoveryRequest, UniqueId uniqueId) {
        LOGGER.trace("discover()");

        EngineDescriptor engineDescriptor = new EngineDescriptor(uniqueId, getId());

        // Test class to test method list mapping, sorted by test class name
        Map<Class<?>, List<Method>> testClassToMethodMap = new TreeMap<>(Comparator.comparing(Class::getName));

        // For each test class that was selected, add all test methods
        List<? extends DiscoverySelector> discoverySelectorList = engineDiscoveryRequest.getSelectorsByType(ClasspathRootSelector.class);
        LOGGER.trace("ClasspathRootSelector size [%d]", discoverySelectorList.size());
        for (DiscoverySelector discoverySelector : discoverySelectorList) {
            URI uri = ((ClasspathRootSelector) discoverySelector).getClasspathRoot();
            List<Class<?>> classList = ReflectionSupport.findAllClassesInClasspathRoot(uri, IS_TEST_CLASS, name -> true);
            for (Class<?> clazz : classList) {
                //LOGGER.trace(String.format("test class [%s]", clazz.getName()));
                testClassToMethodMap.putIfAbsent(clazz, TestEngineUtils.getTestMethods(clazz));
            }
        }

        // For each test class that was selected, add all test methods
        discoverySelectorList = engineDiscoveryRequest.getSelectorsByType(PackageSelector.class);
        LOGGER.debug("PackageSelector size [%d]", discoverySelectorList.size());
        for (DiscoverySelector discoverySelector : discoverySelectorList) {
            String packageName = ((PackageSelector) discoverySelector).getPackageName();
            List<Class<?>> classList = ReflectionSupport.findAllClassesInPackage(packageName, IS_TEST_CLASS, name -> true);
            for (Class<?> clazz : classList) {
                //LOGGER.trace(String.format("test class [%s]", clazz.getName()));
                testClassToMethodMap.putIfAbsent(clazz, TestEngineUtils.getTestMethods(clazz));
            }
        }

        // For each test class selected, add all test methods
        discoverySelectorList = engineDiscoveryRequest.getSelectorsByType(ClassSelector.class);
        LOGGER.debug("ClassSelector size [%d]", discoverySelectorList.size());
        for (DiscoverySelector discoverySelector : discoverySelectorList) {
            Class<?> clazz = ((ClassSelector) discoverySelector).getJavaClass();
            if (IS_TEST_CLASS.test(clazz)) {
                //LOGGER.trace(String.format("test class [%s]", clazz.getName()));
                testClassToMethodMap.putIfAbsent(clazz, TestEngineUtils.getTestMethods(clazz));
            }
        }

        // For each test method that was selected, add the test class and method
        discoverySelectorList = engineDiscoveryRequest.getSelectorsByType(MethodSelector.class);
        LOGGER.debug("MethodSelector size [%d]", discoverySelectorList.size());
        for (DiscoverySelector discoverySelector : discoverySelectorList) {
            Method method = ((MethodSelector) discoverySelector).getJavaMethod();
            Class<?> clazz = method.getDeclaringClass();
            if (IS_TEST_METHOD.test(method)) {
                //LOGGER.trace(String.format("test class [%s] @Test method [%s]", clazz.getName(), method.getName()));
                List<Method> methods = testClassToMethodMap.get(clazz);
                if (methods == null) {
                    methods = new ArrayList<>();
                    testClassToMethodMap.put(clazz, methods);
                }
                methods.add(method);
            }
        }

        // Sort the test methods by name (Test classes are already sorted by name)
        for (Map.Entry<Class<?>, List<Method>> mapEntry : testClassToMethodMap.entrySet()) {
            Collections.sort(mapEntry.getValue(), Comparator.comparing(Method::getName));
        }

        //DEBUG code to print test class / test method selection / ordering
        for (Class<?> testClass : testClassToMethodMap.keySet()) {
            List<Method> testMethodList = testClassToMethodMap.get(testClass);
            for (Method method : testMethodList) {
                LOGGER.trace(String.format("test class [%s] @Test method [%s]", testClass.getName(), method.getName()));
            }
        }

        try {
            for (Class<?> testClass : testClassToMethodMap.keySet()) {
                LOGGER.trace("processing test class [%s]", testClass.getName());

                Collection<Object> testParameters = null;

                // Try to get test parameters using a @ParameterSupplier fields and methods
                List<Field> parameterSupplierFields = TestEngineUtils.getParameterSupplierFields(testClass);
                LOGGER.trace("test class [%s] parameter supplier field count [%d]", testClass.getName(), parameterSupplierFields.size());

                for (Field field : parameterSupplierFields) {
                    LOGGER.trace("test class [%s] parameter supplier field name [%s]", testClass.getName(), field.getName());
                }

                List<Method> parameterSupplierMethods = TestEngineUtils.getParameterSupplierMethods(testClass);
                LOGGER.trace("test class [%s] parameter supplier method count [%d]", testClass.getName(), parameterSupplierMethods.size());

                for (Method method : parameterSupplierMethods) {
                    LOGGER.trace("test class [%s] parameter supplier method name [%s]", testClass.getName(), method.getName());
                }

                if ((parameterSupplierFields.size() > 0) && (parameterSupplierMethods.size() > 0)) {
                    // @ParameterSupplier field(s) and method(s) both found
                    throw new TestClassConfigurationException(
                            String.format(
                                    "Test class [%s] contains both a @ParameterSupplier field and method",
                                    testClass.getName()));
                }

                if ((parameterSupplierFields.size() == 0) && (parameterSupplierMethods.size() == 0)) {
                    // No @ParameterSupplier field or method found
                    throw new TestClassConfigurationException(
                            String.format(
                                    "Test class [%s] requires either @ParameterSupplier field or method",
                                    testClass.getName()));
                }

                if (parameterSupplierFields.size() > 1) {
                    // More than one @ParameterSupplier field found
                    throw new TestClassConfigurationException(
                            String.format(
                                    "Test class [%s] contains more than one @ParameterSupplier field",
                                    testClass.getName()));
                } else if (parameterSupplierFields.size() == 1) {
                    try {
                        testParameters = (Collection<Object>) parameterSupplierFields.get(0).get(null);
                    } catch (ClassCastException e) {
                        throw new TestClassConfigurationException(
                                String.format(
                                        "Test class [%s] @ParameterSupplier field must return a Collection",
                                        testClass.getName()));
                    }
                } else if (parameterSupplierMethods.size() > 1) {
                    // More than one @ParameterSupplier method found
                    throw new TestClassConfigurationException(
                            String.format(
                                    "Test class [%s] contains more than one @ParameterSupplier method",
                                    testClass.getName()));
                } else {
                    try {
                        testParameters = (Collection<Object>) parameterSupplierMethods.get(0).invoke(null, (Object[]) null);
                    } catch (ClassCastException e) {
                        throw new TestClassConfigurationException(
                                String.format(
                                        "Test class [%s] @ParameterSupplier method must return a Collection",
                                        testClass.getName()));
                    }
                }

                // Validate that we have a @Parameter field
                List<Field> parameterFields = TestEngineUtils.getParameterFields(testClass);
                Field parameterField;

                if (parameterFields.size() > 1) {
                    // More than one @Parameter field found
                    throw new TestClassConfigurationException(
                            String.format(
                                    "Test class [%s] contains more than one @Parameter field",
                                    testClass.getName()));
                } else if (parameterFields.size() == 1) {
                    parameterField = parameterFields.get(0);
                } else {
                    // No @Parameter field found
                    throw new TestClassConfigurationException(
                            String.format(
                                    "Test class [%s] public (non-static) @Parameter field required",
                                    testClass.getName()));
                }

                LOGGER.trace("test class @Parameter field [%s]", parameterField.getName());
                LOGGER.trace("test class parameter count [%d]", testParameters.size());

                if (testParameters.size() > 0) {
                    // Build the test descriptor tree if we have test parameters
                    // i.e. Tests with an empty set of parameters will be ignored
                    String testClassDisplayName = TestEngineUtils.getDisplayName(testClass);

                    TestClassTestDescriptor testClassTestDescriptor =
                            new TestClassTestDescriptor(
                                    uniqueId.append("/", testClassDisplayName),
                                    testClassDisplayName,
                                    testClass);

                    List<Object> testParameterList = new ArrayList<>(testParameters);
                    for (int i = 0; i < testParameterList.size(); i++) {
                        // Build the test descriptor for each test class / test parameter
                        Object testParameter = testParameterList.get(i);
                        String testParameterDisplayName = TestEngineUtils.getTestParameterDisplayName(testParameter, i);
                        String testParameterUniqueName = testParameterDisplayName + "/" + UUID.randomUUID();

                        TestParameterTestDescriptor testParameterTestDescriptor =
                                new TestParameterTestDescriptor(
                                        uniqueId.append("/", testClassDisplayName + "/" + testParameterUniqueName),
                                        testParameterDisplayName,
                                        testClass,
                                        testParameter);

                        for (Method testMethod : testClassToMethodMap.get(testClass)) {
                            // Build the test descriptor for each test class / test parameter / test method
                            String testMethodDisplayName = TestEngineUtils.getDisplayName(testMethod);
                            String testMethodUniqueName = testParameterDisplayName + "/" + UUID.randomUUID();

                            TestMethodTestDescriptor testMethodTestDescriptor =
                                    new TestMethodTestDescriptor(
                                            uniqueId.append("/", testClassDisplayName + "/" + testParameterUniqueName + "/" + testMethodUniqueName),
                                            testMethodDisplayName,
                                            testClass,
                                            testParameter,
                                            testMethod);

                            testParameterTestDescriptor.addChild(testMethodTestDescriptor);
                        }

                        testClassTestDescriptor.addChild(testParameterTestDescriptor);
                    }

                    engineDescriptor.addChild(testClassTestDescriptor);
                }
            }

            return engineDescriptor;
        } catch (Throwable t) {
            if (t instanceof TestClassConfigurationException) {
                LOGGER.error(t.getMessage());
                System.exit(1);
                return null;
            } else {
                throw new TestEngineException("Exception in test engine", t);
            }
        }
    }

    /**
     * Method to execute the execution request
     *
     * @param executionRequest
     */
    @Override
    public void execute(ExecutionRequest executionRequest) {
        LOGGER.debug("execute()");

        EngineExecutionListener engineExecutionListener = executionRequest.getEngineExecutionListener();

        if (mode == Mode.CONSOLE) {
            engineExecutionListener = new PrintStreamEngineExecutionListener(engineExecutionListener, System.out);
        }

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
        }

        try {
            Method testMethod = testMethodTestDescriptor.getTestMethod();
            testMethod.invoke(testInstance, (Object[]) null);
        } catch (Throwable t) {
            t = resolve(t);
            printStackTrace(t, System.err);
            throwableCollector.add(t);
        }

        try {
            for (Method afterEachMethod : TestEngineUtils.getAfterEachMethods(testClass)) {
                afterEachMethod.invoke(testInstance, (Object[]) null);
            }
        } catch (Throwable t) {
            t = resolve(t);
            printStackTrace(t, System.err);
            throwableCollector.add(t);
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
                                        .append(testParameterTestDescriptor.getTestParameter().toString())),
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
}
