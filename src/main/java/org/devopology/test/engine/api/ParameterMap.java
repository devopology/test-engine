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

package org.devopology.test.engine.api;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Interface to implement a parameter map
 */
@SuppressWarnings("unchecked")
public interface ParameterMap {

    /**
     * Method to get the display name
     * @return
     */
    default String getName() {
        return this.getClass().getName();
    }

    /**
     * Method to put a key / value into the {@link ParameterMap}
     * <p/>
     * @param key
     * @param value
     * @return
     */
    ParameterMap put(String key, Object value);

    /**
     * Method to determine if a {@link ParameterMap} contains a value for a key
     * <p/>
     * @param key
     * @return
     */
    boolean containsKey(String key);

    /**
     * Method to get a value for a key from the {@link ParameterMap}
     * <p/>
     * @param key
     * @return the value for a key or {@code null} if none exists
     */
    <T> T get(String key);

    /**
     * Method to create a {@link ParameterMap}
     * <p/>
     * @param name
     * @return an empty {@link ParameterMap} with a name
     */
    static ParameterMap of(String name) {
        Objects.requireNonNull(name);

        name = name.trim();
        if (name.isEmpty()) {
            throw new IllegalArgumentException();
        }

        return new ParameterMapImpl(name);
    }

    /**
     * Class to implement a {@link ParameterMap}
     */
    class ParameterMapImpl implements ParameterMap {

        private String name;
        private Map<String, Object> map;

        private ParameterMapImpl(String name) {
            Objects.requireNonNull(name);

            name = name.trim();
            if (name.isEmpty()) {
                throw new IllegalArgumentException();
            }

            this.name = name;
            map = new LinkedHashMap<>();
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public ParameterMap put(String key, Object object) {
            Objects.requireNonNull(key);
            Objects.requireNonNull(object);

            key = key.trim();
            if (key.isEmpty()) {
                throw new IllegalArgumentException();
            }

            map.put(key, object);
            return this;
        }

        @Override
        public boolean containsKey(String key) {
            Objects.requireNonNull(key);

            key = key.trim();
            if (key.isEmpty()) {
                throw new IllegalArgumentException();
            }

            return map.containsKey(key);
        }

        @Override
        public <T> T get(String key) {
            Objects.requireNonNull(key);

            key = key.trim();
            if (key.isEmpty()) {
                throw new IllegalArgumentException();
            }

            return (T) map.get(key);
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
}
