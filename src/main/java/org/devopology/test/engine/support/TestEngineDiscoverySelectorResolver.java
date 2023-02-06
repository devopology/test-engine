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

import org.assertj.core.util.Arrays;
import org.devopology.test.engine.api.Named;
import org.devopology.test.engine.support.descriptor.TestEngineClassTestDescriptor;
import org.devopology.test.engine.support.descriptor.TestEngineParameterTestDescriptor;
import org.devopology.test.engine.support.descriptor.TestEngineTestMethodTestDescriptor;
import org.devopology.test.engine.support.logger.Logger;
import org.devopology.test.engine.support.logger.LoggerFactory;
import org.junit.platform.commons.support.ReflectionSupport;
import org.junit.platform.engine.DiscoverySelector;
import org.junit.platform.engine.EngineDiscoveryRequest;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.discovery.ClassSelector;
import org.junit.platform.engine.discovery.ClasspathRootSelector;
import org.junit.platform.engine.discovery.MethodSelector;
import org.junit.platform.engine.discovery.PackageSelector;
import org.junit.platform.engine.support.descriptor.EngineDescriptor;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.function.Predicate;

/**
 * Class to implement a code to discover tests
 */
public class TestEngineDiscoverySelectorResolver {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestEngineDiscoverySelectorResolver.class);

    /**
     * Predicate to determine if a class is a test class (has @Test methods)
     */
    private static final Predicate<Class<?>> IS_TEST_CLASS = clazz -> !TestEngineUtils.getTestMethods(clazz).isEmpty();

    /**
     * Predicate to determine if a method is a test method (declared class has @Test methods)
     */
    private static final Predicate<Method> IS_TEST_METHOD =
            method -> TestEngineUtils.getTestMethods(method.getDeclaringClass()).contains(method);

    /**
     * Method to resolve tests, adding them to the EngineDescriptor
     *
     * @param engineDiscoveryRequest
     * @param engineDescriptor
     */
    public void resolveSelectors(EngineDiscoveryRequest engineDiscoveryRequest, EngineDescriptor engineDescriptor) {
        LOGGER.trace("resolveSelectors()");

        // Test class to test method list mapping, sorted by test class name
        Map<Class<?>, List<Method>> testClassToMethodMap = new TreeMap<>(Comparator.comparing(Class::getName));

        // For each test class that was selected, add all test methods
        resolveClasspathRoot(engineDiscoveryRequest, testClassToMethodMap);

        // For each test class that was selected, add all test methods
        resolvePackageSelector(engineDiscoveryRequest, testClassToMethodMap);

        // For each test class selected, add all test methods
        resolveClassSelector(engineDiscoveryRequest, testClassToMethodMap);

        // For each test method that was selected, add the test class and method
        resolveMethodSelector(engineDiscoveryRequest, testClassToMethodMap);

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

        processSelectors(engineDescriptor, testClassToMethodMap);
    }

    private void processSelectors(
            EngineDescriptor engineDescriptor,
            Map<Class<?>, List<Method>> testClassToMethodMap) {
        UniqueId uniqueId = engineDescriptor.getUniqueId();

        try {
            for (Class<?> testClass : testClassToMethodMap.keySet()) {
                if (TestEngineUtils.isDisabled(testClass)) {
                    LOGGER.trace("test class [%s] is disabled", testClass.getName());
                    continue;
                }

                LOGGER.trace("processing test class [%s]", testClass.getName());

                Collection<Object> testParameters = null;

                // Try to get test parameters using a @Parameter.Supplier or @ParameterSupplier fields and methods
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

                if (!parameterSupplierFields.isEmpty() && !parameterSupplierMethods.isEmpty()) {
                    // @ParameterSupplier field(s) and method(s) both found
                    throw new TestClassConfigurationException(
                            String.format(
                                    "Test class [%s] contains both a @ParameterSupplier field and method",
                                    testClass.getName()));
                }

                if (parameterSupplierFields.isEmpty() && parameterSupplierMethods.isEmpty()) {
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
                                        testClass.getName()),
                                e);
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
                                        testClass.getName()),
                                e);
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

                if (!testParameters.isEmpty()) {
                    // Build the test descriptor tree if we have test parameters
                    // i.e. Tests with an empty set of parameters will be ignored
                    String testClassDisplayName = TestEngineUtils.getClassDisplayName(testClass);

                    TestEngineClassTestDescriptor testClassTestDescriptor =
                            new TestEngineClassTestDescriptor(
                                    uniqueId.append("/", testClassDisplayName),
                                    testClassDisplayName,
                                    testClass);

                    List<Object> testParameterList = new ArrayList<>(testParameters);
                    for (int i = 0; i < testParameterList.size(); i++) {
                        // Build the test descriptor for each test class / test parameter
                        Object testParameter = testParameterList.get(i);
                        String testParameterDisplayName = TestEngineUtils.getDisplayName(testParameter);

                        if (testParameter instanceof Named) {
                            testParameter = ((Named) testParameter).getPayload();
                        }

                        if (Arrays.isArray(testParameter)) {
                            testParameterDisplayName = "Array [" + i + "]";
                        }

                        String testParameterUniqueName = testParameterDisplayName + "/" + UUID.randomUUID();

                        TestEngineParameterTestDescriptor testEngineParameterTestDescriptor =
                                new TestEngineParameterTestDescriptor(
                                        uniqueId.append("/", testClassDisplayName + "/" + testParameterUniqueName),
                                        testParameterDisplayName,
                                        testClass,
                                        testParameter);

                        for (Method testMethod : testClassToMethodMap.get(testClass)) {
                            if (TestEngineUtils.isDisabled(testMethod)) {
                                LOGGER.trace(
                                        "test class [%s] test method [%s] is disabled",
                                        testClass.getName(),
                                        testMethod.getName());
                                continue;
                            }

                            // Build the test descriptor for each test class / test parameter / test method
                            String testMethodDisplayName = TestEngineUtils.getMethodDisplayName(testMethod);
                            String testMethodUniqueName = testParameterDisplayName + "/" + UUID.randomUUID();

                            TestEngineTestMethodTestDescriptor testEngineTestMethodTestDescriptor =
                                    new TestEngineTestMethodTestDescriptor(
                                            uniqueId.append("/", testClassDisplayName + "/" + testParameterUniqueName + "/" + testMethodUniqueName),
                                            testMethodDisplayName,
                                            testClass,
                                            testParameter,
                                            testMethod);

                            testEngineParameterTestDescriptor.addChild(testEngineTestMethodTestDescriptor);
                        }

                        if (testEngineParameterTestDescriptor.getChildren().size() > 0) {
                            testClassTestDescriptor.addChild(testEngineParameterTestDescriptor);
                        }
                    }

                    if (testClassTestDescriptor.getChildren().size() > 0) {
                        engineDescriptor.addChild(testClassTestDescriptor);
                    }
                }
            }
        } catch (Throwable t) {
            throw new TestEngineException("Exception in test engine", t);
        }
    }

    private void resolveClasspathRoot(EngineDiscoveryRequest engineDiscoveryRequest, Map<Class<?>, List<Method>> testClassToMethodMap) {
        LOGGER.trace("resolveClasspathRoot()");

        List<? extends DiscoverySelector> discoverySelectorList = engineDiscoveryRequest.getSelectorsByType(ClasspathRootSelector.class);
        LOGGER.trace("discoverySelectorList size [%d]", discoverySelectorList.size());

        for (DiscoverySelector discoverySelector : discoverySelectorList) {
            URI uri = ((ClasspathRootSelector) discoverySelector).getClasspathRoot();
            LOGGER.trace("uri [%s]", uri);

            List<Class<?>> classList = ReflectionSupport.findAllClassesInClasspathRoot(uri, IS_TEST_CLASS, name -> true);

            for (Class<?> clazz : classList) {
                LOGGER.trace(String.format("  class [%s]", clazz.getName()));
                testClassToMethodMap.putIfAbsent(clazz, TestEngineUtils.getTestMethods(clazz));
            }
        }
    }

    private void resolvePackageSelector(EngineDiscoveryRequest engineDiscoveryRequest, Map<Class<?>, List<Method>> testClassToMethodMap) {
        LOGGER.trace("resolvePackageSelector()");

        List<? extends DiscoverySelector> discoverySelectorList = engineDiscoveryRequest.getSelectorsByType(PackageSelector.class);
        LOGGER.trace("discoverySelectorList size [%d]", discoverySelectorList.size());

        for (DiscoverySelector discoverySelector : discoverySelectorList) {
            String packageName = ((PackageSelector) discoverySelector).getPackageName();
            List<Class<?>> classList = ReflectionSupport.findAllClassesInPackage(packageName, IS_TEST_CLASS, name -> true);

            for (Class<?> clazz : classList) {
                LOGGER.trace(String.format("  test class [%s]", clazz.getName()));
                testClassToMethodMap.putIfAbsent(clazz, TestEngineUtils.getTestMethods(clazz));
            }
        }
    }

    private void resolveClassSelector(EngineDiscoveryRequest engineDiscoveryRequest, Map<Class<?>, List<Method>> testClassToMethodMap) {
        LOGGER.trace("resolveClassSelector()");

        List<? extends DiscoverySelector> discoverySelectorList = engineDiscoveryRequest.getSelectorsByType(ClassSelector.class);
        LOGGER.trace("discoverySelectorList size [%d]", discoverySelectorList.size());

        for (DiscoverySelector discoverySelector : discoverySelectorList) {
            Class<?> clazz = ((ClassSelector) discoverySelector).getJavaClass();

            if (IS_TEST_CLASS.test(clazz)) {
                LOGGER.trace(String.format("  test class [%s]", clazz.getName()));
                testClassToMethodMap.putIfAbsent(clazz, TestEngineUtils.getTestMethods(clazz));
            }
        }
    }

    private static void resolveMethodSelector(EngineDiscoveryRequest engineDiscoveryRequest, Map<Class<?>, List<Method>> testClassToMethodMap) {
        LOGGER.trace("resolveMethodSelector()");

        List<? extends DiscoverySelector> discoverySelectorList = engineDiscoveryRequest.getSelectorsByType(MethodSelector.class);
        LOGGER.trace("discoverySelectorList size [%d]", discoverySelectorList.size());

        for (DiscoverySelector discoverySelector : discoverySelectorList) {
            Method method = ((MethodSelector) discoverySelector).getJavaMethod();
            Class<?> clazz = method.getDeclaringClass();

            if (IS_TEST_METHOD.test(method)) {
                LOGGER.trace(String.format("  test class [%s] @Test method [%s]", clazz.getName(), method.getName()));
                List<Method> methods = testClassToMethodMap.get(clazz);

                if (methods == null) {
                    methods = new ArrayList<>();
                    testClassToMethodMap.put(clazz, methods);
                }

                methods.add(method);
            }
        }
    }
}
