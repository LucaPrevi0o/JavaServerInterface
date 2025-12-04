package common;

import common.request.RequestHeader;
import common.request.RequestParameter;
import common.request.RequestType;

/**
 * Abstract class representing a generic request.
 */
public abstract class Request {
    
    private RequestType requestType;
    private RequestParameter[] parameters;
    private RequestHeader[] headers;

    /**
     * Constructor for Request.
     * @param requestType The type of the request.
     * @param parameters Request parameters.
     * @param headers Request headers.
     */
    public Request(RequestType requestType, RequestParameter[] parameters, RequestHeader[] headers) {

        this.requestType = requestType;
        this.parameters = parameters;
        this.headers = headers;
    }

    /**
     * Get the request type.
     * @return the request type
     */
    public RequestType getRequestType() { return requestType; }

    /**
     * Get the request parameters.
     * @return the request parameters
     */
    public RequestParameter[] getParameters() { return parameters; }

    /**
     * Get the request headers.
     * @return the request headers
     */
    public RequestHeader[] getHeaders() { return headers; }

    /**
     * Abstract method to get the request type from input.
     * @param input the input string representing the request
     * @return the request type
     */
    protected abstract RequestType getRequestType(String input);

    /**
     * Abstract method to get the request parameters from input.
     * @param input the input string representing the request
     * @return an array of request parameters
     */
    protected abstract RequestParameter[] getRequestParameters(String input);

    /**
     * Abstract method to get the request headers from input.
     * @param input the input string representing the request
     * @return an array of request headers
     */
    protected abstract RequestHeader[] getRequestHeaders(String input);

    /**
     * Abstract method to create a Request object.
     * @param type the request type
     * @param params the request parameters
     * @param headers the request headers
     * @return a new Request object
     */
    protected abstract Request createRequest(RequestType type, RequestParameter[] params, RequestHeader[] headers);

    /**
     * Parse the input string to create a Request object.
     * @param input the input string representing the request
     * @return a Request object
     */
    public Request parse(String input) {

        var type = getRequestType(input);
        var params = getRequestParameters(input);
        var headers = getRequestHeaders(input);
        return createRequest(type, params, headers);
    }
}
