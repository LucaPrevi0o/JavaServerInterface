package com.lucaprevioo.jsi;

/**
 * Abstract class representing a client that can send requests and receive responses.
 */
public abstract class Client {
    
    /**
     * Get the response for a given request.
     * @param request the request to send
     * @return the response received
     */
    public abstract Response getResponse(Request request);
}
