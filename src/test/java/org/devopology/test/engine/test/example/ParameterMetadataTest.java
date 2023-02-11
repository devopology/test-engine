package org.devopology.test.engine.test.example;

import org.devopology.test.engine.api.AfterAll;
import org.devopology.test.engine.api.BeforeAll;
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

    @BeforeAll
    public void beforeAll() {
        System.out.println("beforeAll()");
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

    @AfterAll
    public void afterAll() {
        System.out.println("afterAll()");
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
