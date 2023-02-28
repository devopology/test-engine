package org.devopology.test.engine.test.example;

import org.devopology.test.engine.api.AfterAllTests;
import org.devopology.test.engine.api.BeforeAllTests;
import org.devopology.test.engine.api.Metadata;
import org.devopology.test.engine.api.Parameter;
import org.devopology.test.engine.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * Example test
 */
public class ParameterMetadataTest {

    @Parameter
    public ParameterWithMetadata parameter;

    @Parameter.Supplier
    public static Stream<ParameterWithMetadata> parameters() {
        Collection<ParameterWithMetadata> collection = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            collection.add(new ParameterWithMetadata(String.valueOf(UUID.randomUUID())));
        }

        return collection.stream();
    }

    @BeforeAllTests
    public void beforeAllTests() {
        System.out.println("beforeAllTests()");
    }

    @Test
    public void test1() {
        String value = parameter.getValue();
        System.out.println("test1(" + value + ")");
    }

    @Test
    public void test2() {
        String value = parameter.getValue();
        System.out.println("test2(" + value + ")");
    }

    @AfterAllTests
    public void afterAllTests() {
        System.out.println("afterAllTests()");
    }

    // Custom parameter that has metadata
    private static class ParameterWithMetadata implements Metadata {

        private final String value;

        public ParameterWithMetadata(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        @Override
        public String getDisplayName() {
            return "test value { " + value + " }";
        }
    }
}
