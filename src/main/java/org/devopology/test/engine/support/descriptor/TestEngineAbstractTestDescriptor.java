package org.devopology.test.engine.support.descriptor;

import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.AbstractTestDescriptor;

import java.util.Optional;

public abstract class TestEngineAbstractTestDescriptor extends AbstractTestDescriptor {

    protected TestEngineAbstractTestDescriptor(UniqueId uniqueId, String displayName) {
        super(uniqueId, displayName);
    }

    public abstract Optional<TestSource> getSource();
}
