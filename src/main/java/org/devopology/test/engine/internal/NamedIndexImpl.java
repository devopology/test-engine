package org.devopology.test.engine.internal;

import org.devopology.test.engine.api.NamedIndex;

public class NamedIndexImpl implements NamedIndex {

    private String format;
    private Object payload;

    /**
     * Constructor
     *
     * @param payload
     */
    public NamedIndexImpl(Object payload) {
        this.payload = payload;
    }

    /**
     * Constructor
     *
     * @param format
     * @param payload
     */
    public NamedIndexImpl(String format, Object payload) {
        this.format = format;
        this.payload = payload;
    }

    /**
     * Method to get the display name format
     *
     * @return
     */
    public String getFormat() {
        return format;
    }

    /**
     * Method to get the payload
     *
     * @return
     */
    public Object getPayload() {
        return payload;
    }

    /**
     * Method to get the formatted display name
     *
     * @param index
     * @return
     */
    public String getName(int index) {
        return String.format(format, index);
    }
}
