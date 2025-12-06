package jsi.connection.http.request;

import jsi.request.RequestHeader;

/**
 * Class representing an HTTP request header.
 * Implements the {@link RequestHeader RequestHeader} interface.
 */
public class HttpRequestHeader implements RequestHeader {

    private final String name;
    private final String value;

    /**
     * Constructor for HttpRequestHeader.
     * @param name the name of the HTTP request header
     * @param value the value of the HTTP request header
     */
    public HttpRequestHeader(String name, String value) {

        this.name = name;
        this.value = value;
    }

    /**
     * Get the name of the request header.
     * @return the name of the request header
     */
    @Override
    public String getName() { return name; }

    /**
     * Get the value of the request header.
     * @return the value of the request header
     */
    @Override
    public String getValue() { return value; }
}
