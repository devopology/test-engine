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
import org.devopology.test.engine.support.logger.Logger;
import org.devopology.test.engine.support.logger.LoggerFactory;
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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Class to implement methods to get test class fields / methods, caching the information
 */
@SuppressWarnings("PMD.GodClass")
public final class TestEngineUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestEngineUtils.class);

    private enum Scope { STATIC, NON_STATIC }

    private static final Map<Class<?>, Collection<Field>> parameterInjectFieldCache;
    private static final Map<Class<?>, Collection<Field>> parameterSupplierFieldsCache;
    private static final Map<Class<?>, Collection<Method>> parameterSupplierMethodsCache;
    private static final Map<Class<?>, Collection<Method>> beforeAllMethodCache;
    private static final Map<Class<?>, Collection<Method>> beforeClassMethodCache;
    private static final Map<Class<?>, Collection<Method>> beforeEachMethodCache;
    private static final Map<Class<?>, Collection<Method>> testMethodCache;
    private static final Map<Class<?>, Collection<Method>> afterEachMethodCache;
    private static final Map<Class<?>, Collection<Method>> afterAllMethodCache;
    private static final Map<Class<?>, Collection<Method>> afterClassMethodCache;
    private static final Map<Class<?>, String> classDisplayNameCache;
    private static final Map<Method, String> methodDisplayNameCache;

    static {
        parameterInjectFieldCache = new HashMap<>();
        parameterSupplierFieldsCache = new HashMap<>();
        parameterSupplierMethodsCache = new HashMap<>();
        beforeClassMethodCache = new HashMap<>();
        beforeAllMethodCache = new HashMap<>();
        beforeEachMethodCache = new HashMap<>();
        testMethodCache = new HashMap<>();
        afterEachMethodCache = new HashMap<>();
        afterAllMethodCache = new HashMap<>();
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
     * Method to get a Collection of Fields for a Class and super Classes
     *
     * @param clazz
     * @return
     */
    private static Collection<Field> getAllFields(
            Class<?> clazz,
            Class<? extends Annotation> annotation,
            Scope scope) {
        LOGGER.trace(
                "getAllFields(%s, %s, %s)",
                clazz.getName(),
                annotation.getName(),
                scope);

        Map<String, Field> fieldMap = new HashMap<>();
        resolveFields(clazz, annotation, scope, fieldMap);
        List<Field> fieldList = new LinkedList<>(fieldMap.values());
        fieldList.sort(Comparator.comparing(Field::getName));

        return fieldList;
    }

    /**
     * Method to recursively resolve Fields
     *
     * @param clazz
     * @param annotation
     * @param scope
     * @param fieldMap
     */
    private static void resolveFields(
            Class<?> clazz,
            Class<? extends Annotation> annotation,
            Scope scope,
            Map<String, Field> fieldMap) {
        LOGGER.trace(
                "resolveFields(%s, %s, %s)",
                clazz.getName(),
                annotation.getName(),
                scope);

        Stream.of(clazz.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(annotation))
                .filter(field -> {
                    int modifiers = field.getModifiers();
                    return Modifier.isPublic(modifiers) || Modifier.isProtected(modifiers);
                })
                .filter(field -> {
                    int modifiers = field.getModifiers();
                    if (scope == Scope.STATIC) {
                        return Modifier.isStatic(modifiers);
                    } else {
                        return !Modifier.isStatic(modifiers);
                    }
                }).forEach(field -> {
                    if (fieldMap.putIfAbsent(field.getName(), field) == null) {
                        LOGGER.trace("    add field [%s]", field.getName());
                        field.setAccessible(true);
                    }
                });

        Class<?> declaringClass = clazz.getSuperclass();
        if ((declaringClass != null) && !declaringClass.equals(Object.class)) {
            resolveFields(declaringClass, annotation, scope, fieldMap);
        }
    }

    /**
     * Method to get a Collection of all methods from a Class and super Classes
     *
     * @param clazz
     * @return
     */
    private static Collection<Method> getAllMethods(
            Class<?> clazz,
            Class<? extends Annotation> annotation,
            Scope scope,
            Class<?> returnType,
            int parameterCount) {
        LOGGER.trace(
                "getAllMethods(%s, %s, %s, %s, %d)",
                clazz.getName(),
                annotation.getName(),
                scope,
                returnType.getName(),
                parameterCount);

        Map<String, Method> methodMap = new HashMap<>();
        resolveMethods(clazz, annotation, scope, returnType, parameterCount, methodMap);
        List<Method> methodList = new LinkedList<>(methodMap.values());
        methodList.sort(Comparator.comparing(Method::getName));

        return methodList;
    }

    /**
     * Method to recursively resolve Methods
     *
     * @param clazz
     * @param annotation
     * @param scope
     * @param returnType
     * @param parameterCount
     * @param methodMap
     */
    private static void resolveMethods(
            Class<?> clazz,
            Class<? extends Annotation> annotation,
            Scope scope,
            Class<?> returnType,
            int parameterCount,
            Map<String, Method> methodMap) {
        LOGGER.trace(
                "resolveMethods(%s, %s, %s, %s, %d)",
                clazz.getName(),
                annotation.getName(),
                scope,
                returnType.getName(),
                parameterCount);

        Stream.of(clazz.getDeclaredMethods())
                .filter(method -> method.isAnnotationPresent(annotation))
                .filter(method -> {
                    int modifiers = method.getModifiers();
                    return Modifier.isPublic(modifiers) || Modifier.isProtected(modifiers);
                })
                .filter(method -> {
                    int modifiers = method.getModifiers();
                    if (scope == Scope.STATIC) {
                        return Modifier.isStatic(modifiers);
                    }
                    else {
                        return !Modifier.isStatic(modifiers);
                    }
                })
                .filter(method -> method.getParameterCount() == parameterCount)
                .filter(method -> {
                    // TODO understand why void is special
                    if (returnType == Void.class) {
                        return method.getReturnType().getName().equals("void");
                    } else {
                        return method.getReturnType().equals(returnType);
                    }
                })
                .forEach(method -> {
                    if (methodMap.putIfAbsent(method.getName(), method) == null) {
                        method.setAccessible(true);
                    }
                });

        Class<?> declaringClass = clazz.getSuperclass();
        if ((declaringClass != null) && !declaringClass.equals(Object.class)) {
            resolveMethods(declaringClass, annotation, scope, returnType, parameterCount, methodMap);
        }
    }

    /**
     * Method to get a Collection of @TestEngine.BeforeClass Methods sorted alphabetically
     *
     * @param clazz
     * @return
     */
    public static Collection<Method> getBeforeClassMethods(Class<?> clazz) {
        synchronized (beforeClassMethodCache) {
            LOGGER.trace("getBeforeClassMethods(%s)", clazz.getName());

            if (beforeClassMethodCache.containsKey(clazz)) {
                return beforeClassMethodCache.get(clazz);
            }

            Collection<Method> methods =
                    getAllMethods(
                            clazz,
                            TestEngine.BeforeClass.class,
                            Scope.STATIC,
                            Void.class,
                            0);

            beforeClassMethodCache.put(clazz, Collections.unmodifiableCollection(methods));

            return methods;
        }
    }

    /**
     * Method to get a Collection of @TestEngine.ParameterInject Fields sorted alphabetically
     *
     * @param clazz
     * @return
     */
    public static Collection<Field> getParameterInjectFields(Class<?> clazz) {
        synchronized (parameterInjectFieldCache) {
            LOGGER.trace("getParameterInjectFields(%s)", clazz.getName());

            if (parameterInjectFieldCache.containsKey(clazz)) {
                return parameterInjectFieldCache.get(clazz);
            }

            Collection<Field> fields =
                    getAllFields(
                            clazz,
                            TestEngine.ParameterInject.class,
                            Scope.NON_STATIC);

            parameterInjectFieldCache.put(clazz, Collections.unmodifiableCollection(fields));

            return fields;
        }
    }

    /**
     * Method to get a Collection of @TestEngine.ParameterSupplier Fields sorted alphabetically
     *
     * @param clazz
     * @return
     */
    public static Collection<Field> getParameterSupplierFields(Class<?> clazz) {
        Collection<Field> parameterSupplierFields = parameterSupplierFieldsCache.get(clazz);
        if (parameterSupplierFields != null) {
            return parameterSupplierFields;
        }

        parameterSupplierFields = new ArrayList<>();

        Field[] fields = clazz.getFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(TestEngine.ParameterSupplier.class)) {
                int modifiers = field.getModifiers();
                if (Modifier.isStatic(modifiers)) {
                    field.setAccessible(true);
                    parameterSupplierFields.add(field);
                }
            }
        }

        parameterSupplierFieldsCache.put(clazz, Collections.unmodifiableCollection(parameterSupplierFields));

        return parameterSupplierFields;
    }

    /**
     * Method to get a Collection of @TestEngine.ParameterSupplier Methods sorted alphabetically
     *
     * @param clazz
     * @return
     */
    public static Collection<Method> getParameterSupplierMethods(Class<?> clazz) {
        synchronized (parameterSupplierMethodsCache) {
            LOGGER.trace("getParameterSupplierMethods(%s)", clazz.getName());

            if (parameterSupplierMethodsCache.containsKey(clazz)) {
                return parameterSupplierMethodsCache.get(clazz);
            }

            Collection<Method> methods =
                    getAllMethods(
                            clazz,
                            TestEngine.ParameterSupplier.class,
                            Scope.STATIC,
                            Collection.class,
                            0);

            parameterSupplierMethodsCache.put(clazz, Collections.unmodifiableCollection(methods));

            return methods;
        }
    }

    /**
     * Method to get a Collection of @TestEngine.BeforeAll Methods sorted alphabetically
     *
     * @param clazz
     * @return
     */
    public static Collection<Method> getBeforeAllMethods(Class<?> clazz) {
        synchronized (beforeAllMethodCache) {
            if (beforeAllMethodCache.containsKey(clazz)) {
                return beforeAllMethodCache.get(clazz);
            }

            Collection<Method> methods =
                    getAllMethods(
                            clazz,
                            TestEngine.BeforeAll.class,
                            Scope.NON_STATIC,
                            Void.class,
                            0);

            beforeAllMethodCache.put(clazz, Collections.unmodifiableCollection(methods));

            return methods;
        }
    }

    /**
     * Method to get a Collection of @TestEngine.BeforeEach Methods sorted alphabetically
     *
     * @param clazz
     * @return
     */
    public static Collection<Method> getBeforeEachMethods(Class<?> clazz) {
        synchronized (beforeEachMethodCache) {
            if (beforeEachMethodCache.containsKey(clazz)) {
                return beforeEachMethodCache.get(clazz);
            }

            Collection<Method> methods =
                    getAllMethods(
                            clazz,
                            TestEngine.BeforeEach.class,
                            Scope.NON_STATIC,
                            Void.class,
                            0);

            beforeEachMethodCache.put(clazz, Collections.unmodifiableCollection(methods));

            return methods;
        }
    }

    /**
     * Method to get a Collection of @TestEngine.Test Methods sorted alphabetically
     *
     * @param clazz
     * @return
     */
    public static Collection<Method> getTestMethods(Class<?> clazz) {
        synchronized (testMethodCache) {
            LOGGER.trace("getTestMethods(%s)", clazz.getName());

            if (testMethodCache.containsKey(clazz)) {
                return testMethodCache.get(clazz);
            }

            Collection<Method> methods =
                    getAllMethods(
                            clazz,
                            TestEngine.Test.class,
                            Scope.NON_STATIC,
                            Void.class,
                            0);

            testMethodCache.put(clazz, Collections.unmodifiableCollection(methods));

            return methods;
        }
    }

    /**
     * Method to get a Collection of @TestEngine.AfterEach Methods sorted alphabetically
     *
     * @param clazz
     * @return
     */
    public static Collection<Method> getAfterEachMethods(Class<?> clazz) {
        synchronized (afterEachMethodCache) {
            if (afterEachMethodCache.containsKey(clazz)) {
                return afterEachMethodCache.get(clazz);
            }

            Collection<Method> methods =
                    getAllMethods(
                            clazz,
                            TestEngine.AfterEach.class,
                            Scope.NON_STATIC,
                            Void.class,
                            0);

            afterEachMethodCache.put(clazz, Collections.unmodifiableCollection(methods));

            return methods;
        }
    }

    /**
     * Method to get a Collection of @TestEngine.AfterAll Methods sorted alphabetically
     *
     * @param clazz
     * @return
     */
    public static Collection<Method> getAfterAllMethods(Class<?> clazz) {
        synchronized (afterAllMethodCache) {
            if (afterAllMethodCache.containsKey(clazz)) {
                return afterAllMethodCache.get(clazz);
            }

            Collection<Method> methods =
                    getAllMethods(
                            clazz,
                            TestEngine.AfterAll.class,
                            Scope.NON_STATIC,
                            Void.class,
                            0);

            afterAllMethodCache.put(clazz, Collections.unmodifiableCollection(methods));

            return methods;
        }
    }

    /**
     * Method to get a Collection of @TestEngine.AfterClass Methods sorted alphabetically
     *
     * @param clazz
     * @return
     */
    public static Collection<Method> getAfterClassMethods(Class<?> clazz) {
        synchronized (afterClassMethodCache) {
            if (afterClassMethodCache.containsKey(clazz)) {
                return afterClassMethodCache.get(clazz);
            }

            Collection<Method> methods =
                    getAllMethods(
                            clazz,
                            TestEngine.AfterClass.class,
                            Scope.STATIC,
                            Void.class,
                            0);

            afterClassMethodCache.put(clazz, Collections.unmodifiableCollection(methods));

            return methods;
        }
    }

    /**
     * Method to get whether a test class is a base class
     *
     * @param clazz
     * @return
     */
    public static boolean isBaseClass(Class<?> clazz) {
        return clazz.isAnnotationPresent(TestEngine.BaseClass.class);
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
     * Method to get whether a test Method is disabled
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

        String displayName = null;

        if (object instanceof Metadata) {
            displayName = ((Metadata) object).getDisplayName();
        }

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

    /**
     * Method to create a TestPlan from a TestDescriptor
     *
     * @param testDescriptor
     * @param configurationParameters
     * @return
     */
    public static TestPlan createTestPlan(TestDescriptor testDescriptor, ConfigurationParameters configurationParameters) {
        return TestPlan.from(Collections.singleton(testDescriptor), configurationParameters);
    }
}
