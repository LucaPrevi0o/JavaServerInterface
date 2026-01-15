package jsi.connection.http;

import jsi.Response;
import jsi.response.ResponseBody;
import jsi.response.ResponseHeader;
import jsi.response.ResponseType;
import jsi.connection.http.response.HttpResponseHeader;

/**
 * Represents an HTTP response.
 */
public class HttpResponse implements Response {
    
    private ResponseType responseType;
    private ResponseHeader[] headers;
    private ResponseBody body;

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
     * Constructor for HttpResponse.
     */
    public HttpResponse() { this(null, null, null); }

    /**
     * Constructor for HttpResponse.
     * @param responseType The type of the HTTP response.
     * @param headers HTTP response headers.
     * @param body HTTP response body.
     */
    public HttpResponse(ResponseType responseType, ResponseHeader[] headers, ResponseBody body) { 
        
        this.responseType = responseType;
        this.headers = headers;
        this.body = body;
    }

    /**
     * Add a response header (appends to existing headers array).
     * @param header header to add
     */
    public void addHeader(ResponseHeader header) {

        if (header == null) return;
        if (this.headers == null) {
            
            this.headers = new ResponseHeader[] { header };
            return;
        }

        var newHeaders = new ResponseHeader[this.headers.length + 1];
        System.arraycopy(this.headers, 0, newHeaders, 0, this.headers.length);
        newHeaders[this.headers.length] = header;
        this.headers = newHeaders;
    }
    
    /**
     * Add a cookie by adding a Set-Cookie header.
     * @param cookie the cookie to add
     */
    public void addCookie(Cookie cookie) {

        if (cookie == null) return;
        this.addHeader(new HttpResponseHeader("Set-Cookie", cookie.getName() + "=" + cookie.getValue()));
    }

    /**
     * Delete a cookie by adding a Set-Cookie header with an expired date.
     * @param cookieName the name of the cookie to delete
     */
    public void deleteCookie(String cookieName) {

        if (cookieName == null || cookieName.isEmpty()) return;
        this.addHeader(new HttpResponseHeader("Set-Cookie", cookieName + "=; Expires=Thu, 01 Jan 1970 00:00:00 GMT; Path=/"));
    }

    /**
     * Serialize the HTTP response to a string format suitable for transmission.
     * @return the serialized HTTP response
     */
    @Override
    public String serialize() {

        var response = new StringBuilder();
        
        // Status line: HTTP/1.1 200 OK
        if (getResponseType() != null) response.append("HTTP/1.1 ").append(getResponseType().getName()).append("\r\n");
        
        // Headers
        if (getHeaders() != null) for (ResponseHeader header : getHeaders())
            response.append(header.getName()).append(": ").append(header.getValue()).append("\r\n");
        
        // Empty line between headers and body
        response.append("\r\n");
        
        // Body
        if (getBody() != null && getBody().getContent() != null) response.append(getBody().getContent());
        
        return response.toString();
    }
}
