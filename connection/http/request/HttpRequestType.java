package com.lucaprevioo.jsi.connection.http.request;

import com.lucaprevioo.jsi.request.RequestType;

/**
 * Enumeration of HTTP request types.
 * Implements the {@link RequestType RequestType} interface.
 */
public enum HttpRequestType implements RequestType {
    
    GET,
    POST,
    PUT,
    DELETE,
    HEAD,
    OPTIONS,
    PATCH,
    TRACE;

    /**
     * Get the name of the request type.
     * @return the name of the request type
     */
    @Override
    public String getName() { return this.toString(); }
}