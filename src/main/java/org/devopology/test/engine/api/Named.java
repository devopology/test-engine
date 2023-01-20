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

import org.devopology.test.engine.internal.NamedImpl;

import java.util.Objects;

/**
 * Class to implement a payload wrapper with specific display name
 */
public interface Named {

    /**
     * Method to get the display name
     *
     * @return
     */
    String getName();

    /**
     * Method to get the payload
     *
     * @return
     */
    Object getPayload();

    /**
     * Method to create a Named object (useful for a static import)
     *
     * @param name
     * @param payload
     * @return
     */
    static Named named(String name, Object payload) {
        return of(name, payload);
    }

    /**
     * Method to create a Named object
     *
     * @param name
     * @param payload
     * @return
     */
    static Named of(String name, Object payload) {
        Objects.requireNonNull(name);
        Objects.requireNonNull(payload);

        name = name.trim();
        if (name.isEmpty()) {
            throw new IllegalArgumentException();
        }

        return new NamedImpl(name, payload);
    }
}
