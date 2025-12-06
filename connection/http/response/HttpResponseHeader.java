package jsi.connection.http.response;

import jsi.response.ResponseHeader;

/**
 * Class representing an HTTP response header.
 * Implements the {@link ResponseHeader ResponseHeader} interface.
 */
public class HttpResponseHeader implements ResponseHeader {
    
    private final String name;
    private final String value;

    /**
     * Constructor for HttpResponseHeader.
     * @param name the name of the HTTP response header
     * @param value the value of the HTTP response header
     */
    public HttpResponseHeader(String name, String value) {

        this.name = name;
        this.value = value;
    }

    /**
     * Get the name of the HTTP response header.
     * @return the name of the HTTP response header
     */
    @Override
    public String getName() { return name; }

    /**
     * Get the value of the HTTP response header.
     * @return the value of the HTTP response header
     */
    @Override
    public String getValue() { return value; }
}
