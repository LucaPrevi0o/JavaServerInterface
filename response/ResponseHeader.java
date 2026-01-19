package com.lucaprevioo.jsi.response;

/**
 * Interface representing a response header.
 */
public interface ResponseHeader {
    
    /**
     * Get the name of the response header.
     * @return the name of the response header
     */
    public String getName();

    /**
     * Get the value of the response header.
     * @return the value of the response header
     */
    public String getValue();
}
