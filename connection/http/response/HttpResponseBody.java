package com.lucaprevioo.jsi.connection.http.response;

import com.lucaprevioo.jsi.response.ResponseBody;

/**
 * Represents the body of an HTTP response.
 */
public class HttpResponseBody implements ResponseBody {
    
    private final String content;

    /**
     * Constructor for HttpResponseBody.
     * @param content The content of the HTTP response body.
     */
    public HttpResponseBody(String content) { this.content = content; }

    /**
     * Get the content of the HTTP response body.
     * @return the content
     */
    @Override
    public String getContent() { return content; }
}
