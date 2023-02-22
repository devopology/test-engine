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

public interface Logger {

    /**
     * Method to return if INFO logging is enabled
     *
     * @return
     */
    boolean isInfoEnabled();
    
    /**
     * Method to log an INFO message
     *
     * @param message
     */
    void info(String message);

    /**
     * Method to log an INFO message
     *
     * @param format
     * @param object
     */
    void info(String format, Object object);

    /**
     * Method to log an INFO message
     *
     * @param format
     * @param objects
     */
    void info(String format, Object ... objects);

    /**
     * Method to log a "raw" INFO message
     *
     * @param message
     */
    void infoRaw(String message);

    /**
     * Method to log a "raw" INFO message
     *
     * @param format
     * @param object
     */
    void infoRaw(String format, Object object);

    /**
     * Method to log a "raw" INFO message
     *
     * @param format
     * @param objects
     */
    void infoRaw(String format, Object ... objects);

    /**
     * Method to return if WARNING logging is enabled
     *
     * @return
     */
    boolean isWarningEnabled();

    /**
     * Method to log a WARN message
     *
     * @param message
     */
    void warning(String message);

    void warning(String format, Object object);

    void warning(String format, Object ... objects);

    /**
     * Method to return if ERROR logging is enabled
     *
     * @return
     */
    boolean isErrorEnabled();

    /**
     * Method to log an ERROR message
     *
     * @param message
     */
    void error(String message);

    void error(String format, Object object);

    void error(String format, Object ... objects);

    /**
     * Method to return if DEBUG logging is enabled
     *
     * @return
     */
    boolean isDebugEnabled();

    /**
     * Method to log a DEBUG message
     *
     * @param message
     */
    void debug(String message);

    void debug(String format, Object object);

    void debug(String format, Object ... objects);

    /**
     * Method to return if TRACE logging is enabled
     *
     * @return
     */
    boolean isTraceEnabled();

    /**
     * Method to log a TRACE message
     *
     * @param message
     */
    void trace(String message);

    void trace(String format, Object object);

    void trace(String format, Object ... objects);
}
