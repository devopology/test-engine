package org.devopology.test.engine.test.example;

import org.devopology.test.engine.api.AfterAll;
import org.devopology.test.engine.api.BeforeAll;
import org.devopology.test.engine.api.Named;
import org.devopology.test.engine.api.Parameter;
import org.devopology.test.engine.api.ParameterSupplier;
import org.devopology.test.engine.api.Test;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Function;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;

/**
 * Example test engine test... only runs from an IDE or via the test engine ConsoleRunner
 */
public class MixedObjectsTest {

    @ParameterSupplier
    public static Collection<Named> parameters() {
        Set<Named> collection = new LinkedHashSet<>();

        collection.add(Named.of("BigDecimal", new BigDecimal("1000000000000000000000")));
        collection.add(Named.of("Integer", 1));
        collection.add(Named.of("Map", new HashMap<String, String>()));
        collection.add(Named.of("String", "This is a string"));
        collection.add(Named.of("null", null));
        collection.add(Named.of("null2", null));

        return collection;
    }

    @Parameter
    public Object parameter;

    @BeforeAll
    public void beforeAll() {
        System.out.println("beforeAll()");
    }

    // Function to test... typically this would be core project code
    private static Function<Object, String> TO_SPECIAL_NAME = object -> {
        if (object == null) {
            throw new NullPointerException();
        }

        if (object instanceof String) {
            return "string/" + object;
        } else if (object instanceof Integer) {
            return "int/" + object;
        } else if (object instanceof BigDecimal) {
            return "bigDecimal/" + object;
        } else {
            throw new IllegalArgumentException("Unhandled type [" + object.getClass().getName() + "]");
        }
    };

    @Test
    public void test() {
        System.out.println("[" + parameter + "]");

        if (parameter instanceof String) {
            assertThat(TO_SPECIAL_NAME.apply(parameter)).isEqualTo("string/" + parameter);
        } else if (parameter instanceof Integer) {
            assertThat(TO_SPECIAL_NAME.apply(parameter)).isEqualTo("int/" + parameter);
        } else if (parameter instanceof BigDecimal) {
            assertThat(TO_SPECIAL_NAME.apply(parameter)).isEqualTo("bigDecimal/" + parameter);
        } else if (parameter == null) {
            assertThatExceptionOfType(NullPointerException.class).isThrownBy(() -> TO_SPECIAL_NAME.apply(parameter));
        } else {
            assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> TO_SPECIAL_NAME.apply(parameter));
        }

        System.out.println("[" + parameter + "] PASSED");
    }

    @AfterAll
    public void afterAll() {
        System.out.println("afterAll()");
    }
}
