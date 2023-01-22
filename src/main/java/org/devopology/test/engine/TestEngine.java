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

import org.devopology.test.engine.internal.listener.PrintStreamEngineExecutionListener;
import org.devopology.test.engine.internal.listener.SummaryEngineExecutionListener;
import org.junit.platform.engine.ConfigurationParameters;
import org.junit.platform.engine.EngineDiscoveryRequest;
import org.junit.platform.engine.ExecutionRequest;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.engine.support.descriptor.EngineDescriptor;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.platform.engine.discovery.ClassNameFilter.includeClassNamePatterns;

public class TestEngine implements org.junit.platform.engine.TestEngine {

    private static final String ENGINE_ID = "devopology-test-engine";
    private static final String GROUP_ID = "org.devopology";
    private static final String ARTIFACT_ID = "test-engine";
    private static final String VERSION = TestEngineVersion.getVersion();

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

    public TestEngine() {
        // DO NOTHING
    }

    @Override
    public TestDescriptor discover(EngineDiscoveryRequest discoveryRequest, UniqueId uniqueId) {
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
        // Create a DevopologyTestEngineExecutor and execute the execution request
        new TestEngineExecutor().execute(executionRequest);
    }

    public static void main(String[] args) {
        String classPath = System.getProperty("java.class.path");

        File file =
                new File(
                        TestEngine.class
                                .getProtectionDomain().getCodeSource().getLocation().getPath());

        Set<Path> classPathRoots = new HashSet<>();
        classPathRoots.add(file.getAbsoluteFile().toPath());

        String[] jars = classPath.split(File.pathSeparator);
        for (String jar : jars) {
            //System.out.println("adding jar [" + jar + "]");
            classPathRoots.add(new File(jar).getAbsoluteFile().toPath());
        }

        /*

        if (args != null) {
            List<URL> urlList = new ArrayList<>();
            for (String arg : args) {
                File jarFile = new File(arg);
                if (jarFile.exists() && jarFile.isFile() && jarFile.canRead()) {
                    classPathRoots.add(jarFile.getAbsoluteFile().toPath());
                    urlList.add(jarFile.getAbsoluteFile().toURI().toURL());
                }
            }

            if (urlList.size() > 0) {
                URLClassLoader urlClassLoader = new URLClassLoader(urlList.toArray(new URL[0]), Thread.currentThread().getContextClassLoader());
                Thread.currentThread().setContextClassLoader(urlClassLoader);
            }
        } else {
            LOGGER.error("No arguments provided");
            System.exit(1);
        }

        for (Path path : classPathRoots) {
            LOGGER.trace("class path root [%s]", path);
        }
        */

        LauncherDiscoveryRequest launcherDiscoveryRequest =
                LauncherDiscoveryRequestBuilder.request()
                        .selectors(DiscoverySelectors.selectClasspathRoots(classPathRoots))
                        .filters(includeClassNamePatterns(".*"))
                        .build();

        TestEngine testEngine = new TestEngine();

        TestDescriptor testDescriptor =
                testEngine.discover(launcherDiscoveryRequest, UniqueId.root("/", "/"));

        SummaryEngineExecutionListener summaryEngineExecutionListener = new SummaryEngineExecutionListener(System.out);
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
