package JavaServerInterface.connection.http.request;

import JavaServerInterface.request.RequestParameter;

/**
 * Class representing an HTTP request parameter.
 * Implements the {@link RequestParameter RequestParameter} interface.
 */
public class HttpRequestParameter implements RequestParameter {

    private final String name;
    private final String value;

    /**
     * Constructor for HttpRequestParameter.
     * @param name the name of the HTTP request parameter
     * @param value the value of the HTTP request parameter
     */
    public HttpRequestParameter(String name, String value) {

        this.name = name;
        this.value = value;
    }

    /**
     * Get the name of the request parameter.
     * @return the name of the request parameter
     */
    @Override
    public String getName() { return name; }

    /**
     * Get the value of the request parameter.
     * @return the value of the request parameter
     */
    @Override
    public String getValue() { return value; }
    
}
