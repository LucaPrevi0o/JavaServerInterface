package common;

import common.response.ResponseType;
import common.response.ResponseHeader;
import common.response.ResponseBody;


/**
 * Abstract class representing a generic response.
 */
public abstract class Response {
    
    private ResponseType responseType;
    private ResponseHeader[] headers;
    private ResponseBody body;

    /**
     * Constructor for Response.
     * @param responseType The type of the response.
     * @param headers Response headers.
     * @param body Response body.
     */
    public Response(ResponseType responseType, ResponseHeader[] headers, ResponseBody body) {

        this.responseType = responseType;
        this.headers = headers;
        this.body = body;
    }

    /**
     * Get the response type.
     * @return the response type
     */
    public ResponseType getResponseType() { return responseType; }

    /**
     * Get the response headers.
     * @return the response headers
     */
    public ResponseHeader[] getHeaders() { return headers; }

    /**
     * Get the response body.
     * @return the response body
     */
    public ResponseBody getBody() { return body; }


    /**
     * Serialize the response to a string format suitable for transmission.
     * @return the serialized response
     */
    public abstract String serialize();
}
