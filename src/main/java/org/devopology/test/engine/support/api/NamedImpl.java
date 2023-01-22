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

import org.devopology.test.engine.api.Named;

import java.util.Objects;

public class NamedImpl implements Named {

    private String name;
    private Object payload;

    /**
     * Constructor
     *
     * @param name
     * @param payload
     */
    public NamedImpl(String name, Object payload) {
        Objects.requireNonNull(name);

        name = name.trim();
        if (name.isEmpty()) {
            throw new IllegalArgumentException();
        }

        this.name = name;
        this.payload = payload;
    }

    /**
     * Method to get the display name
     *
     * @return
     */
    public String getDisplayName() {
        return name;
    }

    /**
     * Method to get the payload
     *
     * @return
     */
    public Object getPayload() {
        return payload;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NamedImpl named = (NamedImpl) o;
        return Objects.equals(name, named.name) && Objects.equals(payload, named.payload);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, payload);
    }

    @Override
    public int compareTo(Object o) {
        Objects.requireNonNull(o);

        if (!(o instanceof Named)) {
            throw new ClassCastException();
        }

        return name.compareTo(((Named) o).getDisplayName());
    }
}
