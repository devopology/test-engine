package org.devopology.test.engine.test.example.inheritance;

import org.devopology.test.engine.api.Parameter;

import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class EvenBaseTest extends BaseTest {

    protected static Stream<Parameter> parameters() {
        return BaseTest
                .parameters()
                .filter(parameter -> {
                    int value = parameter.value();
                    return (value % 2) == 0;
                });
    }
}
