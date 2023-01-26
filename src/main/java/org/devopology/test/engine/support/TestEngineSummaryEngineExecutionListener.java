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

package org.devopology.test.engine.support;

import org.devopology.test.engine.support.descriptor.TestEngineClassTestDescriptor;
import org.devopology.test.engine.support.descriptor.TestEngineParameterTestDescriptor;
import org.devopology.test.engine.support.descriptor.TestEngineTestMethodTestDescriptor;
import org.devopology.test.engine.support.util.AnsiColor;
import org.devopology.test.engine.support.util.Switch;
import org.junit.platform.engine.EngineExecutionListener;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.reporting.ReportEntry;
import org.junit.platform.engine.support.descriptor.EngineDescriptor;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;

import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public class TestEngineSummaryEngineExecutionListener implements EngineExecutionListener {

    private static final String INFO = "[" + AnsiColor.BLUE_BOLD.wrap("INFO") + "] ";
    private static final String TEST = "[" + AnsiColor.WHITE_BOLD_BRIGHT.wrap("TEST") + "]";
    private static final String ABORT = "[" + AnsiColor.YELLOW_BOLD_BRIGHT.wrap("ABORT") + "]";
    private static final String FAIL = "[" + AnsiColor.RED_BOLD_BRIGHT.wrap("FAIL") + "]";
    private static final String PASS = "[" + AnsiColor.GREEN_BOLD_BRIGHT.wrap("PASS") + "]";

    private TestPlan testPlan;
    private PrintWriter printWriter;
    private SummaryGeneratingListener summaryGeneratingListener;
    private boolean detailedOutput = true;

    public TestEngineSummaryEngineExecutionListener(TestPlan testPlan, PrintStream printWriter) {
        this.printWriter = new PrintWriter(new OutputStreamWriter(printWriter, StandardCharsets.UTF_8));
        this.testPlan = testPlan;
        this.summaryGeneratingListener = new SummaryGeneratingListener();
        this.summaryGeneratingListener.testPlanExecutionStarted(testPlan);

        Optional<String> optionalDetailOutput =testPlan.getConfigurationParameters().get("devopology.test.engine.output");
        if (optionalDetailOutput.isPresent()) {
            detailedOutput = "detailed".equalsIgnoreCase(optionalDetailOutput.get());
        }
    }

    public void dynamicTestRegistered(TestDescriptor testDescriptor) {
        summaryGeneratingListener.dynamicTestRegistered(TestIdentifier.from(testDescriptor));
    }

    public void executionSkipped(TestDescriptor testDescriptor, String reason) {
        summaryGeneratingListener.executionSkipped(TestIdentifier.from(testDescriptor), reason);
    }

    public void executionStarted(TestDescriptor testDescriptor) {
        summaryGeneratingListener.executionStarted(TestIdentifier.from(testDescriptor));

        final StringBuilder stringBuilder = new StringBuilder();

        Switch.switchType(
                testDescriptor,
                Switch.switchCase(EngineDescriptor.class, consumer -> {}),
                Switch.switchCase(TestEngineClassTestDescriptor.class, consumer -> {}),
                Switch.switchCase(TestEngineParameterTestDescriptor.class, consumer -> {
                    TestEngineParameterTestDescriptor testClassTestDescriptor = (TestEngineParameterTestDescriptor) testDescriptor;
                    Class<?> testClass = testClassTestDescriptor.getTestClass();
                    Object testParameter = testClassTestDescriptor.getTestParameter();
                    stringBuilder
                            .append(INFO)
                            .append("Test: ").append(testClass.getName())
                            .append(" (").append(testParameter).append(")")
                            .append(" ").append(TEST);
                }),
                Switch.switchCase(TestEngineTestMethodTestDescriptor.class, consumer -> {
                    TestEngineTestMethodTestDescriptor testEngineTestMethodTestDescriptor = (TestEngineTestMethodTestDescriptor) testDescriptor;
                    Class<?> testClass = testEngineTestMethodTestDescriptor.getTestClass();
                    Object testParameter = testEngineTestMethodTestDescriptor.getTestParameter();
                    Method testMethod = testEngineTestMethodTestDescriptor.getTestMethod();
                    stringBuilder
                            .append(INFO)
                            .append("Method: ").append(testClass.getName())
                            .append(" (").append(testParameter).append(") ").append(testMethod.getName()).append("()")
                            .append(" ").append(TEST);
                })
        );

        if (detailedOutput) {
            if (stringBuilder.length() > 0) {
                printWriter.println(stringBuilder);
                printWriter.flush();
            }
        }
    }

    public void executionFinished(TestDescriptor testDescriptor, TestExecutionResult testExecutionResult) {
        summaryGeneratingListener.executionFinished(TestIdentifier.from(testDescriptor), testExecutionResult);

        final StringBuilder stringBuilder = new StringBuilder();

        Switch.switchType(
                testDescriptor,
                Switch.switchCase(EngineDescriptor.class, consumer -> {}),
                Switch.switchCase(TestEngineClassTestDescriptor.class, consumer -> {}),
                Switch.switchCase(TestEngineParameterTestDescriptor.class, consumer -> {
                    TestEngineParameterTestDescriptor testClassTestDescriptor = (TestEngineParameterTestDescriptor) testDescriptor;
                    Class<?> testClass = testClassTestDescriptor.getTestClass();
                    Object testParameter = testClassTestDescriptor.getTestParameter();
                    stringBuilder
                            .append(INFO)
                            .append("Test: ").append(testClass.getName())
                            .append(" (").append(testParameter).append(")");
                }),
                Switch.switchCase(TestEngineTestMethodTestDescriptor.class, consumer -> {
                    TestEngineTestMethodTestDescriptor testEngineTestMethodTestDescriptor = (TestEngineTestMethodTestDescriptor) testDescriptor;
                    Class<?> testClass = testEngineTestMethodTestDescriptor.getTestClass();
                    Object testParameter = testEngineTestMethodTestDescriptor.getTestParameter();
                    Method testMethod = testEngineTestMethodTestDescriptor.getTestMethod();
                    stringBuilder
                            .append(INFO)
                            .append("Method: ").append(testClass.getName())
                            .append(" (").append(testParameter).append(") ").append(testMethod.getName()).append("()");
                }));

        if (stringBuilder.length() > 0) {
            TestExecutionResult.Status status = testExecutionResult.getStatus();
            switch (status) {
                case ABORTED: {
                    stringBuilder.append(" ").append(ABORT);
                    break;
                }
                case FAILED: {
                    stringBuilder.append(" ").append(FAIL);
                    break;
                }
                case SUCCESSFUL: {
                    stringBuilder.append(" ").append(PASS);
                    break;
                }
            }

            if (detailedOutput) {
                if (stringBuilder.length() > 0) {
                    printWriter.println(stringBuilder);
                    printWriter.flush();
                }
            }
        }
    }

    public void reportingEntryPublished(TestDescriptor testDescriptor, ReportEntry entry) {
        summaryGeneratingListener.reportingEntryPublished(TestIdentifier.from(testDescriptor), entry);
    }

    public TestExecutionSummary getSummary() {
        summaryGeneratingListener.testPlanExecutionFinished(testPlan);
        return summaryGeneratingListener.getSummary();
    }
}
