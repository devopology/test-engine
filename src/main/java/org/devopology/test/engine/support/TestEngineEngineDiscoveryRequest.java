package org.devopology.test.engine.support;

import org.junit.platform.engine.ConfigurationParameters;
import org.junit.platform.engine.DiscoveryFilter;
import org.junit.platform.engine.DiscoverySelector;
import org.junit.platform.engine.EngineDiscoveryListener;
import org.junit.platform.engine.EngineDiscoveryRequest;

import java.util.List;
import java.util.Objects;

public class TestEngineEngineDiscoveryRequest implements EngineDiscoveryRequest {

    private EngineDiscoveryRequest engineDiscoveryRequest;
    private ConfigurationParameters configurationParameters;

    public TestEngineEngineDiscoveryRequest(EngineDiscoveryRequest engineDiscoveryRequest, ConfigurationParameters configurationParameters) {
        Objects.requireNonNull(engineDiscoveryRequest);

        this.engineDiscoveryRequest = engineDiscoveryRequest;
        this.configurationParameters = configurationParameters;
    }

    @Override
    public <T extends DiscoverySelector> List<T> getSelectorsByType(Class<T> clazz) {
        return engineDiscoveryRequest.getSelectorsByType(clazz);
    }

    @Override
    public <T extends DiscoveryFilter<?>> List<T> getFiltersByType(Class<T> clazz) {
        return engineDiscoveryRequest.getFiltersByType(clazz);
    }

    @Override
    public ConfigurationParameters getConfigurationParameters() {
        return configurationParameters;
    }

    @Override
    public EngineDiscoveryListener getDiscoveryListener() {
        return engineDiscoveryRequest.getDiscoveryListener();
    }
}
