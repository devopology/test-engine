package org.devopology.test.engine.support;

public class TestEngineConfiguration {

    private TestEngineConfiguration() {

    }

    public static String getValue(String systemProperty, String environmentVariable) {
        String propertyValue = System.getProperty(systemProperty);
        String environmentVariableProperty = System.getenv(environmentVariable);

        if (propertyValue != null) {
            return propertyValue;
        } else if (environmentVariableProperty != null) {
            return environmentVariableProperty;
        }

        return null;
    }
}
