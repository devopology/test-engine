package org.devopology.test.engine.test.example;

import org.devopology.test.engine.api.Metadata;
import org.devopology.test.engine.api.Named;
import org.devopology.test.engine.api.Parameter;
import org.devopology.test.engine.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Example test
 *
 * Made up... in real life you wouldn't do this
 */
public class CamelCaseFunctionTest {

    private static Function<String, String> FUNCTION = new CamelCaseFunction();

    @Parameter
    public Tuple parameter;

    @Parameter.Supplier
    public static Collection<Object> parameters() {
        Collection<Object> collection = new ArrayList<>();

        Tuple tuple = new Tuple("THIS STRING SHOULD BE IN CAMEL CASE", "thisStringShouldBeInCamelCase");
        collection.add(Named.of(tuple.input, tuple));

        tuple = new Tuple("THIS string SHOULD be IN camel CASE", "thisStringShouldBeInCamelCase");
        collection.add(Named.of(tuple.input, tuple));

        tuple = new Tuple("THIS", "this");
        collection.add(Named.of(tuple.input, tuple));

        tuple = new Tuple("tHis", "this");
        collection.add(Named.of(tuple.input, tuple));

        return collection;
    }



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
                    word = word.isEmpty() ? word : word.toLowerCase(Locale.getDefault());
                } else {
                    word = word.isEmpty() ? word :
                            (word.charAt(0) + "").toUpperCase(Locale.getDefault())
                                    + word.substring(1).toLowerCase(Locale.getDefault());
                }

                builder.append(word);
            }

            return builder.toString();
        }
    }

    private static class Tuple implements Metadata {

        public String input;
        public String expected;

        public Tuple(String input, String expected) {
            this.input = input;
            this.expected = expected;
        }

        @Override
        public String getDisplayName() {
            return "Tuple { " + input + " | " + expected + " }";
        }
    }

    @Test
    public void test() {
        String actual = FUNCTION.apply(parameter.input);
        System.out.println("test() input [" + parameter.input + "] expected [" + parameter.expected + "] actual [" + actual + "]");
        assertThat(actual).isEqualTo(parameter.expected);
    }
}
