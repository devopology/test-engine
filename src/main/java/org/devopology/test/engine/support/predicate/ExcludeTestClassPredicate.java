package org.devopology.test.engine.support.predicate;

public final class ExcludeTestClassPredicate extends RegexPredicate<Class<?>> {

    private ExcludeTestClassPredicate(String regex) {
        super(regex);
    }

    @Override
    public boolean test(Class<?> clazz) {
        matcher.reset(clazz.getName());
        return matcher.find();
    }

    public static ExcludeTestClassPredicate of(String regex) {
        return new ExcludeTestClassPredicate(regex);
    }
}
