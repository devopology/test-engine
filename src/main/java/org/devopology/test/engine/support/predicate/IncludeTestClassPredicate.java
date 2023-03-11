package org.devopology.test.engine.support.predicate;

public final class IncludeTestClassPredicate extends RegexPredicate<Class<?>> {

    private IncludeTestClassPredicate(String regex) {
        super(regex);
    }

    @Override
    public boolean test(Class<?> clazz) {
        matcher.reset(clazz.getName());
        return matcher.find();
    }

    public static IncludeTestClassPredicate of(String regex) {
        return new IncludeTestClassPredicate(regex);
    }
}
