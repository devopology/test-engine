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
import org.devopology.test.engine.support.TestEngineEngineDiscoveryRequest;
import org.devopology.test.engine.support.TestEngineDiscoverySelectorResolver;
import org.devopology.test.engine.support.TestEngineExecutor;
import org.devopology.test.engine.support.TestEngineInformation;
import org.devopology.test.engine.support.listener.PrintStreamEngineExecutionListener;
import org.devopology.test.engine.support.listener.SummaryEngineExecutionListener;
import org.devopology.test.engine.support.util.AnsiColor;
import org.junit.platform.engine.EngineDiscoveryRequest;
import org.junit.platform.engine.ExecutionRequest;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.engine.support.descriptor.EngineDescriptor;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;

import java.io.File;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.platform.engine.discovery.ClassNameFilter.includeClassNamePatterns;

/**
 * Class to implement a TestEngine
 */
public class TestEngine implements org.junit.platform.engine.TestEngine {

    private static final String ENGINE_ID = "devopology-test-engine";
    private static final String GROUP_ID = "org.devopology";
    private static final String ARTIFACT_ID = "test-engine";
    private static final String VERSION = TestEngineInformation.getVersion();

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
    public TestDescriptor discover(EngineDiscoveryRequest discoveryRequest, UniqueId uniqueId) {
        // Create configuration parameters which first gets the
        // discovery request parameters then merges System properties
        TestEngineConfigurationParameters configurationParameters =
                new TestEngineConfigurationParameters(discoveryRequest.getConfigurationParameters());

        // Wrap the discovery request
        discoveryRequest = new TestEngineEngineDiscoveryRequest(discoveryRequest, configurationParameters);

        // Create a EngineDescriptor as the target
        EngineDescriptor engineDescriptor = new EngineDescriptor(uniqueId, getId());

        // Create a DevopologyTestEngineDiscoverySelectorResolver and
        // resolve selectors, adding them to the engine descriptor
        new TestEngineDiscoverySelectorResolver().resolveSelectors(discoveryRequest, engineDescriptor);

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
        AnsiColor.force();

        Set<Path> classPathRoots = new HashSet<>();

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

        TestEngineConfigurationParameters configurationParameters = new TestEngineConfigurationParameters();

        LauncherDiscoveryRequest launcherDiscoveryRequest =
                LauncherDiscoveryRequestBuilder.request()
                        .selectors(DiscoverySelectors.selectClasspathRoots(classPathRoots))
                        .filters(includeClassNamePatterns(".*"))
                        .configurationParameters(configurationParameters.getConfigurationMap())
                        .build();

        TestEngine testEngine = new TestEngine();

        SummaryEngineExecutionListener summaryEngineExecutionListener = new SummaryEngineExecutionListener(System.out);

        TestDescriptor testDescriptor =
                testEngine.discover(launcherDiscoveryRequest, UniqueId.root("/", "/"));

        PrintStreamEngineExecutionListener printStreamEngineExecutionListener = new PrintStreamEngineExecutionListener(summaryEngineExecutionListener, System.out);

        testEngine.execute(
                ExecutionRequest.create(
                        testDescriptor,
                        printStreamEngineExecutionListener,
                        launcherDiscoveryRequest.getConfigurationParameters()));

        summaryEngineExecutionListener.printSummary(System.out);

        if (!summaryEngineExecutionListener.hasFailures()) {
            System.exit(0);
        } else {
            System.exit(1);
        }
    }
}
