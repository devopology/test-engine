package org.devopology.test.engine.api;

import org.junit.platform.commons.annotation.Testable;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public @interface TestEngine {

    @Target({ ElementType.ANNOTATION_TYPE, ElementType.METHOD })
    @Retention(RetentionPolicy.RUNTIME)
    /**
     * Annotation for a parameter setter method
     */
    @interface ParameterSetter {

    }

    @Target({ ElementType.ANNOTATION_TYPE, ElementType.METHOD, ElementType.FIELD })
    @Retention(RetentionPolicy.RUNTIME)
    /**
     * Annotation for a parameter supplier
     */
    @interface ParameterSupplier {

    }

    @Target({ ElementType.ANNOTATION_TYPE, ElementType.METHOD, ElementType.FIELD })
    @Retention(RetentionPolicy.RUNTIME)
    @interface BeforeClass {

    }

    @Target({ ElementType.ANNOTATION_TYPE, ElementType.METHOD, ElementType.FIELD })
    @Retention(RetentionPolicy.RUNTIME)
    @interface BeforeAll {

    }

    @Target({ ElementType.ANNOTATION_TYPE, ElementType.METHOD, ElementType.FIELD })
    @Retention(RetentionPolicy.RUNTIME)
    @interface BeforeEach {

    }

    @Testable
    @Target({ ElementType.ANNOTATION_TYPE, ElementType.METHOD })
    @Retention(RetentionPolicy.RUNTIME)
    @interface Test {

    }

    @Target({ ElementType.ANNOTATION_TYPE, ElementType.METHOD, ElementType.FIELD })
    @Retention(RetentionPolicy.RUNTIME)
    @interface AfterEach {

    }

    @Target({ ElementType.ANNOTATION_TYPE, ElementType.METHOD, ElementType.FIELD })
    @Retention(RetentionPolicy.RUNTIME)
    @interface AfterAll {

    }

    @Target({ ElementType.ANNOTATION_TYPE, ElementType.METHOD, ElementType.TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    @interface AfterClass {

    }

    @Target({ ElementType.ANNOTATION_TYPE, ElementType.METHOD, ElementType.TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    /**
     * Annotation for a disabled test class or method
     */
    @interface Disabled {

    }

    @Target({ ElementType.ANNOTATION_TYPE, ElementType.TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    /**
     * Annotation to mark a test class as a base class (don't execute)
     */
    @interface BaseClass {

    }

    @Target({ ElementType.ANNOTATION_TYPE, ElementType.METHOD, ElementType.TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    /**
     * Annotation for a before each method
     */
    @interface DisplayName {
        String value();
    }
}
