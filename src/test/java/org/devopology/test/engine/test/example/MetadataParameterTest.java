package org.devopology.test.engine.test.example;

import org.devopology.test.engine.api.AfterAll;
import org.devopology.test.engine.api.BeforeAll;
import org.devopology.test.engine.api.Metadata;
import org.devopology.test.engine.api.Parameter;
import org.devopology.test.engine.api.ParameterSupplier;
import org.devopology.test.engine.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

/**
 * Example test engine test... only runs from an IDE or via the test engine ConsoleRunner
 */
public class MetadataParameterTest {

    // Custom parameter that has metadata
    private static class MetadataParameter implements Metadata {

        private String value;

        public MetadataParameter(String value) {
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

    @ParameterSupplier
    public static Collection<MetadataParameter> parameters() {
        Collection<MetadataParameter> collection = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            collection.add(new MetadataParameter(String.valueOf(UUID.randomUUID())));
        }

        return collection;
    }

    @Parameter
    public MetadataParameter parameter;

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
}
