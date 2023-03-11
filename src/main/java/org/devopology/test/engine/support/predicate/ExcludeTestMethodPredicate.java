package org.devopology.test.engine.support.predicate;

import java.lang.reflect.Method;

public final class ExcludeTestMethodPredicate extends RegexPredicate<Method> {

    private ExcludeTestMethodPredicate(String regex) {
        super(regex);
    }

    @Override
    public boolean test(Method method) {
        matcher.reset(method.getName());
        return matcher.find();
    }

    public static ExcludeTestMethodPredicate of(String regex) {
        return new ExcludeTestMethodPredicate(regex);
    }
}
