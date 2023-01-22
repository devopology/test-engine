/*
 * Copyright 2022-2023 Douglas Hoard
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.devopology.test.engine;

import org.junit.platform.engine.EngineDiscoveryRequest;
import org.junit.platform.engine.ExecutionRequest;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.EngineDescriptor;

import java.util.Optional;

public class TestEngine implements org.junit.platform.engine.TestEngine {

    private static final String ENGINE_ID = "devopology-test-engine";
    private static final String GROUP_ID = "org.devopology";
    private static final String ARTIFACT_ID = "test-engine";
    private static final String VERSION = TestEngineVersion.getVersion();

    @Override
    public String getId() {
        return ENGINE_ID;
    }

    @Override
    public Optional<String> getGroupId() {
        return Optional.of(GROUP_ID);
    }

    @Override
    public Optional<String> getArtifactId() {
        return Optional.of(ARTIFACT_ID);
    }

    @Override
    public Optional<String> getVersion() {
        return Optional.of(VERSION);
    }

    public TestEngine() {

    }

    @Override
    public TestDescriptor discover(EngineDiscoveryRequest discoveryRequest, UniqueId uniqueId) {
        // Create a EngineDescriptor as the target
        EngineDescriptor engineDescriptor = new EngineDescriptor(uniqueId, getId());

        // Create a DevopologyTestEngineDiscoverySelectorResolver and
        // resolve selectors, adding them to the engine descriptor
        new TestEngineDiscoverySelectorResolver().resolveSelectors(discoveryRequest, engineDescriptor);

        // Return the engine descriptor with all child test descriptors
        return engineDescriptor;
    }

    @Override
    public void execute(ExecutionRequest executionRequest) {
        // Create a DevopologyTestEngineExecutor and execute the execution request
        new TestEngineExecutor().execute(executionRequest);
    }
}
