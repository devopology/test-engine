package org.devopology.test.engine.test.example.inheritance;

import org.devopology.test.engine.api.Parameter;

import java.util.Collection;
import java.util.stream.Collectors;

public abstract class EvenBaseTest extends BaseTest {

    protected static Collection<Parameter> parameters() {
        return BaseTest
                .parameters()
                .stream()
                .filter(parameter -> {
                    int value = (Integer) parameter.value();
                    return (value % 2) == 0;
                })
                .collect(Collectors.toList());
    }
}
