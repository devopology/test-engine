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

package org.devopology.test.engine.internal.descriptor;

import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.UniqueId;

import java.util.Optional;

public class TestClassTestTestDescriptor extends TestEngineAbstractTestDescriptor {

    private TestSource testSource;
    private Class<?> testClass;

    public TestClassTestTestDescriptor(UniqueId uniqueId, String displayName, Class<?> testClass) {
        super(uniqueId, displayName);
        this.testSource = new TestEngineTestSource(this);
        this.testClass = testClass;
    }

    @Override
    public Type getType() {
        return Type.CONTAINER_AND_TEST;
    }

    @Override
    public Optional<TestSource> getSource() {
        return Optional.of(testSource);
    }

    public Class<?> getTestClass() {
        return testClass;
    }
}
