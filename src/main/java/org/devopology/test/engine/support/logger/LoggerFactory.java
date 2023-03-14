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

package org.devopology.test.engine.support.logger;

import org.devopology.test.engine.support.logger.impl.LoggerImpl;

import java.util.HashMap;
import java.util.Map;

/**
 * Class to implement a LoggerFactory to create a Logger
 */
public final class LoggerFactory {

    private static final Map<String, Logger> LOGGER_MAP = new HashMap<>();

    /**
     * Constructor
     */
    private LoggerFactory() {
        // DO NOTHING
    }

    /**
     * Method to create a Logger for a Class
     *
     * @param clazz
     * @return
     */
    public static Logger getLogger(Class clazz) {
        return getLogger(clazz.getName());
    }

    /**
     * Method to create a named Logger
     *
     * @param name
     * @return
     */
    public static Logger getLogger(String name) {
        if ((name == null) || name.trim().isEmpty()) {
            throw new IllegalArgumentException("name is required");
        }

        Logger logger;

        synchronized (LOGGER_MAP) {
            logger = LOGGER_MAP.get(name.trim());
            if (logger == null) {
                logger = new LoggerImpl(name.trim());
                LOGGER_MAP.put(name, logger);
            }
        }

        return logger;
    }
}
