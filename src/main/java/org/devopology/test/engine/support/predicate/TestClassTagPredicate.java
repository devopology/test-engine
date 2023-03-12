package org.devopology.test.engine.support.predicate;

import org.devopology.test.engine.api.TestEngine;
import org.devopology.test.engine.support.TestEngineException;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public final class TestClassTagPredicate extends RegexPredicate<Class<?>> {

    private TestClassTagPredicate(String regex) {
        super(regex);
    }

    @Override
    public boolean test(Class<?> clazz) {
        if (!clazz.isAnnotationPresent(TestEngine.Tag.class)) {
            return false;
        }

        try {
            Annotation annotation = clazz.getAnnotation(TestEngine.Tag.class);
            Class<? extends Annotation> type = annotation.annotationType();
            Method valueMethod = type.getDeclaredMethod("value", (Class<?>[]) null);
            String tag = valueMethod.invoke(annotation, (Object[]) null).toString();

            matcher.reset(tag);
            return matcher.find();
        } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
            throw new TestEngineException(String.format("Invalid @TestEngine.Tag configuration", e));
        }
    }

    public static TestClassTagPredicate of(String regex) {
        return new TestClassTagPredicate(regex);
    }
}
