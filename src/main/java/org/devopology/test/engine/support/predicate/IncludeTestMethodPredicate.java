package org.devopology.test.engine.support.predicate;

import java.lang.reflect.Method;

public class IncludeTestMethodPredicate extends RegexPredicate<Method> {

    private IncludeTestMethodPredicate(String regex) {
        super(regex);
    }

    @Override
    public boolean test(Method method) {
        matcher.reset(method.getName());
        return matcher.find();
    }

    public static IncludeTestMethodPredicate of(String regex) {
        return new IncludeTestMethodPredicate(regex);
    }
}
