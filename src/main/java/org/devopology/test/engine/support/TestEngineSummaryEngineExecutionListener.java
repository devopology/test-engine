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

import org.devopology.test.engine.TestEngine;
import org.devopology.test.engine.support.descriptor.TestEngineClassTestDescriptor;
import org.devopology.test.engine.support.descriptor.TestEngineParameterTestDescriptor;
import org.devopology.test.engine.support.descriptor.TestEngineTestMethodTestDescriptor;
import org.devopology.test.engine.support.logger.Logger;
import org.devopology.test.engine.support.logger.LoggerFactory;
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

    private static final Logger LOGGER = LoggerFactory.getLogger(TestEngine.class);

    private static final String TEST = "[TEST]";
    private static final String ABORT = "[ABORT]";
    private static final String FAIL = "[FAIL]";
    private static final String PASS = "[PASS]";

    private final TestPlan testPlan;
    private final PrintWriter printWriter;
    private final SummaryGeneratingListener summaryGeneratingListener;
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

    @Override
    public void dynamicTestRegistered(TestDescriptor testDescriptor) {
        summaryGeneratingListener.dynamicTestRegistered(TestIdentifier.from(testDescriptor));
    }

    @Override
    public void executionSkipped(TestDescriptor testDescriptor, String reason) {
        summaryGeneratingListener.executionSkipped(TestIdentifier.from(testDescriptor), reason);
    }

    @Override
    public void executionStarted(TestDescriptor testDescriptor) {
        summaryGeneratingListener.executionStarted(TestIdentifier.from(testDescriptor));

        final StringBuilder stringBuilder = new StringBuilder();

        Switch.switchType(
                testDescriptor,
                Switch.switchCase(EngineDescriptor.class, consumer -> {}),
                Switch.switchCase(TestEngineClassTestDescriptor.class, consumer -> {}),
                Switch.switchCase(TestEngineParameterTestDescriptor.class, consumer -> {
                    TestEngineParameterTestDescriptor testEngineParameterTestDescriptor = (TestEngineParameterTestDescriptor) testDescriptor;
                    Class<?> testClass = testEngineParameterTestDescriptor.getTestClass();
                    Object parameter = testEngineParameterTestDescriptor.getTestParameter();
                    String parameterDisplayName = TestEngineUtils.getDisplayName(parameter);
                    stringBuilder
                            .append("[")
                            .append(parameterDisplayName)
                            .append("] - ")
                            .append(testClass.getName())
                            .append(" ")
                            .append(TEST);
                }),
                Switch.switchCase(TestEngineTestMethodTestDescriptor.class, consumer -> {
                    TestEngineTestMethodTestDescriptor testEngineTestMethodTestDescriptor = (TestEngineTestMethodTestDescriptor) testDescriptor;
                    Class<?> testClass = testEngineTestMethodTestDescriptor.getTestClass();
                    Method testMethod = testEngineTestMethodTestDescriptor.getTestMethod();
                    Object parameter = testEngineTestMethodTestDescriptor.getTestParameter();
                    String parameterDisplayName = TestEngineUtils.getDisplayName(parameter);
                    stringBuilder
                            .append("[")
                            .append(parameterDisplayName)
                            .append("] - ")
                            .append(testClass.getName())
                            .append(" ")
                            .append(testMethod.getName())
                            .append("() ")
                            .append(TEST);
                })
        );

        if (detailedOutput && (stringBuilder.length() > 0)) {
            LOGGER.infoRaw(stringBuilder.toString());
        }
    }

    @Override
    public void executionFinished(TestDescriptor testDescriptor, TestExecutionResult testExecutionResult) {
        summaryGeneratingListener.executionFinished(TestIdentifier.from(testDescriptor), testExecutionResult);

        final StringBuilder stringBuilder = new StringBuilder();

        Switch.switchType(
                testDescriptor,
                Switch.switchCase(EngineDescriptor.class, consumer -> {}),
                Switch.switchCase(TestEngineClassTestDescriptor.class, consumer -> {}),
                Switch.switchCase(TestEngineParameterTestDescriptor.class, consumer -> {
                    TestEngineParameterTestDescriptor testengineParameterTestDescriptor = (TestEngineParameterTestDescriptor) testDescriptor;
                    Class<?> testClass = testengineParameterTestDescriptor.getTestClass();
                    Object parameter = testengineParameterTestDescriptor.getTestParameter();
                    String parameterDisplayName = TestEngineUtils.getDisplayName(parameter);
                    stringBuilder
                            .append("[")
                            .append(parameterDisplayName)
                            .append("] - ")
                            .append(testClass.getName());
                }),
                Switch.switchCase(TestEngineTestMethodTestDescriptor.class, consumer -> {
                    TestEngineTestMethodTestDescriptor testEngineTestMethodTestDescriptor = (TestEngineTestMethodTestDescriptor) testDescriptor;
                    Class<?> testClass = testEngineTestMethodTestDescriptor.getTestClass();
                    Method testMethod = testEngineTestMethodTestDescriptor.getTestMethod();
                    Object parameter = testEngineTestMethodTestDescriptor.getTestParameter();
                    String parameterDisplayName = TestEngineUtils.getDisplayName(parameter);
                    stringBuilder
                            .append("[")
                            .append(parameterDisplayName)
                            .append("] - ")
                            .append(testClass.getName())
                            .append(" ")
                            .append(testMethod.getName())
                            .append("()");
                }));

        if (stringBuilder.length() > 0) {
            stringBuilder.append(" ");
            TestExecutionResult.Status status = testExecutionResult.getStatus();
            switch (status) {
                case ABORTED: {
                    stringBuilder.append(ABORT);
                    break;
                }
                case FAILED: {
                    stringBuilder.append(FAIL);
                    break;
                }
                case SUCCESSFUL: {
                    stringBuilder.append(PASS);
                    break;
                }
                default: {
                    // DO NOTHING
                    break;
                }
            }

            if (detailedOutput && (stringBuilder.length() > 0)) {
                LOGGER.infoRaw(stringBuilder.toString());
            }
        }
    }

    @Override
    public void reportingEntryPublished(TestDescriptor testDescriptor, ReportEntry entry) {
        summaryGeneratingListener.reportingEntryPublished(TestIdentifier.from(testDescriptor), entry);
    }

    public TestExecutionSummary getSummary() {
        summaryGeneratingListener.testPlanExecutionFinished(testPlan);
        return summaryGeneratingListener.getSummary();
    }
}
