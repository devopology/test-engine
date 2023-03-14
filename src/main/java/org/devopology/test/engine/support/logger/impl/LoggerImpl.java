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

package org.devopology.test.engine.support.logger.impl;

import org.devopology.test.engine.support.TestEngineConfiguration;
import org.devopology.test.engine.support.logger.Logger;

import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * Class to implement logger
 */
@SuppressWarnings("PMD.GodClass")
public class LoggerImpl implements Logger {

    private static final SimpleDateFormat SIMPLE_DATE_FORMAT =
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault());

    private static final int OFF = 0;
    private static final int ERROR = 100;
    private static final int WARNING = 200;
    private static final int INFO = 300;
    private static final int DEBUG = 400;
    private static final int TRACE = 500;
    private static final int ALL = Integer.MAX_VALUE;

    private static final Map<String, Integer> LOG_LEVEL_MAP;

    static {
        LOG_LEVEL_MAP = new HashMap<>();
        LOG_LEVEL_MAP.put("OFF", OFF);
        LOG_LEVEL_MAP.put("ERROR", ERROR);
        LOG_LEVEL_MAP.put("WARNING", WARNING);
        LOG_LEVEL_MAP.put("INFO", INFO);
        LOG_LEVEL_MAP.put("DEBUG", DEBUG);
        LOG_LEVEL_MAP.put("TRACE", TRACE);
        LOG_LEVEL_MAP.put("ALL", ALL);
    }

    private final String name;
    private int logLevel = INFO;

    /**
     * Constructor
     *
     * @param name
     */
    public LoggerImpl(String name) {
        if ((name == null) || name.trim().isEmpty()) {
            throw new IllegalArgumentException("name is required");
        }
        
        this.name = name.trim();
        
        String logLevelValue =
                TestEngineConfiguration.getValue(
                "devopology.test.engine.log.level",
                "DEVOPOLOGY_TEST_ENGINE_LOG_LEVEL");
        
        if ((logLevelValue != null) && !logLevelValue.trim().isEmpty()) {
            logLevel = LOG_LEVEL_MAP.getOrDefault(logLevelValue.trim().toUpperCase(), INFO);
        } else {
            logLevel = INFO;
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
            log(System.out, createMessage("INFO", name, message));
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
            log(System.out, createMessage("INFO", name, String.format(format, objects)));
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
            log(System.out, createMessage("WARNING", name, message));
        }
    }

    public void warning(String format, Object object) {
        if (isWarningEnabled()) {
            warning(format, new Object[]{object});
        }
    }

    public void warning(String format, Object ... objects) {
        if (isWarningEnabled()) {
            Objects.requireNonNull(format);
            log(System.out, createMessage("WARNING", name, String.format(format, objects)));
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
            log(System.err, createMessage("ERROR", name, message));
        }
    }

    public void error(String format, Object object) {
        if (isErrorEnabled()) {
            error(format, new Object[]{object});
        }
    }

    public void error(String format, Object ... objects) {
        if (isErrorEnabled()) {
            Objects.requireNonNull(format);
            log(System.out, createMessage("ERROR", name, String.format(format, objects)));
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
            log(System.out, createMessage("DEBUG", name, message));
        }
    }

    public void debug(String format, Object object) {
        if (isDebugEnabled()) {
            debug(format, new Object[]{object});
        }
    }

    public void debug(String format, Object ... objects) {
        if (isDebugEnabled()) {
            Objects.requireNonNull(format);
            log(System.out, createMessage("DEBUG", name, String.format(format, objects)));
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
            log(System.out, createMessage("TRACE", name, message));
        }
    }

    public void trace(String format, Object object) {
        if (isTraceEnabled()) {
            trace(format, new Object[]{object});
        }
    }

    public void trace(String format, Object ... objects) {
        if (isTraceEnabled()) {
            Objects.requireNonNull(format);
            log(System.out, createMessage("TRACE", name, String.format(format, objects)));
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
     * @param name
     * @param message
     * @return
     */
    private static String createMessage(String level, String name, String message) {
        String dateTime;

        synchronized (SIMPLE_DATE_FORMAT) {
            dateTime = SIMPLE_DATE_FORMAT.format(new Date());
        }

        int index = dateTime.indexOf(" ");
        String date = dateTime.substring(0, index);
        String time = dateTime.substring(index + 1);

        return String.format(
                    "%s | %s | %s | %s | %s",
                    date,
                    time,
                    Thread.currentThread().getName(),
                    level,
                    message);
    }
}