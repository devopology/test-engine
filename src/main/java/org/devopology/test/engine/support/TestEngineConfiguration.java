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

package org.devopology.test.engine.support;

public final class TestEngineConfiguration {

    private TestEngineConfiguration() {
        // DO NOTHING
    }

    public static String getValue(String systemProperty, String environmentVariable) {
        String systemPropertyValue = System.getProperty(systemProperty);
        String environmentVariableValue = System.getenv(environmentVariable);

        if ((systemPropertyValue != null) && !systemPropertyValue.trim().isEmpty()) {
            return systemPropertyValue;
        } else if ((environmentVariableValue != null) && !environmentVariableValue.trim().isEmpty()) {
            return environmentVariableValue;
        }

        return null;
    }
}