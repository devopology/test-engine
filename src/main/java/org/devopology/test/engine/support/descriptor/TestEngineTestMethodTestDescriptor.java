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

package org.devopology.test.engine.support.descriptor;

import org.devopology.test.engine.api.Parameter;
import org.junit.platform.engine.UniqueId;

import java.lang.reflect.Method;

public class TestEngineTestMethodTestDescriptor extends TestEngineAbstractTestDescriptor {

    private final Class<?> testClass;
    private final Parameter testParameter;
    private final Method testMethod;

    public TestEngineTestMethodTestDescriptor(
            UniqueId uniqueId,
            String displayName,
            Class<?> testClass,
            Parameter testParameter,
            Method testMethod) {
        super(uniqueId, displayName);
        this.testClass = testClass;
        this.testParameter = testParameter;
        this.testMethod = testMethod;
    }

    @Override
    public Type getType() {
        return Type.TEST;
    }

    public Class<?> getTestClass() {
        return testClass;
    }

    public Parameter getTestParameter() {
        return testParameter;
    }

    public Method getTestMethod() {
        return testMethod;
    }
}
