package org.devopology.test.engine.internal;

import org.devopology.test.engine.api.Named;

public class NamedImpl implements Named {

    private String name;
    private Object payload;

    /**
     * Constructor
     *
     * @param name
     * @param payload
     */
    public NamedImpl(String name, Object payload) {
        this.name = name;
        this.payload = payload;
    }

    /**
     * Method to get the display name
     *
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * Method to get the payload
     *
     * @return
     */
    public Object getPayload() {
        return payload;
    }
}
