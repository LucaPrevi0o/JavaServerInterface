package jsi.connection.http.response;

import jsi.response.ResponseType;

/**
 * Enum representing HTTP response types.
 * Implements the {@link ResponseType ResponseType} interface.
 */
public enum HttpResponseType implements ResponseType {
    
    OK(200, "OK"),
    CREATED(201, "Created"),
    NO_CONTENT(204, "No Content"),
    BAD_REQUEST(400, "Bad Request"),
    UNAUTHORIZED(401, "Unauthorized"),
    FORBIDDEN(403, "Forbidden"),
    NOT_FOUND(404, "Not Found"),
    INTERNAL_SERVER_ERROR(500, "Internal Server Error"),
    NOT_IMPLEMENTED(501, "Not Implemented"),
    BAD_GATEWAY(502, "Bad Gateway");
    
    private final int code;
    private final String reasonPhrase;
    
    /** 
     * Constructor for HttpResponseType.
     * @param code the HTTP status code associated with the response type
     * @param reasonPhrase the reason phrase for the status code
     */
    HttpResponseType(int code, String reasonPhrase) { 
        this.code = code;
        this.reasonPhrase = reasonPhrase;
    }
    
    /**
     * Get the HTTP status code associated with the response type.
     * @return the HTTP status code
     */
    public int getCode() { return code; }

    /**
     * Get the reason phrase for the status code.
     * @return the reason phrase
     */
    public String getReasonPhrase() { return reasonPhrase; }

    /**
     * Get the name of the response type (code + reason phrase).
     * @return the name of the response type
     */
    @Override
    public String getName() { return code + " " + reasonPhrase; }
}
