package com.lucaprevioo.jsi;
/**
 * Abstract class representing a generic request.
 */
public interface Request {

    /**
     * Serialize the request into a string format.
     * @return the serialized request string
     */
    public abstract String serialize();
}
