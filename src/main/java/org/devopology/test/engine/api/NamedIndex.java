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

import org.devopology.test.engine.internal.NamedIndexImpl;

import java.util.Objects;

/**
 * Class to implement a payload wrapper with specific display name
 */
public interface NamedIndex {

    String getFormat();

    Object getPayload();

    /**
     * Method to create a NamedIndex
     *
     * @param payload
     * @return
     */
    static NamedIndex of(Object payload) {
        Objects.requireNonNull(payload);

        return new NamedIndexImpl(payload);
    }

    /**
     * Method to create a NamedIndex using a String format ("index %d")
     *
     * @param format
     * @param payload
     * @return
     */
    static NamedIndex of(String format, Object payload) {
        Objects.requireNonNull(format);
        Objects.requireNonNull(payload);

        format = format.trim();
        if (format.isEmpty()) {
            throw new IllegalArgumentException();
        }

        try {
            String value = String.format(format, 1);
            if (format.equals(value)) {
                throw new IllegalArgumentException();
            }
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Throwable t) {
            throw new IllegalArgumentException();
        }

        return new NamedIndexImpl(format, payload);
    }


    /**
     * Method to create a NamedIndex (useful for a static import)
     *
     * @param payload
     * @return
     */
    static NamedIndex indexed(Object payload) {
        return of(payload);
    }

    /**
     * Method to create a NamedIndex using a specific String format string ("index %d")
     *
     * @param format
     * @param payload
     * @return
     */
    static NamedIndex indexed(String format, Object payload) {
        return of(format, payload);
    }
}
