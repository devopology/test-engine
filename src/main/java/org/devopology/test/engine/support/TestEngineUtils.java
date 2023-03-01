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

import org.devopology.test.engine.api.Metadata;
import org.devopology.test.engine.api.TestEngine;
import org.devopology.test.engine.support.util.Switch;
import org.junit.platform.engine.ConfigurationParameters;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.launcher.TestPlan;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

/**
 * Class to implement methods to get test class fields / methods, caching the information
 */
@SuppressWarnings("PMD.GodClass")
public final class TestEngineUtils {

    private static Map<Class<?>, List<Field>> parameterInjectFieldCache;
    private static Map<Class<?>, List<Field>> parameterSupplierFieldsCache;
    private static Map<Class<?>, List<Method>> parameterSupplierMethodsCache;
    private static Map<Class<?>, List<Method>> beforeAllMethodListCache;
    private static Map<Class<?>, List<Method>> beforeClassMethodCache;
    private static Map<Class<?>, List<Method>> beforeEachMethodListCache;
    private static Map<Class<?>, List<Method>> testMethodListCache;
    private static Map<Class<?>, List<Method>> afterEachMethodListCache;
    private static Map<Class<?>, List<Method>> afterAllMethodListCache;
    private static Map<Class<?>, List<Method>> afterClassMethodCache;
    private static Map<Class<?>, String> classDisplayNameCache;
    private static Map<Method, String> methodDisplayNameCache;

    static {
        parameterInjectFieldCache = new HashMap<>();
        parameterSupplierFieldsCache = new HashMap<>();
        parameterSupplierMethodsCache = new HashMap<>();
        beforeClassMethodCache = new HashMap<>();
        beforeAllMethodListCache = new HashMap<>();
        beforeEachMethodListCache = new HashMap<>();
        testMethodListCache = new HashMap<>();
        afterEachMethodListCache = new HashMap<>();
        afterAllMethodListCache = new HashMap<>();
        afterClassMethodCache = new HashMap<>();
        classDisplayNameCache = new HashMap<>();
        methodDisplayNameCache = new HashMap<>();
    }

    /**
     * Constructor
     */
    private TestEngineUtils() {
        // DO NOTHING
    }

    /**
     * Method to get a List of @PreTest methods sorted alphabetically
     *
     * @param clazz
     * @return
     */
    public static List<Method> getBeforeClassMethods(Class<?> clazz) {
        List<Method> methodList = beforeClassMethodCache.get(clazz);
        if (methodList != null) {
            return methodList;
        }

        methodList = getStaticDeclaredMethods(clazz, TestEngine.BeforeClass.class);
        beforeClassMethodCache.put(clazz, methodList);
        return methodList;
    }

    /**
     * Method to get a List of @Parameter.Inject fields sorted alphabetically
     *
     * @param clazz
     * @return
     */
    public static List<Field> getParameterInjectFields(Class<?> clazz) {
        List<Field> parameterInjectFields = parameterInjectFieldCache.get(clazz);
        if (parameterInjectFields != null) {
            return parameterInjectFields;
        }

        parameterInjectFields = new ArrayList<>();
        
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(TestEngine.ParameterInject.class)) {
                int modifiers = field.getModifiers();
                if (!Modifier.isStatic(modifiers)) {
                    field.setAccessible(true);
                    parameterInjectFields.add(field);
                }
            }
        }

        parameterInjectFields.sort(Comparator.comparing(Field::getName));
        parameterInjectFieldCache.put(clazz, parameterInjectFields);

        return parameterInjectFields;
    }

    /**
     * Method to get a List of @Parameter.Supplier fields sorted alphabetically
     *
     * @param clazz
     * @return
     */
    public static List<Field> getParameterSupplierFields(Class<?> clazz) {
        List<Field> parameterSupplierFields = parameterSupplierFieldsCache.get(clazz);
        if (parameterSupplierFields != null) {
            return parameterSupplierFields;
        }

        parameterSupplierFields = new ArrayList<>();

        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(TestEngine.ParameterSupplier.class)) {
                int modifiers = field.getModifiers();
                if (Modifier.isStatic(modifiers)) {
                    field.setAccessible(true);
                    parameterSupplierFields.add(field);
                }
            }
        }

        parameterSupplierFields.sort(Comparator.comparing(Field::getName));
        parameterSupplierFieldsCache.put(clazz, parameterSupplierFields);

        return parameterSupplierFields;
    }

    /**
     * Method to get a List of @Parameter.Supplier fields sorted alphabetically
     *
     * @param clazz
     * @return
     */
    public static List<Method> getParameterSupplierMethods(Class<?> clazz) {
        List<Method> parameterSupplierMethods = parameterSupplierMethodsCache.get(clazz);
        if (parameterSupplierMethods != null) {
            return parameterSupplierMethods;
        }

        parameterSupplierMethods = new ArrayList<>();

        Method[] methods = clazz.getDeclaredMethods();
        for (Method method : methods) {
            int modifiers = method.getModifiers();
            if (method.isAnnotationPresent(TestEngine.ParameterSupplier.class)
                && Modifier.isStatic(modifiers)
                && (method.getParameterCount() == 0)) {
                    Class<?> returnType = method.getReturnType();
                    if (Collection.class.isAssignableFrom(returnType) || Stream.class.isAssignableFrom(returnType)) {
                        method.setAccessible(true);
                        parameterSupplierMethods.add(method);
                    }
            }
        }

        parameterSupplierMethods.sort(Comparator.comparing(Method::getName));
        parameterSupplierMethodsCache.put(clazz, parameterSupplierMethods);

        return parameterSupplierMethods;
    }

    /**
     * Method to get a List of @TestEngine.BeforeAll methods sorted alphabetically
     *
     * @param clazz
     * @return
     */
    public static List<Method> getBeforeAllMethods(Class<?> clazz) {
        List<Method> methodList = beforeAllMethodListCache.get(clazz);
        if (methodList != null) {
            return methodList;
        }

        methodList = getDeclaredMethods(clazz, TestEngine.BeforeAll.class);
        beforeAllMethodListCache.put(clazz, methodList);
        return methodList;
    }

    /**
     * Method to get a List of @TestEngine.BeforeEach methods sorted alphabetically
     *
     * @param clazz
     * @return
     */
    public static List<Method> getBeforeEachMethods(Class<?> clazz) {
        List<Method> methodList = beforeEachMethodListCache.get(clazz);
        if (methodList != null) {
            return methodList;
        }

        methodList = getDeclaredMethods(clazz, TestEngine.BeforeEach.class);
        beforeEachMethodListCache.put(clazz, methodList);
        return methodList;
    }

    /**
     * Method to get a List of @Test methods sorted alphabetically
     *
     * @param clazz
     * @return
     */
    public static List<Method> getTestMethods(Class<?> clazz) {
        List<Method> methodList = testMethodListCache.get(clazz);
        if (methodList != null) {
            return methodList;
        }

        methodList = getDeclaredMethods(clazz, TestEngine.Test.class);
        testMethodListCache.put(clazz, methodList);
        return methodList;
    }

    /**
     * Method to get a List of @TestEngine.AfterEach methods sorted alphabetically
     *
     * @param clazz
     * @return
     */
    public static List<Method> getAfterEachMethods(Class<?> clazz) {
        List<Method> methodList = afterEachMethodListCache.get(clazz);
        if (methodList != null) {
            return methodList;
        }

        methodList = getDeclaredMethods(clazz, TestEngine.AfterEach.class);
        afterEachMethodListCache.put(clazz, methodList);
        return methodList;
    }

    /**
     * Method to get a List of @TestEngine.AfterAll methods sorted alphabetically
     *
     * @param clazz
     * @return
     */
    public static List<Method> getAfterAllMethods(Class<?> clazz) {
        List<Method> methodList = afterAllMethodListCache.get(clazz);
        if (methodList != null) {
            return methodList;
        }

        methodList = getDeclaredMethods(clazz, TestEngine.AfterAll.class);
        afterAllMethodListCache.put(clazz, methodList);
        return methodList;
    }

    /**
     * Method to get a List of @TestEngine.AfterClass methods sorted alphabetically
     *
     * @param clazz
     * @return
     */
    public static List<Method> getAfterClassMethods(Class<?> clazz) {
        List<Method> methodList = afterClassMethodCache.get(clazz);
        if (methodList != null) {
            return methodList;
        }

        methodList = getStaticDeclaredMethods(clazz, TestEngine.AfterClass.class);
        afterClassMethodCache.put(clazz, methodList);
        return methodList;
    }

    /**
     * Method to get whether a test class is disabled
     *
     * @param clazz
     * @return
     */
    public static boolean isDisabled(Class<?> clazz) {
        return clazz.isAnnotationPresent(TestEngine.Disabled.class);
    }

    /**
     * Method to get whether a test method is disabled
     *
     * @param method
     * @return
     */
    public static boolean isDisabled(Method method) {
        return method.isAnnotationPresent(TestEngine.Disabled.class);
    }

    /**
     * Method to get the optional test class display name
     *
     * @param clazz
     * @return
     */
    public static String getClassDisplayName(Class<?> clazz) {
        String displayName = classDisplayNameCache.get(clazz);
        if (displayName != null) {
            return displayName;
        }

        if (clazz.isAnnotationPresent(TestEngine.DisplayName.class)) {
            try {
                Annotation annotation = clazz.getAnnotation(TestEngine.DisplayName.class);
                Class<? extends Annotation> type = annotation.annotationType();
                Method valueMethod = type.getDeclaredMethod("value", (Class<?>[]) null);
                displayName = valueMethod.invoke(annotation, (Object[]) null).toString();
            } catch (Throwable t) {
                // DO NOTHING
            }
        }

        if (displayName == null) {
            displayName = clazz.getName();
        }

        classDisplayNameCache.put(clazz, displayName);
        return displayName;
    }

    /**
     * Method to get the optional test method display name
     *
     * @param method
     * @return
     */
    public static String getMethodDisplayName(Method method) {
        String displayName = methodDisplayNameCache.get(method);
        if (displayName != null) {
            return displayName;
        }

        if (method.isAnnotationPresent(TestEngine.DisplayName.class)) {
            try {
                Annotation annotation = method.getAnnotation(TestEngine.DisplayName.class);
                Class<? extends Annotation> type = annotation.annotationType();
                Method valueMethod = type.getDeclaredMethod("value", (Class<?>[]) null);
                displayName = valueMethod.invoke(annotation, (Object[]) null).toString();
            } catch (Throwable t) {
                // DO NOTHING
            }
        }

        if (displayName == null) {
            displayName = method.getName();
        }

        methodDisplayNameCache.put(method, displayName);
        return displayName;
    }

    /**
     * Method to get a display name
     *
     * @param object
     * @return
     */
    public static String getDisplayName(Object object) {
        if (object == null) {
            return "null";
        }

        String displayName;

        AtomicReference<String> result = new AtomicReference<>();

        Switch.switchType(
                object,
                Switch.switchCase(Metadata.class, metadata -> result.set(metadata.getDisplayName())));

        displayName = result.get();

        if (displayName == null) {
            displayName = object.toString();
        } else {
            displayName = displayName.trim();
            if (displayName.isEmpty()) {
                displayName = object.toString();
            }
        }

        return displayName;
    }

    public static TestPlan createTestPlan(TestDescriptor testDescriptor, ConfigurationParameters configurationParameters) {
        return TestPlan.from(Collections.singleton(testDescriptor), configurationParameters);
    }

    /**
     * Method to determine if a TestDescriptor has siblings
     *
     * @param testDescriptor
     * @return
     */
    public static boolean hasSiblings(TestDescriptor testDescriptor) {
        AtomicBoolean atomicBoolean = new AtomicBoolean();
        testDescriptor.getParent().ifPresent(
                testDescriptor1 -> atomicBoolean.set(testDescriptor1.getChildren().size() > 1));

        return atomicBoolean.get();
    }

    /**
     * Class to get a List of static methods with a specific annotation sorted alphabetically
     *
     * @param clazz
     * @param annotation
     * @return
     */
    private static List<Method> getStaticDeclaredMethods(Class<?> clazz, Class<? extends Annotation> annotation) {
        List<Method> methodList = new ArrayList<>();

        Method[] methods = clazz.getDeclaredMethods();
        for (Method method : methods) {
            int modifiers = method.getModifiers();
            if (method.isAnnotationPresent(annotation)
                    && Modifier.isStatic(modifiers)
                    && Modifier.isPublic(modifiers)
                    && (method.getParameterCount() == 0)) {
                Class<?> returnType = method.getReturnType();
                if (Void.TYPE.isAssignableFrom(returnType)) {
                    methodList.add(method);
                }
            }
        }

        methodList.sort(Comparator.comparing(Method::getName));

        return methodList;
    }

    /**
     * Class to get a List of methods with a specific annotation sorted alphabetically
     *
     * @param clazz
     * @param annotation
     * @return
     */
    private static List<Method> getDeclaredMethods(Class<?> clazz, Class<? extends Annotation> annotation) {
        List<Method> methodList = new ArrayList<>();

        Method[] methods = clazz.getDeclaredMethods();
        for (Method method : methods) {
            int modifiers = method.getModifiers();
            if (method.isAnnotationPresent(annotation)
                    && !Modifier.isStatic(modifiers)
                    && Modifier.isPublic(modifiers)
                    && (method.getParameterCount() == 0)) {
                        Class<?> returnType = method.getReturnType();
                        if (Void.TYPE.isAssignableFrom(returnType)) {
                            methodList.add(method);
                        }
            }
        }

        methodList.sort(Comparator.comparing(Method::getName));

        return methodList;
    }
}
