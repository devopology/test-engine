package org.devopology.test.engine.test.example;

import org.devopology.test.engine.api.Parameter;
import org.devopology.test.engine.api.ParameterSupplier;
import org.devopology.test.engine.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Example test engine test... only runs from an IDE or via the test engine ConsoleRunner
 *
 * Made up... in real life you wouldn't do this
 */
public class TestingAFunction {

    // Based on https://www.baeldung.com/java-string-to-camel-case
    private static class CamelCaseFunction implements Function<String, String> {

        public String apply(String string) {
            if (string == null) {
                return null;
            }

            String[] words = string.split("[\\W_]+");
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < words.length; i++) {
                String word = words[i];
                if (i == 0) {
                    word = word.isEmpty() ? word : word.toLowerCase();
                } else {
                    word = word.isEmpty() ? word : Character.toUpperCase(word.charAt(0)) + word.substring(1).toLowerCase();
                }
                builder.append(word);
            }
            return builder.toString();
        }
    }

    private static class Tuple {

        public String input;
        public String expected;

        public Tuple(String input, String expected) {
            this.input = input;
            this.expected = expected;
        }

        // Optional / called via reflection
        public String getDisplayName() {
            return input + " -> " + expected;
        }
    }

    @ParameterSupplier
    public static Collection<Tuple> values() {
        Collection<Tuple> collection = new ArrayList<>();

        collection.add(new Tuple("THIS STRING SHOULD BE IN CAMEL CASE", "thisStringShouldBeInCamelCase"));
        collection.add(new Tuple("THIS string SHOULD be IN camel CASE", "thisStringShouldBeInCamelCase"));
        collection.add(new Tuple("THIS", "this"));
        collection.add(new Tuple("tHis", "this"));

        return collection;
    }

    @Parameter
    public Tuple tuple;

    private static Function<String, String> FUNCTION = new CamelCaseFunction();

    @Test
    public void test() {
        assertThat(FUNCTION.apply(tuple.input)).isEqualTo(tuple.expected);
    }
}
