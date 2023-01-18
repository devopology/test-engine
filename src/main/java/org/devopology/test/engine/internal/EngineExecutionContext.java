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

package org.devopology.test.engine.internal;

import org.junit.platform.engine.EngineExecutionListener;

public class EngineExecutionContext {

    private EngineExecutionListener engineExecutionListener;
    private ThrowableCollector throwableCollector;
    private Object testInstance;

    public EngineExecutionContext(EngineExecutionListener engineExecutionListener, ThrowableCollector throwableCollector) {
        this.engineExecutionListener = engineExecutionListener;
        this.throwableCollector = throwableCollector;
    }

    public EngineExecutionListener getEngineExecutionListener() {
        return engineExecutionListener;
    }

    public ThrowableCollector getThrowableCollector() {
        return throwableCollector;
    }

    public void setTestInstance(Object testInstance) {
        this.testInstance = testInstance;
    }

    public Object getTestInstance() {
        return testInstance;
    }
}
