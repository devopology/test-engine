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

package org.devopology.test.engine.internal.listener;

import org.devopology.test.engine.internal.descriptor.TestClassTestTestDescriptor;
import org.devopology.test.engine.internal.descriptor.TestMethodTestDescriptor;
import org.devopology.test.engine.internal.descriptor.TestParameterTestDescriptor;
import org.devopology.test.engine.internal.util.AnsiColor;
import org.devopology.test.engine.internal.util.Switch;
import org.junit.platform.engine.EngineExecutionListener;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.support.descriptor.EngineDescriptor;

import java.io.PrintStream;
import java.lang.reflect.Method;

public class PrintStreamEngineExecutionListener implements EngineExecutionListener {

    private static final String INFO = "[" + AnsiColor.BLUE_BOLD.wrap("INFO") + "]";
    private static final String ABORTED = "[" + AnsiColor.YELLOW_BOLD_BRIGHT.wrap("ABORTED") + "]";
    private static final String FAILED = "[" + AnsiColor.RED_BOLD_BRIGHT.wrap("FAILED") + "]";
    private static final String PASSED = "[" + AnsiColor.GREEN_BOLD_BRIGHT.wrap("PASSED") + "]";

    private EngineExecutionListener engineExecutionListener;
    private PrintStream printStream;

    public PrintStreamEngineExecutionListener(EngineExecutionListener engineExecutionListener, PrintStream printStream) {
        this.engineExecutionListener = engineExecutionListener;
        this.printStream = printStream;
    }

    public void executionStarted(TestDescriptor testDescriptor) {
        engineExecutionListener.executionStarted(testDescriptor);

        final StringBuilder stringBuilder = new StringBuilder();

        Switch.switchType(
                testDescriptor,
                Switch.switchCase(EngineDescriptor.class, consumer -> {}),
                Switch.switchCase(TestClassTestTestDescriptor.class, consumer -> {}),
                Switch.switchCase(TestParameterTestDescriptor.class, consumer -> {
                    TestParameterTestDescriptor testClassTestDescriptor = (TestParameterTestDescriptor) testDescriptor;
                    Class<?> testClass = testClassTestDescriptor.getTestClass();
                    Object testParameter = testClassTestDescriptor.getTestParameter();
                    stringBuilder
                            .append(INFO)
                            .append(" Test: ").append(testClass.getName())
                            .append(" (").append(testParameter).append(")");
                }),
                Switch.switchCase(TestMethodTestDescriptor.class, consumer -> {
                    TestMethodTestDescriptor testMethodTestDescriptor = (TestMethodTestDescriptor) testDescriptor;
                    Class<?> testClass = testMethodTestDescriptor.getTestClass();
                    Object testParameter = testMethodTestDescriptor.getTestParameter();
                    Method testMethod = testMethodTestDescriptor.getTestMethod();
                    stringBuilder
                            .append(INFO)
                            .append(" Method: ").append(testClass.getName())
                            .append(" (").append(testParameter).append(") ").append(testMethod.getName()).append("()");
                })
        );

        if (stringBuilder.length() > 0) {
            printStream.println(stringBuilder);
        }
    }

    public void executionSkipped(TestDescriptor testDescriptor, String reason) {
        engineExecutionListener.executionSkipped(testDescriptor, reason);
    }

    public void executionFinished(TestDescriptor testDescriptor, TestExecutionResult testExecutionResult) {
        final StringBuilder stringBuilder = new StringBuilder();

        Switch.switchType(
                testDescriptor,
                Switch.switchCase(EngineDescriptor.class, consumer -> {}),
                Switch.switchCase(TestClassTestTestDescriptor.class, consumer -> {}),
                Switch.switchCase(TestParameterTestDescriptor.class, consumer -> {
                    TestParameterTestDescriptor testClassTestDescriptor = (TestParameterTestDescriptor) testDescriptor;
                    Class<?> testClass = testClassTestDescriptor.getTestClass();
                    Object testParameter = testClassTestDescriptor.getTestParameter();
                    stringBuilder
                            .append(INFO)
                            .append(" Test: ").append(testClass.getName())
                            .append(" (").append(testParameter).append(")");
                }),
                Switch.switchCase(TestMethodTestDescriptor.class, consumer -> {
                    TestMethodTestDescriptor testMethodTestDescriptor = (TestMethodTestDescriptor) testDescriptor;
                    Class<?> testClass = testMethodTestDescriptor.getTestClass();
                    Object testParameter = testMethodTestDescriptor.getTestParameter();
                    Method testMethod = testMethodTestDescriptor.getTestMethod();
                    stringBuilder
                            .append(INFO)
                            .append(" Method: ").append(testClass.getName())
                            .append(" (").append(testParameter).append(") ").append(testMethod.getName()).append("()");
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
            }

            if (stringBuilder.length() > 0) {
                printStream.println(stringBuilder);
            }
        }

        engineExecutionListener.executionFinished(testDescriptor, testExecutionResult);
    }
}
