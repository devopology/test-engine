package org.devopology.test.engine;

import org.devopology.test.engine.internal.descriptor.TestClassTestTestDescriptor;
import org.devopology.test.engine.internal.descriptor.TestEngineTestSource;
import org.devopology.test.engine.internal.descriptor.TestMethodTestDescriptor;
import org.devopology.test.engine.internal.descriptor.TestParameterTestDescriptor;
import org.devopology.test.engine.internal.logger.Logger;
import org.devopology.test.engine.internal.logger.LoggerFactory;
import org.devopology.test.engine.internal.util.AnsiColor;
import org.devopology.test.engine.internal.util.Switch;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.support.descriptor.EngineDescriptor;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;

import java.lang.reflect.Method;
import java.util.Set;

public class TestEngineTestExecutionListener implements TestExecutionListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestEngineTestExecutionListener.class);

    static {
        AnsiColor.force();
    }

    private static final String INFO = "[" + AnsiColor.BLUE_BOLD.wrap("INFO") + "]";
    private static final String ABORTED = "[" + AnsiColor.YELLOW_BOLD_BRIGHT.wrap("ABORTED") + "]";
    private static final String FAILED = "[" + AnsiColor.RED_BOLD_BRIGHT.wrap("FAILED") + "]";
    private static final String PASSED = "[" + AnsiColor.GREEN_BOLD_BRIGHT.wrap("PASSED") + "]";

    public void testPlanExecutionStarted(TestPlan testPlan) {
        Set<TestIdentifier> testIdentifierSet = testPlan.getRoots();
        for (TestIdentifier testIdentifier : testIdentifierSet) {
            testIdentifier.getSource().ifPresent(testSource -> LOGGER.info("testPlanExecutionStarted()"));
        }
    }

    public void executionStarted(TestIdentifier testIdentifier) {
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
                    System.out.println(stringBuilder);
                }
            }
        });
    }

    public void executionSkipped(TestIdentifier testIdentifier, String reason) {
        //LOGGER.info("testIdentifier display name [%s]", testIdentifier.getDisplayName());
        testIdentifier.getSource().ifPresent(testSource -> {
            //LOGGER.info("testSource class [%s]", testSource.getClass().getName());
            if (testSource instanceof TestEngineTestSource) {
                TestDescriptor testDescriptor = ((TestEngineTestSource) testSource).getTestDescriptor();
                LOGGER.info("testDescriptor display name [%s]", testDescriptor.getDisplayName());
                if (testDescriptor instanceof TestMethodTestDescriptor) {
                    LOGGER.info("executionSkipped()");
                }
            }
        });
    }

    public void executionFinished(TestIdentifier testIdentifier, TestExecutionResult testExecutionResult) {
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
                        System.out.println(stringBuilder);
                    }
                }
            }
        });
    }

    public void testPlanExecutionFinished(TestPlan testPlan) {
        Set<TestIdentifier> testIdentifierSet = testPlan.getRoots();
        for (TestIdentifier testIdentifier : testIdentifierSet) {
            testIdentifier.getSource().ifPresent(testSource -> LOGGER.info("testPlanExecutionFinished()"));
        }
    }
}

