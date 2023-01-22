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

package org.devopology.test.engine.support.util;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Class to implement a manager of objects
 *
 * @param <T>
 */
public class Manager<T> {

    private Map<String, T> map;

    /**
     * Constructor
     */
    public Manager() {
        this.map = new LinkedHashMap<>();
    }

    /**
     * Method to determine if a key exists
     *
     * @param key
     * @return
     */
    public boolean containsKey(String key) {
        return map.containsKey(key);
    }

    /**
     * Method to remove a key/value
     */
    public Optional<T> remove(String key) {
        return Optional.ofNullable(map.remove(key));
    }

    /**
     * Method to add a key/value to the manager
     *
     * @param key
     * @param t
     * @return
     */
    public Manager put(String key, T t) {
        map.put(key, t);
        return this;
    }

    /**
     * Method to get a value for a key
     *
     * @param key
     * @return
     */
    public Optional<T> get(String key) {
        return Optional.ofNullable(map.get(key));
    }
}