package com.lucaprevioo.jsi;

/**
 * Abstract class representing a generic response.
 */
public interface Response {

    /**
     * Serialize the response to a string format suitable for transmission.
     * @return the serialized response
     */
    public String serialize();
}
