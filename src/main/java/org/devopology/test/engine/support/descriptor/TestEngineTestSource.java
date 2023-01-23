package org.devopology.test.engine.support.descriptor;

import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestSource;

public class TestEngineTestSource implements TestSource {

    private TestDescriptor testDescriptor;

    public TestEngineTestSource(TestDescriptor testDescriptor) {
        this.testDescriptor = testDescriptor;
    }

    public TestDescriptor getTestDescriptor() {
        return testDescriptor;
    }
}
