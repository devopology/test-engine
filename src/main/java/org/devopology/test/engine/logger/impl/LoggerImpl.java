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

package org.devopology.test.engine.logger.impl;

import org.devopology.test.engine.logger.Logger;

import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Class to implement logger
 */
public class LoggerImpl implements Logger {

    private static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    private static int OFF = 0;
    private static int ERROR = 100;
    private static int WARNING = 200;
    private static int INFO = 300;
    private static int DEBUG = 400;
    private static int TRACE = 500;

    private static final Map<String, Integer> LOG_LEVEL_MAP;

    static {
        LOG_LEVEL_MAP = new HashMap<>();
        LOG_LEVEL_MAP.put("OFF", OFF);
        LOG_LEVEL_MAP.put("ERROR", ERROR);
        LOG_LEVEL_MAP.put("WARNING", WARNING);
        LOG_LEVEL_MAP.put("INFO", INFO);
        LOG_LEVEL_MAP.put("DEBUG", DEBUG);
        LOG_LEVEL_MAP.put("TRACE", TRACE);
    }

    private String className;
    private int logLevel = INFO;

    /**
     * Constructor
     *
     * @param className
     */
    public LoggerImpl(String className) {
        this.className = className;

        String logLevelString = System.getProperty("devopology.test.engine.log.level");
        if (logLevelString != null) {
            logLevelString = logLevelString.toUpperCase().trim();
        }

        if (logLevelString == null) {
            logLevelString = System.getenv().get("DEVOPOLOGY_TEST_ENGINE_LOG_LEVEL");
            if (logLevelString != null) {
                logLevelString = logLevelString.toUpperCase().trim();
            }
        }

        if ((logLevelString == null) || logLevelString.isEmpty()) {
            logLevel = INFO;
        } else {
            logLevel = LOG_LEVEL_MAP.getOrDefault(logLevelString, INFO);
        }
    }

    /**
     * Method to return if INFO logging is enabled
     *
     * @return
     */
    public boolean isInfoEnabled() {
        return logLevel >= INFO;
    }

    /**
     * Method to log an INFO message
     *
     * @param message
     */
    public void info(String message) {
        if (isInfoEnabled()) {
            log(System.out, createMessage("INFO", className, message));
        }
    }

    public void info(String format, Object object) {
        if (isInfoEnabled()) {
            info(format, new Object[]{object});
        }
    }

    public void info(String format, Object ... objects) {
        if (isInfoEnabled()) {
            Objects.requireNonNull(format);
            log(System.out, createMessage("INFO", className, String.format(format, objects)));
        }
    }

    /**
     * Method to return if WARNING logging is enabled
     *
     * @return
     */
    public boolean isWarningEnabled() {
        return logLevel >= WARNING;
    }

    /**
     * Method to log a WARN message
     *
     * @param message
     */
    public void warning(String message) {
        if (isWarningEnabled()) {
            log(System.out, createMessage("WARNING", className, message));
        }
    }

    public void warning(String format, Object object) {
        if (isWarningEnabled()) {
            info(format, new Object[]{object});
        }
    }

    public void warning(String format, Object ... objects) {
        if (isWarningEnabled()) {
            Objects.requireNonNull(format);
            log(System.out, createMessage("WARNING", className, String.format(format, objects)));
        }
    }

    /**
     * Method to return if ERROR logging is enabled
     *
     * @return
     */
    public boolean isErrorEnabled() {
        return logLevel >= ERROR;
    }

    /**
     * Method to log an ERROR message
     *
     * @param message
     */
    public void error(String message) {
        if (isErrorEnabled()) {
            log(System.err, createMessage("ERROR", className, message));
        }
    }

    public void error(String format, Object object) {
        if (isErrorEnabled()) {
            info(format, new Object[]{object});
        }
    }

    public void error(String format, Object ... objects) {
        if (isErrorEnabled()) {
            Objects.requireNonNull(format);
            log(System.out, createMessage("ERROR", className, String.format(format, objects)));
        }
    }

    /**
     * Method to return if DEBUG logging is enabled
     *
     * @return
     */
    public boolean isDebugEnabled() {
        return logLevel >= DEBUG;
    }

    /**
     * Method to log a DEBUG message
     *
     * @param message
     */
    public void debug(String message) {
        if (isDebugEnabled()) {
            log(System.out, createMessage("DEBUG", className, message));
        }
    }

    public void debug(String format, Object object) {
        if (isDebugEnabled()) {
            info(format, new Object[]{object});
        }
    }

    public void debug(String format, Object ... objects) {
        if (isDebugEnabled()) {
            Objects.requireNonNull(format);
            log(System.out, createMessage("DEBUG", className, String.format(format, objects)));
        }
    }

    /**
     * Method to return if TRACE logging is enabled
     *
     * @return
     */
    public boolean isTraceEnabled() {
        return logLevel >= TRACE;
    }

    /**
     * Method to log a TRACE message
     *
     * @param message
     */
    public void trace(String message) {
        if (isTraceEnabled()) {
            log(System.out, createMessage("TRACE", className, message));
        }
    }

    public void trace(String format, Object object) {
        if (isTraceEnabled()) {
            info(format, new Object[]{object});
        }
    }

    public void trace(String format, Object ... objects) {
        if (isTraceEnabled()) {
            Objects.requireNonNull(format);
            log(System.out, createMessage("TRACE", className, String.format(format, objects)));
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