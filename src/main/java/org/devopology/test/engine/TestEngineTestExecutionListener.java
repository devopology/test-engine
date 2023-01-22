package org.devopology.test.engine;

import org.devopology.test.engine.internal.TestEngineUtils;
import org.devopology.test.engine.internal.descriptor.TestClassTestTestDescriptor;
import org.devopology.test.engine.internal.descriptor.TestEngineTestSource;
import org.devopology.test.engine.internal.descriptor.TestMethodTestDescriptor;
import org.devopology.test.engine.internal.descriptor.TestParameterTestDescriptor;
import org.devopology.test.engine.internal.logger.Logger;
import org.devopology.test.engine.internal.logger.LoggerFactory;
import org.devopology.test.engine.internal.util.AnsiColor;
import org.devopology.test.engine.internal.util.Switch;
import org.junit.platform.engine.ConfigurationParameters;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.support.descriptor.EngineDescriptor;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

public class TestEngineTestExecutionListener implements TestExecutionListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestEngineTestExecutionListener.class);

    static {
        AnsiColor.force();
    }

    private static final String INFO = "[" + AnsiColor.BLUE_BOLD.wrap("INFO") + "]";
    private static final String ABORTED = "[" + AnsiColor.YELLOW_BOLD_BRIGHT.wrap("ABORTED") + "]";
    private static final String FAILED = "[" + AnsiColor.RED_BOLD_BRIGHT.wrap("FAILED") + "]";
    private static final String PASSED = "[" + AnsiColor.GREEN_BOLD_BRIGHT.wrap("PASSED") + "]";

    private enum Mode { IDE, CONSOLE }

    private Mode mode = Mode.IDE;

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
                        Switch.switchCase(TestClassTestTestDescriptor.class, consumer -> {}),
                        Switch.switchCase(TestParameterTestDescriptor.class, consumer -> {
                            TestParameterTestDescriptor testClassTestDescriptor = (TestParameterTestDescriptor) testDescriptor;
                            Class<?> testClass = testClassTestDescriptor.getTestClass();
                            Object testParameter = testClassTestDescriptor.getTestParameter();
                            String testParameterDisplayName = TestEngineUtils.getDisplayName(testParameter);
                            stringBuilder
                                    .append(INFO)
                                    .append(" Test: ").append(testClass.getName())
                                    .append(" (").append(testParameterDisplayName).append(")");
                        }),
                        Switch.switchCase(TestMethodTestDescriptor.class, consumer -> {
                            TestMethodTestDescriptor testMethodTestDescriptor = (TestMethodTestDescriptor) testDescriptor;
                            Class<?> testClass = testMethodTestDescriptor.getTestClass();
                            Object testParameter = testMethodTestDescriptor.getTestParameter();
                            String testParameterDisplayName = TestEngineUtils.getDisplayName(testParameter);
                            Method testMethod = testMethodTestDescriptor.getTestMethod();
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

    public void executionSkipped(TestIdentifier testIdentifier, String reason) {
        // TODO
    }

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
                        Switch.switchCase(TestClassTestTestDescriptor.class, consumer -> {
                        }),
                        Switch.switchCase(TestParameterTestDescriptor.class, consumer -> {
                            TestParameterTestDescriptor testClassTestDescriptor = (TestParameterTestDescriptor) testDescriptor;
                            Class<?> testClass = testClassTestDescriptor.getTestClass();
                            Object testParameter = testClassTestDescriptor.getTestParameter();
                            String testParameterDisplayName = TestEngineUtils.getDisplayName(testParameter);
                            stringBuilder
                                    .append(INFO)
                                    .append(" Test: ").append(testClass.getName())
                                    .append(" (").append(testParameterDisplayName).append(")");
                        }),
                        Switch.switchCase(TestMethodTestDescriptor.class, consumer -> {
                            TestMethodTestDescriptor testMethodTestDescriptor = (TestMethodTestDescriptor) testDescriptor;
                            Class<?> testClass = testMethodTestDescriptor.getTestClass();
                            Object testParameter = testMethodTestDescriptor.getTestParameter();
                            Method testMethod = testMethodTestDescriptor.getTestMethod();
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
                    }

                    if (stringBuilder.length() > 0) {
                        System.out.println(stringBuilder);
                    }
                }
            }
        });
    }

    public void testPlanExecutionFinished(TestPlan testPlan) {
        // DO NOTHING
    }
}

