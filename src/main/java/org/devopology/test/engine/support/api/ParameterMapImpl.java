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

package org.devopology.test.engine.support.api;

import org.devopology.test.engine.api.ParameterMap;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Class to implement a {@link ParameterMap}
 */
@SuppressWarnings("unchecked")
public class ParameterMapImpl implements ParameterMap {

    private final String name;
    private final Map<String, Object> map;

    public ParameterMapImpl(String name) {
        Objects.requireNonNull(name);

        String nameTrimmed = name.trim();
        if (nameTrimmed.isEmpty()) {
            throw new IllegalArgumentException();
        }

        this.name = nameTrimmed;
        this.map = new LinkedHashMap<>();
    }

    @Override
    public String getDisplayName() {
        return name;
    }

    @Override
    public ParameterMap put(String key, Object object) {
        Objects.requireNonNull(key);

        String keyTrimmed = key.trim();
        if (keyTrimmed.isEmpty()) {
            throw new IllegalArgumentException();
        }

        map.put(keyTrimmed, object);
        return this;
    }

    @Override
    public boolean containsKey(String key) {
        Objects.requireNonNull(key);

        String keyTrimmed = key.trim();
        if (keyTrimmed.isEmpty()) {
            throw new IllegalArgumentException();
        }

        return map.containsKey(keyTrimmed);
    }

    @Override
    public <T> T get(String key) {
        Objects.requireNonNull(key);

        String keyTrimmed = key.trim();
        if (keyTrimmed.isEmpty()) {
            throw new IllegalArgumentException();
        }

        return (T) map.get(keyTrimmed);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ParameterMapImpl that = (ParameterMapImpl) o;
        return Objects.equals(name, that.name) && Objects.equals(map, that.map);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, map);
    }
}
