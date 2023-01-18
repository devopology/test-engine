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

package org.devopology.test.engine.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Class to implement collector of Throwable exceptions
 */
public class ThrowableCollector {

    private List<Throwable> throwables;

    /**
     * Constructor
     */
    public ThrowableCollector() {
        throwables = new ArrayList<>();
    }

    /**
     * Method to add a Throwable to the collector
     *
     * @param throwable
     * @return
     */
    public ThrowableCollector add(Throwable throwable) {
        throwables.add(throwable);
        return this;
    }

    /**
     * Method to add all Throwable exceptions from another ThrowableCollector
     *
     * @param throwableCollector
     * @return
     */
    public ThrowableCollector addAll(ThrowableCollector throwableCollector) {
        this.throwables.addAll(throwableCollector.getThrowables());
        return this;
    }

    /**
     * Method to return whether the collector is empty
     *
     * @return
     */
    public boolean isEmpty() {
        return throwables.isEmpty();
    }

    /**
     * Method to get the collector Throwable count
     *
     * @return
     */
    public int count() {
        return throwables.size();
    }

    /**
     * Method to get the first Throwable contained in the collector
     *
     * @return
     */
    public Throwable getFirstThrowable() {
        if (throwables.size() > 0) {
            return throwables.get(0);
        } else {
            return null;
        }
    }

    /**
     * Method to get a Collection of all Throwable exceptions in the collector
     *
     * @return
     */
    public Collection<Throwable> getThrowables() {
        return throwables;
    }
}
