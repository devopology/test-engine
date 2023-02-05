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
import org.devopology.test.engine.support.descriptor.TestEngineTestSource;
import org.devopology.test.engine.support.util.AnsiColor;
import org.devopology.test.engine.support.util.Switch;
import org.junit.platform.engine.ConfigurationParameters;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.support.descriptor.EngineDescriptor;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;

import java.lang.reflect.Method;

/**
 * Class to implement a TestExecutionListener
 */
@SuppressWarnings("PMD.DUPLICATE")
public class TestEngineTestExecutionListener implements TestExecutionListener {

    private static final String INFO = "[" + AnsiColor.BLUE_BOLD.wrap("INFO") + "]";
    private static final String ABORTED = "[" + AnsiColor.YELLOW_BOLD_BRIGHT.wrap("ABORTED") + "]";
    private static final String FAILED = "[" + AnsiColor.RED_BOLD_BRIGHT.wrap("FAILED") + "]";
    private static final String PASSED = "[" + AnsiColor.GREEN_BOLD_BRIGHT.wrap("PASSED") + "]";

    private enum Mode { IDE, CONSOLE }

    private Mode mode = Mode.IDE;

    @Override
    public void testPlanExecutionStarted(TestPlan testPlan) {
        ConfigurationParameters configurationParameters = testPlan.getConfigurationParameters();
        for (String key : configurationParameters.keySet()) {
            if (key.startsWith("devopology.test.engine.output")) {
                configurationParameters.get(key).ifPresent(value -> {
                    if ("detailed".equalsIgnoreCase(value)) {
                        mode = Mode.CONSOLE;
                    }
                });
            }
        }
    }

    @SuppressWarnings("PMD.")
    @Override
    public void executionStarted(TestIdentifier testIdentifier) {
        if (mode != Mode.CONSOLE) {
            return;
        }

        testIdentifier.getSource().ifPresent(testSource -> {
            if (testSource instanceof TestEngineTestSource) {
                TestDescriptor testDescriptor = ((TestEngineTestSource) testSource).getTestDescriptor();
                final StringBuilder stringBuilder = new StringBuilder();

                Switch.switchType(
                        testDescriptor,
                        Switch.switchCase(EngineDescriptor.class, consumer -> {}),
                        Switch.switchCase(TestEngineClassTestDescriptor.class, consumer -> {}),
                        Switch.switchCase(TestEngineParameterTestDescriptor.class, consumer -> {
                            TestEngineParameterTestDescriptor testClassTestDescriptor = (TestEngineParameterTestDescriptor) testDescriptor;
                            Class<?> testClass = testClassTestDescriptor.getTestClass();
                            Object testParameter = testClassTestDescriptor.getTestParameter();
                            String testParameterDisplayName = TestEngineUtils.getDisplayName(testParameter);
                            stringBuilder
                                    .append(INFO)
                                    .append(" Test: ").append(testClass.getName())
                                    .append(" (").append(testParameterDisplayName).append(")");
                        }),
                        Switch.switchCase(TestEngineTestMethodTestDescriptor.class, consumer -> {
                            TestEngineTestMethodTestDescriptor testEngineTestMethodTestDescriptor = (TestEngineTestMethodTestDescriptor) testDescriptor;
                            Class<?> testClass = testEngineTestMethodTestDescriptor.getTestClass();
                            Object testParameter = testEngineTestMethodTestDescriptor.getTestParameter();
                            String testParameterDisplayName = TestEngineUtils.getDisplayName(testParameter);
                            Method testMethod = testEngineTestMethodTestDescriptor.getTestMethod();
                            stringBuilder
                                    .append(INFO)
                                    .append(" Method: ").append(testClass.getName())
                                    .append(" (").append(testParameterDisplayName).append(") ").append(testMethod.getName()).append("()");
                        })
                );

                if (stringBuilder.length() > 0) {
                    System.out.println(stringBuilder);
                }
            }
        });
    }

    @Override
    public void executionSkipped(TestIdentifier testIdentifier, String reason) {
        // TODO
    }

    @Override
    public void executionFinished(TestIdentifier testIdentifier, TestExecutionResult testExecutionResult) {
        if (mode != Mode.CONSOLE) {
            return;
        }

        testIdentifier.getSource().ifPresent(testSource -> {
            if (testSource instanceof TestEngineTestSource) {
                TestDescriptor testDescriptor = ((TestEngineTestSource) testSource).getTestDescriptor();
                final StringBuilder stringBuilder = new StringBuilder();

                Switch.switchType(
                        testDescriptor,
                        Switch.switchCase(EngineDescriptor.class, consumer -> {
                        }),
                        Switch.switchCase(TestEngineClassTestDescriptor.class, consumer -> {
                        }),
                        Switch.switchCase(TestEngineParameterTestDescriptor.class, consumer -> {
                            TestEngineParameterTestDescriptor testClassTestDescriptor = (TestEngineParameterTestDescriptor) testDescriptor;
                            Class<?> testClass = testClassTestDescriptor.getTestClass();
                            Object testParameter = testClassTestDescriptor.getTestParameter();
                            String testParameterDisplayName = TestEngineUtils.getDisplayName(testParameter);
                            stringBuilder
                                    .append(INFO)
                                    .append(" Test: ").append(testClass.getName())
                                    .append(" (").append(testParameterDisplayName).append(")");
                        }),
                        Switch.switchCase(TestEngineTestMethodTestDescriptor.class, consumer -> {
                            TestEngineTestMethodTestDescriptor testEngineTestMethodTestDescriptor = (TestEngineTestMethodTestDescriptor) testDescriptor;
                            Class<?> testClass = testEngineTestMethodTestDescriptor.getTestClass();
                            Object testParameter = testEngineTestMethodTestDescriptor.getTestParameter();
                            Method testMethod = testEngineTestMethodTestDescriptor.getTestMethod();
                            String testParameterDisplayName = TestEngineUtils.getDisplayName(testParameter);
                            stringBuilder
                                    .append(INFO)
                                    .append(" Method: ").append(testClass.getName())
                                    .append(" (").append(testParameterDisplayName).append(") ").append(testMethod.getName()).append("()");
                        }));

                if (stringBuilder.length() > 0) {
                    TestExecutionResult.Status status = testExecutionResult.getStatus();
                    switch (status) {
                        case ABORTED: {
                            stringBuilder.append(" ").append(ABORTED);
                            break;
                        }
                        case FAILED: {
                            stringBuilder.append(" ").append(FAILED);
                            break;
                        }
                        case SUCCESSFUL: {
                            stringBuilder.append(" ").append(PASSED);
                            break;
                        }
                        default: {
                            // DO NOTHING
                            break;
                        }
                    }

                    System.out.println(stringBuilder);
                }
            }
        });
    }

    @Override
    public void testPlanExecutionFinished(TestPlan testPlan) {
        // DO NOTHING
    }
}

