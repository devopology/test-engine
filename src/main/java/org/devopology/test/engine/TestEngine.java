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

package org.devopology.test.engine;

import org.devopology.test.engine.support.TestEngineConfigurationParameters;
import org.devopology.test.engine.support.TestEngineDiscoverySelectorResolver;
import org.devopology.test.engine.support.TestEngineEngineDiscoveryRequest;
import org.devopology.test.engine.support.TestEngineExecutor;
import org.devopology.test.engine.support.TestEngineInformation;
import org.devopology.test.engine.support.TestEngineSummaryEngineExecutionListener;
import org.devopology.test.engine.support.TestEngineUtils;
import org.devopology.test.engine.support.logger.Logger;
import org.devopology.test.engine.support.logger.LoggerFactory;
import org.devopology.test.engine.support.util.AnsiColor;
import org.devopology.test.engine.support.util.HumanReadableTime;
import org.junit.platform.engine.EngineDiscoveryRequest;
import org.junit.platform.engine.ExecutionRequest;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.engine.support.descriptor.EngineDescriptor;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.TestPlan;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.listeners.TestExecutionSummary;

import java.io.File;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

import static org.junit.platform.engine.discovery.ClassNameFilter.includeClassNamePatterns;

/**
 * Class to implement a TestEngine
 */
public class TestEngine implements org.junit.platform.engine.TestEngine {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestEngine.class);

    private static final String ENGINE_ID = "devopology-test-engine";
    private static final String GROUP_ID = "org.devopology";
    private static final String ARTIFACT_ID = "test-engine";
    private static final String VERSION = TestEngineInformation.getVersion();
    private static final String INFO = "[" + AnsiColor.BLUE_BOLD.wrap("INFO") + "] ";

    @Override
    public String getId() {
        return ENGINE_ID;
    }

    @Override
    public Optional<String> getGroupId() {
        return Optional.of(GROUP_ID);
    }

    @Override
    public Optional<String> getArtifactId() {
        return Optional.of(ARTIFACT_ID);
    }

    @Override
    public Optional<String> getVersion() {
        return Optional.of(VERSION);
    }

    @Override
    public TestDescriptor discover(EngineDiscoveryRequest engineDiscoveryRequest, UniqueId uniqueId) {
        // Create configuration parameters which first gets the
        // discovery request parameters then merges System properties
        TestEngineConfigurationParameters configurationParameters =
                new TestEngineConfigurationParameters(engineDiscoveryRequest.getConfigurationParameters());

        // Wrap the discovery request
        TestEngineEngineDiscoveryRequest testEngineDiscoveryRequest =
                new TestEngineEngineDiscoveryRequest(engineDiscoveryRequest, configurationParameters);

        // Create a EngineDescriptor as the target
        EngineDescriptor engineDescriptor = new EngineDescriptor(uniqueId, getId());

        // Create a DevopologyTestEngineDiscoverySelectorResolver and
        // resolve selectors, adding them to the engine descriptor
        new TestEngineDiscoverySelectorResolver().resolveSelectors(testEngineDiscoveryRequest, engineDescriptor);

        // Return the engine descriptor with all child test descriptors
        return engineDescriptor;
    }

    @Override
    public void execute(ExecutionRequest executionRequest) {
        new TestEngineExecutor().execute(executionRequest);
    }

    /**
     * Method to run the TestEngine as a console application
     *
     * @param args
     */
    public static void main(String[] args) {
        long startTimeMilliseconds = System.currentTimeMillis();

        PrintStream printStream = null;
        boolean failed = false;

        try {
            printStream = System.out;

            String banner = "Devopology Test Engine " + VERSION;

            StringBuilder stringBuilder = new StringBuilder();
            for (char c : banner.toCharArray()) {
                stringBuilder.append("-");
            }

            String separator = stringBuilder.toString();

            printStream.println(INFO + AnsiColor.WHITE_BOLD_BRIGHT.wrap(separator));
            printStream.println(INFO + AnsiColor.WHITE_BOLD_BRIGHT.wrap(banner));
            printStream.println(INFO + AnsiColor.WHITE_BOLD_BRIGHT.wrap(separator));
            printStream.println(INFO + AnsiColor.WHITE_BOLD_BRIGHT.wrap("Scanning all classpath jars for tests..."));

            Set<Path> classPathRoots =
                    new TreeSet<>(Comparator.comparing(o -> o.toAbsolutePath().toFile().getAbsolutePath()));

            // Add the jar containing the test engine to the class path to search for tests
            File file =
                    new File(
                            TestEngine.class
                                    .getProtectionDomain().getCodeSource().getLocation().getPath());

            classPathRoots.add(file.getAbsoluteFile().toPath());

            // Add all jars in the class path to search for tests
            String classPath = System.getProperty("java.class.path");
            String[] jars = classPath.split(File.pathSeparator);
            for (String jar : jars) {
                classPathRoots.add(new File(jar).getAbsoluteFile().toPath());
            }

            for (Path path : classPathRoots) {
                LOGGER.trace("jar [%s]", path.toAbsolutePath());
            }

            TestEngineConfigurationParameters configurationParameters = new TestEngineConfigurationParameters();

            LauncherDiscoveryRequest launcherDiscoveryRequest =
                    LauncherDiscoveryRequestBuilder.request()
                            .selectors(DiscoverySelectors.selectClasspathRoots(classPathRoots))
                            .filters(includeClassNamePatterns(".*"))
                            .configurationParameters(configurationParameters.getConfigurationMap())
                            .build();

            TestEngine testEngine = new TestEngine();

            TestDescriptor testDescriptor =
                    testEngine.discover(launcherDiscoveryRequest, UniqueId.root("/", "/"));

            TestPlan testPlan = TestEngineUtils.createTestPlan(testDescriptor, configurationParameters);

            TestEngineSummaryEngineExecutionListener summaryEngineExecutionListener = new TestEngineSummaryEngineExecutionListener(testPlan, System.out);

            testEngine.execute(
                    ExecutionRequest.create(
                            testDescriptor,
                            summaryEngineExecutionListener,
                            launcherDiscoveryRequest.getConfigurationParameters()));

            long endTimeMilliseconds = System.currentTimeMillis();

            TestExecutionSummary testExecutionSummary = summaryEngineExecutionListener.getSummary();

            banner = "Devopology Test Engine " + VERSION + " Summary";

            stringBuilder = new StringBuilder();
            for (char c : banner.toCharArray()) {
                stringBuilder.append("-");
            }

            separator = stringBuilder.toString();

            printStream.println(INFO + AnsiColor.WHITE_BOLD_BRIGHT.wrap(separator));
            printStream.println(INFO + AnsiColor.WHITE_BOLD_BRIGHT.wrap(banner));
            printStream.println(INFO + AnsiColor.WHITE_BOLD_BRIGHT.wrap(separator));
            printStream.println(INFO);

            //testExecutionSummary.printTo(new PrintWriter(new OutputStreamWriter(printStream, StandardCharsets.UTF_8)));

            printStream.println(
                    INFO
                            + AnsiColor.WHITE_BOLD_BRIGHT.wrap(
                            "TESTS : "
                                    + AnsiColor.WHITE_BOLD_BRIGHT.wrap((testExecutionSummary.getTestsFoundCount() + testExecutionSummary.getContainersFailedCount()))
                                    + ", "
                                    + AnsiColor.GREEN_BOLD_BRIGHT.wrap("PASSED")
                                    + " : "
                                    + AnsiColor.WHITE_BOLD_BRIGHT.wrap(testExecutionSummary.getTestsSucceededCount() - testExecutionSummary.getContainersFailedCount()))
                            + ", "
                            + AnsiColor.RED_BOLD_BRIGHT.wrap("FAILED")
                            + " : "
                            + AnsiColor.WHITE_BOLD_BRIGHT.wrap((testExecutionSummary.getTestsFailedCount() + testExecutionSummary.getContainersFailedCount()))
                            + ", "
                            + AnsiColor.YELLOW_BOLD_BRIGHT.wrap("SKIPPED")
                            + " : "
                            + AnsiColor.WHITE_BOLD_BRIGHT.wrap(testExecutionSummary.getTestsSkippedCount()));

            printStream.println(INFO);
            printStream.println(INFO + AnsiColor.WHITE_BOLD_BRIGHT.wrap(separator));

            failed = (testExecutionSummary.getTestsFailedCount() + testExecutionSummary.getContainersFailedCount()) > 0;

            if (failed) {
                printStream.println(INFO + AnsiColor.RED_BOLD_BRIGHT.wrap("FAILED"));
            } else {
                printStream.println(INFO + AnsiColor.GREEN_BOLD_BRIGHT.wrap("PASSED"));
            }

            printStream.println(INFO + AnsiColor.WHITE_BOLD_BRIGHT.wrap(separator));
            printStream.println(INFO + AnsiColor.WHITE_BOLD_BRIGHT.wrap("Total Time  : " + AnsiColor.WHITE_BOLD_BRIGHT.wrap(HumanReadableTime.toHumanReadable(endTimeMilliseconds - startTimeMilliseconds, false))));
            printStream.println(INFO + AnsiColor.WHITE_BOLD_BRIGHT.wrap("Finished At : " + HumanReadableTime.now()));
            printStream.println(INFO + AnsiColor.WHITE_BOLD_BRIGHT.wrap(separator));
        } finally {
            if (printStream != null) {
                try {
                    printStream.close();
                } catch (Throwable t) {
                    // DO NOTHING
                }
            }

            if (failed) {
                System.exit(1);
            } else {
                System.exit(0);
            }
        }
    }
}
