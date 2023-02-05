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

import org.devopology.test.engine.support.api.ParameterMapImpl;

import java.util.Objects;

/**
 * Interface to implement a parameter map
 */
@SuppressWarnings("unchecked")
public interface ParameterMap extends Metadata {

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

        String nameTrimmed = name.trim();
        if (nameTrimmed.isEmpty()) {
            throw new IllegalArgumentException();
        }

        return new ParameterMapImpl(nameTrimmed);
    }

}
