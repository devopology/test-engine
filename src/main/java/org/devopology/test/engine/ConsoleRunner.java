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

import org.devopology.test.engine.internal.ConfigurationParameters;
import org.devopology.test.engine.internal.SummaryEngineExecutionListener;
import org.devopology.test.engine.logger.Logger;
import org.devopology.test.engine.logger.LoggerFactory;
import org.junit.platform.engine.ExecutionRequest;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.platform.engine.discovery.ClassNameFilter.includeClassNamePatterns;

/**
 * Class to implement a command line runner for the {@link TestEngine}
 */
public class ConsoleRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConsoleRunner.class);

    /**
     * Main method
     * <p/>
     * @param args
     */
    public static void main(String[] args) throws MalformedURLException {
        new ConsoleRunner().run(args);
    }

    public ConsoleRunner() {
        LOGGER.trace("ConsoleRunner()");
    }

    /**
     * Method to run the test engine
     */
    public void run(String[] args) throws MalformedURLException {
        LOGGER.trace("run()");

        SummaryEngineExecutionListener testExecutionSummaryListener = new SummaryEngineExecutionListener(System.out);

        File file =
                new File(
                        ConsoleRunner.class
                                .getProtectionDomain().getCodeSource().getLocation().getPath());

        LOGGER.trace("enclosing jar [%s]", file.getAbsoluteFile());

        Set<Path> classPathRoots = new HashSet<>();
        classPathRoots.add(file.getAbsoluteFile().toPath());

        if (args != null) {
            List<URL> urlList = new ArrayList<>();
            for (String arg : args) {
                File jarFile = new File(arg);
                if (jarFile.exists() && jarFile.isFile() && jarFile.canRead()) {
                    LOGGER.trace("adding jar to classpath [%s]", jarFile.getAbsolutePath());
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

        LauncherDiscoveryRequest launcherDiscoveryRequest =
                LauncherDiscoveryRequestBuilder.request()
                        .selectors(DiscoverySelectors.selectClasspathRoots(classPathRoots))
                        .filters(includeClassNamePatterns(".*"))
                        .build();

        TestEngine testEngine = new TestEngine(TestEngine.Mode.CONSOLE);

        TestDescriptor testDescriptor =
                testEngine.discover(launcherDiscoveryRequest, UniqueId.root("/", "/"));

        ConfigurationParameters testEngineConfigurationParameters = new ConfigurationParameters();

        testEngine.execute(
                ExecutionRequest.create(
                        testDescriptor,
                        testExecutionSummaryListener,
                        testEngineConfigurationParameters));

        testExecutionSummaryListener.printSummary(System.out);

        if (!testExecutionSummaryListener.hasFailures()) {
            System.exit(0);
        } else {
            System.exit(1);
        }
    }
}
