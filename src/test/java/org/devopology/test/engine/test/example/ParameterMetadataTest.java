package org.devopology.test.engine.test.example;

import org.devopology.test.engine.api.Metadata;
import org.devopology.test.engine.api.TestEngine;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

/**
 * Example test
 */
public class ParameterMetadataTest {

    @TestEngine.ParameterInject
    public ParameterWithMetadata parameter;

    @TestEngine.ParameterSupplier
    public static Collection<ParameterWithMetadata> parameters() {
        Collection<ParameterWithMetadata> collection = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            collection.add(new ParameterWithMetadata(String.valueOf(UUID.randomUUID())));
        }

        return collection;
    }

    @TestEngine.BeforeAll
    public void beforeAll() {
        System.out.println("beforeAll()");
    }

    @TestEngine.Test
    public void test1() {
        String value = parameter.getValue();
        System.out.println("test1(" + value + ")");
    }

    @TestEngine.Test
    public void test2() {
        String value = parameter.getValue();
        System.out.println("test2(" + value + ")");
    }

    @TestEngine.AfterAll
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
