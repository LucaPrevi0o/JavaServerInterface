package common.http.request;

import common.request.RequestType;

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