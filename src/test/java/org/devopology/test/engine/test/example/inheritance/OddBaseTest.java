package org.devopology.test.engine.test.example.inheritance;

import org.devopology.test.engine.api.Named;

import java.util.Collection;
import java.util.stream.Collectors;

public abstract class OddBaseTest extends BaseTest {

    protected static Collection<Named> parameters() {
        return BaseTest
                .parameters()
                .stream()
                .filter(named -> {
                    int value = (Integer) named.getPayload();
                    return (value % 2) != 0;
                })
                .collect(Collectors.toList());
    }
}
