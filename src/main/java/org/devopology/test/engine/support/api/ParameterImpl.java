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

import org.devopology.test.engine.api.Parameter;

import java.util.Objects;

@SuppressWarnings("unchecked")
public class ParameterImpl implements Parameter {

    private final String name;
    private final Object value;

    /**
     * Constructor
     *
     * @param name
     * @param value
     */
    public ParameterImpl(String name, Object value) {
        Objects.requireNonNull(name);

        if (name.trim().isEmpty()) {
            throw new IllegalArgumentException("name is empty");
        }

        this.name = name.trim();
        this.value = value;
    }

    /**
     * Method to get the parameter name
     *
     * @return
     */
    @Override
    public String name() {
        return name;
    }

    /**
     * Method to get the parameter value
     * @return
     * @param <T>
     */
    @Override
    public <T> T value() {
        return (T) value;
    }

    /**
     * Method to get the parameter value cast to a specific type
     *
     * @param clazz
     * @return
     * @param <T>
     */
    @Override
    public <T> T value(Class<T> clazz) {
        return clazz.cast(value);
    }

    @Override
    public String toString() {
        if (value == null) {
            return "null";
        } else {
            return value.toString();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ParameterImpl named = (ParameterImpl) o;
        return Objects.equals(name, named.name) && Objects.equals(value, named.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, value);
    }
}
