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

package org.devopology.test.engine.internal.util;

import org.devopology.test.engine.api.AfterAll;
import org.devopology.test.engine.api.AfterEach;
import org.devopology.test.engine.api.BeforeAll;
import org.devopology.test.engine.api.BeforeEach;
import org.devopology.test.engine.api.DisplayName;
import org.devopology.test.engine.api.Parameter;
import org.devopology.test.engine.api.ParameterMap;
import org.devopology.test.engine.api.ParameterSupplier;
import org.devopology.test.engine.api.Test;
import org.junit.platform.engine.TestDescriptor;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Class to implement methods to get test class fields / methods, caching the information
 */
public class TestUtils {

    private static Map<Class<?>, Field> parameterFieldCache;
    private static Map<Class<?>, Field> parameterSupplierFieldCache;
    private static Map<Class<?>, Method> parameterSupplierMethodCache;
    private static Map<Class<?>, List<Method>> beforeAllMethodListCache;
    private static Map<Class<?>, List<Method>> beforeEachMethodListCache;
    private static Map<Class<?>, List<Method>> testMethodListCache;
    private static Map<Class<?>, List<Method>> afterEachMethodListCache;
    private static Map<Class<?>, List<Method>> afterAllMethodListCache;
    private static Map<Class<?>, String> classDisplayNameCache;
    private static Map<Method, String> methodDisplayNameCache;

    static {
        parameterFieldCache = new HashMap<>();
        parameterSupplierFieldCache = new HashMap<>();
        parameterSupplierMethodCache = new HashMap<>();
        beforeAllMethodListCache = new HashMap<>();
        beforeEachMethodListCache = new HashMap<>();
        testMethodListCache = new HashMap<>();
        afterEachMethodListCache = new HashMap<>();
        afterAllMethodListCache = new HashMap<>();
        classDisplayNameCache = new HashMap<>();
        methodDisplayNameCache = new HashMap<>();
    }

    /**
     * Constructor
     */
    private TestUtils() {
        // DO NOTHING
    }

    /**
     * Method to get the first test class @Parameter field
     *
     * @param clazz
     * @return
     */
    public static Field getParameterField(Class<?> clazz) {
        Field parameterField = parameterFieldCache.get(clazz);
        if (parameterField != null) {
            return parameterField;
        }

        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(Parameter.class)) {
                int modifiers = field.getModifiers();
                if (!Modifier.isStatic(modifiers) && Modifier.isPublic(modifiers)) {
                    parameterFieldCache.put(clazz, field);
                    return field;
                }
            }
        }

        return null;
    }

    /**
     * Method to get the first @ParameterSupplier field
     *
     * @param clazz
     * @return
     */
    public static Field getParameterSupplierField(Class<?> clazz) {
        Field parameterSupplierField = parameterSupplierFieldCache.get(clazz);
        if (parameterSupplierField != null) {
            return parameterSupplierField;
        }

        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(ParameterSupplier.class)) {
                int modifiers = field.getModifiers();
                if (Modifier.isStatic(modifiers) && Modifier.isPublic(modifiers)) {
                    Class<?> fieldType = field.getType();
                    if (Collection.class.isAssignableFrom(fieldType)) {
                        parameterSupplierFieldCache.put(clazz, field);
                        return field;
                    }
                }
            }
        }

        return null;
    }

    /**
     * Method to get the first @ParameterSupplier method
     *
     * @param clazz
     * @return
     */
    public static Method getParameterSupplierMethod(Class<?> clazz) {
        Method parameterSupplierMethod = parameterSupplierMethodCache.get(clazz);
        if (parameterSupplierMethod != null) {
            return parameterSupplierMethod;
        }

        Method[] methods = clazz.getDeclaredMethods();
        for (Method method : methods) {
            if (method.isAnnotationPresent(ParameterSupplier.class)) {
                int modifiers = method.getModifiers();
                if (Modifier.isStatic(modifiers) && Modifier.isPublic(modifiers)) {
                    if (method.getParameterCount() == 0) {
                        Class<?> returnType = method.getReturnType();
                        if (Collection.class.isAssignableFrom(returnType)) {
                            parameterSupplierMethodCache.put(clazz, method);
                            return method;
                        }
                    }
                }
            }
        }

        return null;
    }

    /**
     * Method to get a Collection of @BeforeAll methods sorted alphabetically
     *
     * @param clazz
     * @return
     */
    public static List<Method> getBeforeAllMethods(Class<?> clazz) {
        List<Method> methodList = beforeAllMethodListCache.get(clazz);
        if (methodList != null) {
            return methodList;
        }

        methodList = getDeclaredMethods(clazz, BeforeAll.class);
        beforeAllMethodListCache.put(clazz, methodList);
        return methodList;
    }

    /**
     * Method to get a Collection of @BeforeEach methods sorted alphabetically
     *
     * @param clazz
     * @return
     */
    public static List<Method> getBeforeEachMethods(Class<?> clazz) {
        List<Method> methodList = beforeEachMethodListCache.get(clazz);
        if (methodList != null) {
            return methodList;
        }

        methodList = getDeclaredMethods(clazz, BeforeEach.class);
        beforeEachMethodListCache.put(clazz, methodList);
        return methodList;
    }

    /**
     * Method to get a Collection of @Test methods sorted alphabetically
     *
     * @param clazz
     * @return
     */
    public static List<Method> getTestMethods(Class<?> clazz) {
        List<Method> methodList = testMethodListCache.get(clazz);
        if (methodList != null) {
            return methodList;
        }

        methodList = getDeclaredMethods(clazz, Test.class);
        testMethodListCache.put(clazz, methodList);
        return methodList;
    }

    /**
     * Method to get a Collection of @AfterEach methods sorted alphabetically
     *
     * @param clazz
     * @return
     */
    public static List<Method> getAfterEachMethods(Class<?> clazz) {
        List<Method> methodList = afterEachMethodListCache.get(clazz);
        if (methodList != null) {
            return methodList;
        }

        methodList = getDeclaredMethods(clazz, AfterEach.class);
        afterEachMethodListCache.put(clazz, methodList);
        return methodList;
    }

    /**
     * Method to get a Collection of @AfterAll methods sorted alphabetically
     *
     * @param clazz
     * @return
     */
    public static List<Method> getAfterAllMethods(Class<?> clazz) {
        List<Method> methodList = afterAllMethodListCache.get(clazz);
        if (methodList != null) {
            return methodList;
        }

        methodList = getDeclaredMethods(clazz, AfterAll.class);
        afterAllMethodListCache.put(clazz, methodList);
        return methodList;
    }

    public static String getDisplayName(Class<?> clazz) {
        String displayName = classDisplayNameCache.get(clazz);
        if (displayName != null) {
            return displayName;
        }

        if (clazz.isAnnotationPresent(DisplayName.class)) {
            try {
                Annotation annotation = clazz.getAnnotation(DisplayName.class);
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

    public static String getDisplayName(Method method) {
        String displayName = methodDisplayNameCache.get(method);
        if (displayName != null) {
            return displayName;
        }

        if (method.isAnnotationPresent(DisplayName.class)) {
            try {
                Annotation annotation = method.getAnnotation(DisplayName.class);
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
     * Method to get the optional object display name (public String getDisplayName() method)
     *
     * @param object
     * @return
     */
    public static String getTestParameterDisplayName(Object object, int index) {
        String displayName = null;

        if (object instanceof ParameterMap) {
            displayName = ((ParameterMap) object).getDisplayName();
        } else {
            Method[] methods = object.getClass().getDeclaredMethods();
            for (Method method : methods) {
                int modifiers = method.getModifiers();
                if (method.getName().equals("getDisplayName")
                        && !Modifier.isStatic(modifiers)
                        && (method.getParameterCount() == 0)) {
                    try {
                        method.setAccessible(true);
                        Object o = method.invoke(object, (Object[]) null);
                        if (o != null) {
                            if (o instanceof String) {
                                displayName = (String) o;
                            } else {
                                displayName = o.toString();
                            }
                            break;
                        }
                    } catch (Throwable t) {
                        t.getStackTrace();
                    }
                }
            }
        }

        if (displayName == null) {
            displayName = object.toString();
        }

        if (displayName.isEmpty()) {
            synchronized (TestUtils.class) {
                displayName = "[" + index + "] (empty)";
            }
        }

        return displayName;
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
     * Class to get a Collection of methods with a specific annotation sorted alphabetically
     *
     * @param clazz
     * @param annotation
     * @return
     */
    private static List<Method> getDeclaredMethods(Class<?> clazz, Class<? extends Annotation> annotation) {
        List<Method> methodList = new ArrayList<>();

        Method[] methods = clazz.getDeclaredMethods();
        for (Method method : methods) {
            if (method.isAnnotationPresent(annotation)) {
                int modifiers = method.getModifiers();
                if (!Modifier.isStatic(modifiers) && Modifier.isPublic(modifiers)) {
                    if (method.getParameterCount() == 0) {
                        Class<?> returnType = method.getReturnType();
                        if (Void.TYPE.isAssignableFrom(returnType)) {
                            methodList.add(method);
                        }
                    }
                }
            }
        }

        methodList.sort(Comparator.comparing(Method::getName));

        return methodList;
    }
}
