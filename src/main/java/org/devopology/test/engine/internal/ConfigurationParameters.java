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

package org.devopology.test.engine.internal;

import java.util.Optional;
import java.util.Set;

/**
 * Class to implement {@link org.junit.platform.engine.ConfigurationParameters} without any parameters
 */
@SuppressWarnings("unchecked")
public class ConfigurationParameters implements org.junit.platform.engine.ConfigurationParameters {

    @Override
    public Optional<String> get(String s) {
        return Optional.empty();
    }

    @Override
    public Optional<Boolean> getBoolean(String s) {
        return Optional.empty();
    }

    @Override
    @SuppressWarnings("deprecation")
    public int size() {
        return 0;
    }

    @Override
    public Set<String> keySet() {
        return null;
    }
}
