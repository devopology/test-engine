package org.devopology.test.engine;

import org.devopology.test.engine.api.Named;
import org.devopology.test.engine.internal.TestClassConfigurationException;
import org.devopology.test.engine.internal.TestEngineUtils;
import org.devopology.test.engine.internal.descriptor.TestClassTestDescriptor;
import org.devopology.test.engine.internal.descriptor.TestMethodTestDescriptor;
import org.devopology.test.engine.internal.descriptor.TestParameterTestDescriptor;
import org.devopology.test.engine.internal.logger.Logger;
import org.devopology.test.engine.internal.logger.LoggerFactory;
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

public class TestEngineDiscoverySelectorResolver {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestEngineDiscoverySelectorResolver.class);

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

    public void resolveSelectors(EngineDiscoveryRequest engineDiscoveryRequest, EngineDescriptor engineDescriptor) {
        LOGGER.trace("resolveSelectors()");

        UniqueId uniqueId = engineDescriptor.getUniqueId();

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
                if (TestEngineUtils.isDisabled(testClass)) {
                    LOGGER.trace("test class [%s] is disabled", testClass.getName());
                    continue;
                }

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
                    String testClassDisplayName = TestEngineUtils.getClassDisplayName(testClass);

                    TestClassTestDescriptor testClassTestDescriptor =
                            new TestClassTestDescriptor(
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

                        String testParameterUniqueName = testParameterDisplayName + "/" + UUID.randomUUID();

                        TestParameterTestDescriptor testParameterTestDescriptor =
                                new TestParameterTestDescriptor(
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

                            TestMethodTestDescriptor testMethodTestDescriptor =
                                    new TestMethodTestDescriptor(
                                            uniqueId.append("/", testClassDisplayName + "/" + testParameterUniqueName + "/" + testMethodUniqueName),
                                            testMethodDisplayName,
                                            testClass,
                                            testParameter,
                                            testMethod);

                            testParameterTestDescriptor.addChild(testMethodTestDescriptor);
                        }

                        if (testParameterTestDescriptor.getChildren().size() > 0) {
                            testClassTestDescriptor.addChild(testParameterTestDescriptor);
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
}
