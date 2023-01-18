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

package org.devopology.test.engine.internal.util;

import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Class to implement logger
 */
public class Logger {

    private static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    private static final boolean DEBUG;
    private static final boolean TRACE;

    static {
        String trace = System.getenv().get("DEVOPOLOGY_TEST_ENGINE_TRACE");
        if ("true".equals(trace)) {
            TRACE = true;
            DEBUG = true;
        } else {
            TRACE = false;
            String debug = System.getenv().get("DEVOPOLOGY_TEST_ENGINE_DEBUG");
            if ("true".equals(debug)) {
                DEBUG = true;
            } else {
                DEBUG = false;
            }
        }
    }

    private String className;

    /**
     * Constructor
     *
     * @param className
     */
    Logger(String className) {
        this.className = className;
    }

    /**
     * Method to log an INFO message
     *
     * @param message
     */
    public void info(String message) {
        log(System.out, createMessage("INFO", className, message));
    }

    /**
     * Method to log a WARN message
     *
     * @param message
     */
    public void warn(String message) {
        log(System.out, createMessage("WARNING", className, message));
    }

    /**
     * Method to log an ERROR message
     *
     * @param message
     */
    public void error(String message) {
        log(System.err, createMessage("ERROR", className, message));
    }

    /**
     * Method to log a DEBUG message
     *
     * @param message
     */
    public void debug(String message) {
        if (DEBUG) {
            log(System.out, createMessage("DEBUG", className, message));
        }
    }

    /**
     * Method to log a TRACE message
     *
     * @param message
     */
    public void trace(String message) {
        if (TRACE) {
            log(System.out, createMessage("TRACE", className, message));
        }
    }

    /**
     * Method to log to a PrintStream
     *
     * @param printStream
     * @param message
     */
    private void log(PrintStream printStream, String message) {
        synchronized (printStream) {
            printStream.println(message);
            printStream.flush();
        }
    }

    /**
     * Method to create a log message
     *
     * @param level
     * @param className
     * @param message
     * @return
     */
    private static String createMessage(String level, String className, String message) {
        synchronized (SIMPLE_DATE_FORMAT) {
            return String.format(
                    "%s [%s] %-5s %s - %s",
                    SIMPLE_DATE_FORMAT.format(new Date()),
                    Thread.currentThread().getName(),
                    level,
                    className,
                    message);
        }
    }
}