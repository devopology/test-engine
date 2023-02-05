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

import java.util.Optional;
import java.util.function.Consumer;

/**
 * Class to implement switch functionally based on an Object's class
 */
@SuppressWarnings("unchecked")
public final class Switch {

    /**
     * Constructor
     */
    private Switch() {
        // DO NOTHING
    }

    /**
     * Method to implement a "switch" based on an Object's class
     * @param object
     * @param consumers
     * @param <T>
     */
    public static <T> void switchType(Object object, Consumer... consumers) {
        if (consumers != null) {
            for (Consumer consumer : consumers) {
                consumer.accept(object);
            }
        }
    }

    /**
     * Method to implement a switch "case" based on an Object's class
     * @param clazz
     * @param consumer
     * @return
     * @param <T>
     */
    public static <T> Consumer switchCase(Class<T> clazz, Consumer<T> consumer) {
        if ((clazz != null) && (consumer != null)){
            return object -> Optional.ofNullable(object).filter(clazz::isInstance).map(clazz::cast).ifPresent(consumer);
        } else {
            return null;
        }
    }
}
