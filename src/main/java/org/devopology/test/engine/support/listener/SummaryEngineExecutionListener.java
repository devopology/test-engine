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

package org.devopology.test.engine.support.listener;

import org.devopology.test.engine.support.TestEngineInformation;
import org.devopology.test.engine.support.descriptor.TestClassTestTestDescriptor;
import org.devopology.test.engine.support.descriptor.TestMethodTestDescriptor;
import org.devopology.test.engine.support.descriptor.TestParameterTestDescriptor;
import org.devopology.test.engine.support.util.AnsiColor;
import org.devopology.test.engine.support.util.Counter;
import org.devopology.test.engine.support.util.Manager;
import org.devopology.test.engine.support.util.Padding;
import org.devopology.test.engine.support.util.Switch;
import org.junit.platform.engine.EngineExecutionListener;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.support.descriptor.EngineDescriptor;

import java.io.PrintStream;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class SummaryEngineExecutionListener implements EngineExecutionListener {

    private static final String INFO = "[" + AnsiColor.BLUE_BOLD.wrap("INFO") + "] ";

    private long startMilliseconds;
    private Set<String> parameterStringSet;
    private Set<String> classNameSet;
    private Set<String> methodNameSet;
    private Manager<Counter> counterManager;

    public SummaryEngineExecutionListener(PrintStream printStream) {
        startMilliseconds = System.currentTimeMillis();
        parameterStringSet = new HashSet<>();
        classNameSet = new HashSet<>();
        methodNameSet = new HashSet<>();

        counterManager = new Manager<>();
        counterManager.put("parameters.passed", new Counter());
        counterManager.put("parameters.skipped", new Counter());
        counterManager.put("parameters.failed", new Counter());
        counterManager.put("classes.passed", new Counter());
        counterManager.put("classes.skipped", new Counter());
        counterManager.put("classes.failed", new Counter());
        counterManager.put("methods.passed", new Counter());
        counterManager.put("methods.skipped", new Counter());
        counterManager.put("methods.failed", new Counter());

        String banner = "Devopology Test Engine " + TestEngineInformation.getVersion();

        StringBuilder stringBuilder = new StringBuilder();
        for (char c : banner.toCharArray()) {
            stringBuilder.append("-");
        }

        String separator = stringBuilder.toString();

        printStream.println(INFO + separator);
        printStream.println(INFO + banner);
        printStream.println(INFO + separator);
        printStream.println(INFO + "Scanning for tests...");
    }

    public boolean hasFailures() {
        return (counterManager.get("parameters.failed").get().getCount()
                + counterManager.get("classes.failed").get().getCount()
                + counterManager.get("methods.failed").get().getCount()) > 0;
    }

    public void executionSkipped(TestDescriptor testDescriptor, String reason) {
        Switch.switchType(
                testDescriptor,
                Switch.switchCase(EngineDescriptor.class, consumer -> executionSkipped((EngineDescriptor) testDescriptor, reason)),
                Switch.switchCase(TestClassTestTestDescriptor.class, consumer -> executionSkipped((TestClassTestTestDescriptor) testDescriptor, reason)),
                Switch.switchCase(TestParameterTestDescriptor.class, consumer -> executionSkipped((TestParameterTestDescriptor) testDescriptor, reason)),
                Switch.switchCase(TestMethodTestDescriptor.class, consumer -> executionSkipped((TestMethodTestDescriptor) testDescriptor, reason)));
    }

    public void executionSkipped(EngineDescriptor engineDescriptor, String reason) {
        // TODO
    }

    public void executionSkipped(TestClassTestTestDescriptor TestClassTestDescriptor, String reason) {
        // TODO
    }

    public void executionSkipped(TestParameterTestDescriptor TestParameterTestDescriptor, String reason) {
        // TODO
    }

    public void executionSkipped(TestMethodTestDescriptor TestMethodTestDescriptor, String reason) {
        // TODO
    }

    public void executionStarted(TestDescriptor testDescriptor) {
        Switch.switchType(
                testDescriptor,
                Switch.switchCase(EngineDescriptor.class, consumer -> executionStarted((EngineDescriptor) testDescriptor)),
                Switch.switchCase(TestClassTestTestDescriptor.class, consumer -> executionStarted((TestClassTestTestDescriptor) testDescriptor)),
                Switch.switchCase(TestParameterTestDescriptor.class, consumer -> executionStarted((TestParameterTestDescriptor) testDescriptor)),
                Switch.switchCase(TestMethodTestDescriptor.class, consumer -> executionStarted((TestMethodTestDescriptor) testDescriptor)));
    }

    public void executionStarted(EngineDescriptor engineDescriptor) {
        // DO NOTHING
    }

    public void executionStarted(TestClassTestTestDescriptor TestClassTestDescriptor) {
        // DO NOTHING
    }

    public void executionStarted(TestParameterTestDescriptor testParameterTestDescriptor) {
        Object object = testParameterTestDescriptor.getTestParameter();
        parameterStringSet.add(object.toString());

        Class<?> clazz = testParameterTestDescriptor.getTestClass();
        classNameSet.add(clazz.getName());
    }

    public void executionStarted(TestMethodTestDescriptor testMethodTestDescriptor) {
        Class<?> clazz = testMethodTestDescriptor.getTestClass();
        Method method = testMethodTestDescriptor.getTestMethod();
        methodNameSet.add(clazz.getName() + "/" + method.getName());
    }

    public void executionFinished(TestDescriptor testDescriptor, TestExecutionResult testExecutionResult) {
        Switch.switchType(
                testDescriptor,
                Switch.switchCase(EngineDescriptor.class, consumer -> executionFinished((EngineDescriptor) testDescriptor, testExecutionResult)),
                Switch.switchCase(TestClassTestTestDescriptor.class, consumer -> executionFinished((TestClassTestTestDescriptor) testDescriptor, testExecutionResult)),
                Switch.switchCase(TestParameterTestDescriptor.class, consumer -> executionFinished((TestParameterTestDescriptor) testDescriptor, testExecutionResult)),
                Switch.switchCase(TestMethodTestDescriptor.class, consumer -> executionFinished((TestMethodTestDescriptor) testDescriptor, testExecutionResult)));
    }

    public void executionFinished(EngineDescriptor engineDescriptor, TestExecutionResult testExecutionResult) {
        // DO NOTHING
    }

    public void executionFinished(TestClassTestTestDescriptor TestClassTestDescriptor, TestExecutionResult testExecutionResult) {
        // DO NOTHING
    }

    public void executionFinished(TestParameterTestDescriptor TestParameterTestDescriptor, TestExecutionResult testExecutionResult) {
        TestExecutionResult.Status status = testExecutionResult.getStatus();
        switch (status) {
            case ABORTED: {
                counterManager.get("classes.skipped").ifPresent(Counter::increment);
                break;
            }
            case FAILED: {
                counterManager.get("classes.failed").ifPresent(Counter::increment);
                break;
            }
            case SUCCESSFUL: {
                counterManager.get("classes.passed").ifPresent(Counter::increment);
                break;
            }
            default: {
                throw new RuntimeException("processFinished not implemented");
            }
        }
    }

    public void executionFinished(TestMethodTestDescriptor TestMethodTestDescriptor, TestExecutionResult testExecutionResult) {
        TestExecutionResult.Status status = testExecutionResult.getStatus();
        switch (status) {
            case ABORTED: {
                counterManager.get("methods.skipped").ifPresent(Counter::increment);
                break;
            }
            case FAILED: {
                counterManager.get("methods.failed").ifPresent(Counter::increment);
                break;
            }
            case SUCCESSFUL: {
                counterManager.get("methods.passed").ifPresent(Counter::increment);
                break;
            }
            default: {
                throw new RuntimeException("processFinished not implemented");
            }
        }
    }

    public void printSummary(PrintStream printStream) {
        long finishMilliseconds = System.currentTimeMillis();

        String banner = "Devopology Test Engine " + TestEngineInformation.getVersion() + " Summary";

        StringBuilder stringBuilder = new StringBuilder();
        for (char c : banner.toCharArray()) {
            stringBuilder.append("-");
        }

        String separator = stringBuilder.toString();

        printStream.println(INFO + separator);
        printStream.println("[" + AnsiColor.BLUE_BOLD.wrap("INFO") + "] " + banner);
        printStream.println(INFO + separator);
        printStream.println(INFO);

        int pad = Padding.calculatePadding(parameterStringSet.size(), classNameSet.size(), methodNameSet.size());

        printStream.println(INFO + "Unique parameters   : " + String.format("%" + pad + "s", parameterStringSet.size()));
        printStream.println(INFO + "Unique test classes : " + String.format("%" + pad + "s", classNameSet.size()));
        printStream.println(INFO + "Unique test methods : " + String.format("%" + pad + "s", methodNameSet.size()));
        printStream.println(INFO);

        String[] prefixes = new String[] { "methods" };

        Padding padding = new Padding();

        for (String prefix : prefixes) {
            padding.apply(counterManager.get(prefix + ".passed").get().getCount());
        }

        int passPadding = padding.getPadding();

        padding.reset();

        for (String prefix : prefixes) {
            padding.apply(counterManager.get(prefix + ".failed").get().getCount());
        }

        int failedPadding = padding.getPadding();

        /*
        padding.reset();

        for (String prefix : prefixes) {
            padding.apply(counterManager.get(prefix + ".skipped").get().getCount());
        }

        int skippedPadding = padding.getPadding();
        */

        for (String prefix : prefixes) {
            long passedCount = counterManager.get(prefix + ".passed").get().getCount();
            long failedCount = counterManager.get(prefix + ".failed").get().getCount();
            //long skippedCount = counterManager.get(prefix + ".skipped").get().getCount();

            printStream.println(INFO
                    + "Tests run: "
                    + (passedCount + failedCount)
                    + ", "
                    + AnsiColor.GREEN_BOLD_BRIGHT.wrap("PASSED")
                    + ": "
                    + String.format("%" + passPadding + "d", passedCount)
                    + ", "
                    + AnsiColor.RED_BOLD_BRIGHT.wrap("FAILED")
                    + ": "
                    + String.format("%" + failedPadding + "d", failedCount));
                    //+ ", "
                    //+ AnsiColor.YELLOW_BOLD_BRIGHT.wrap("SKIPPED")
                    //+ ": "
                    //+ String.format("%" + skippedPadding + "d", skippedCount));
        }

        printStream.println(INFO);
        
        if (!hasFailures()) {
            printStream.println(INFO + separator);
            printStream.println(INFO + AnsiColor.GREEN_BOLD_BRIGHT.wrap("PASSED"));

        } else {
            printStream.println(INFO + separator);
            printStream.println(INFO + AnsiColor.RED_BOLD_BRIGHT.wrap("FAILED"));
        }

        printStream.println(INFO + separator);
        printStream.println(INFO + "Total time: " + toHumanReadable(finishMilliseconds - startMilliseconds, true));
        printStream.println(INFO + "Finished at: " + finishedAt());
        printStream.println(INFO + separator);
    }

    private static String capitalize(String string) {
        char[] chars = string.toCharArray();
        chars[0] = String.valueOf(chars[0]).toUpperCase().toCharArray()[0];
        return new String(chars);
    }

    private static String toHumanReadable(long duration, boolean useShortFormat) {
        if (duration < 0) {
            duration = -duration;
        }

        long hours = TimeUnit.MILLISECONDS.toHours(duration);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(duration) - (hours * 60);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(duration) - ((hours * 60 * 60) + (minutes * 60));
        long milliseconds = duration - ((hours * 60 * 60 * 1000) + (minutes * 60 * 1000) + (seconds * 1000));

        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append(hours);

        if (useShortFormat) {
            stringBuilder.append(" h");
        } else {
            stringBuilder.append(" hour");

            if (hours != 1) {
                stringBuilder.append("s");
            }
        }

        stringBuilder.append(", ");
        stringBuilder.append(minutes);

        if (useShortFormat) {
            stringBuilder.append(" m");
        } else {
            stringBuilder.append(" minute");
            if (minutes != 1) {
                stringBuilder.append("s");
            }
        }

        stringBuilder.append(", ");
        stringBuilder.append(seconds);

        if (useShortFormat) {
            stringBuilder.append(" s");
        } else {
            stringBuilder.append(" second");
            if (seconds != 1) {
                stringBuilder.append("s");
            }
        }

        stringBuilder.append(", ");
        stringBuilder.append(milliseconds);
        stringBuilder.append(" ms");

        String result = stringBuilder.toString();

        if (result.startsWith("0 h, ")) {
            result = result.substring("0 h, ".length());
        }

        if (result.startsWith("0 hours, ")) {
            result = result.substring("0 hours, ".length());
        }

        if (result.startsWith("0 m, ")) {
            result = result.substring("0 m, ".length());
        }

        if (result.startsWith("0 minutes, ")) {
            result = result.substring("0 minutes, ".length());
        }

        if (result.startsWith("0 s, ")) {
            result = result.substring("0 s, ".length());
        }

        if (result.startsWith("0 seconds, ")) {
            result = result.substring("0 seconds, ".length());
        }

        return result;
    }

    private static String finishedAt() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        return simpleDateFormat.format(new Date());
    }
}
