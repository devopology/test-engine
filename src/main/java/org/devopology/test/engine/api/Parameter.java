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

import org.devopology.test.engine.support.api.ParameterImpl;

import java.util.Objects;

/**
 * Interface to implement a Parameter
 */
public interface Parameter {

    /**
     * Method to get the parameter name
     * @return
     */
    String name();

    /**
     * Method to get the parameter value
     *
     * @return
     * @param <T>
     */
    <T> T value();

    /**
     * Method to create a Named object (useful for a static import)
     *
     * @param name
     * @param value
     * @return
     */
    static Parameter parameter(String name, Object value) {
        return of(name, value);
    }

    /**
     * Method to create a Named object
     *
     * @param name
     * @param value
     * @return
     */
    static Parameter of(String name, Object value) {
        Objects.requireNonNull(name);

        if (name.trim().isEmpty()) {
            throw new IllegalArgumentException("name is empty");
        }

        return new ParameterImpl(name.trim(), value);
    }

    static Parameter of(String value) {
        Objects.requireNonNull(value);

        if (value.trim().isEmpty()) {
            throw new IllegalArgumentException("value is empty");
        }

        return new ParameterImpl(value, value);
    }
}
