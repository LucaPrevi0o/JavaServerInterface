package jsi.connection.http;

import java.util.ArrayList;

import jsi.Request;
import jsi.connection.http.request.HttpRequestHeader;
import jsi.connection.http.request.HttpRequestParameter;
import jsi.connection.http.request.HttpRequestType;

/**
 * Class representing an HTTP request.
 * Extends the {@link Request Request} class.
 */
public class HttpRequest implements Request {

    private String path;
    private HttpRequestType requestType;
    private HttpRequestParameter[] parameters;
    private HttpRequestHeader[] headers;

    public HttpRequestType getHttpRequestType() { return requestType; }
    public HttpRequestParameter[] getParameters() { return parameters; }
    public HttpRequestHeader[] getHeaders() { return headers; }

    /**
     * Get the path from the HTTP request.
     * @return the request path (e.g., "/about")
     */
    public String getPath() { return path; }

    /**
     * Get a specific request parameter by name.
     * @param name the name of the request parameter
     * @return the HttpRequestParameter object, or null if not found
     */
    public Object getParameter(String name) {

        if (name == null || name.isEmpty() || parameters == null) return null;
        for (var param : parameters) if (param.getName().equals(name)) return param.getValue();
        return null;
    }

    /**
     * Get the request type from the input string.
     * @param input the input string representing the request
     * @return the request type
     */
    protected HttpRequestType extractType(String input) {

        // Implementation for extracting HTTP request type from input
        if (input == null || input.isEmpty()) return null;

        var method = input.trim().split("\\s+")[0].toUpperCase();
        try { return HttpRequestType.valueOf(method); }
        catch (IllegalArgumentException e) { return null; }
    }

    /**
    * Get the request parameters from the input string.
    * @param input the input string representing the request
    * @return an array of request parameters
    */
    protected HttpRequestParameter[] extractParameters(String input) {

        if (input == null || input.isEmpty())  return new HttpRequestParameter[0];

        // Split the request into lines
        var lines = input.split("\\r?\\n");
        if (lines.length == 0) return new HttpRequestParameter[0];

        // Extract query parameters from the request line (e.g., GET /path?key=value HTTP/1.1)
        var requestLine = lines[0];
        var parts = requestLine.split("\\s+");
        
        if (parts.length < 2) return new HttpRequestParameter[0];

        var uri = parts[1]; // e.g., "/path?key1=value1&key2=value2"
        
        // Check if there's a query string
        var queryIndex = uri.indexOf('?');
        if (queryIndex == -1) return new HttpRequestParameter[0];

        var queryString = uri.substring(queryIndex + 1);
        var params = queryString.split("&");
        
        var requestParameters = new HttpRequestParameter[params.length];
        
        for (var i = 0; i < params.length; i++) {

            var param = params[i];
            var equalsIndex = param.indexOf('=');
            
            final String key;
            final String value;
            
            if (equalsIndex != -1) {

                key = param.substring(0, equalsIndex);
                value = param.substring(equalsIndex + 1);
            } else {

                key = param;
                value = "";
            }
            
            requestParameters[i] = new HttpRequestParameter(key, value);
        }
        
        return requestParameters;
    }

    /**
     * Get the request headers from the input string.
     * @param input the input string representing the request
     * @return an array of request headers
     */
    protected HttpRequestHeader[] extractHeaders(String input) {

        if (input == null || input.isEmpty()) return new HttpRequestHeader[0];

        // Split the request into lines
        var lines = input.split("\\r?\\n");
        if (lines.length <= 1) return new HttpRequestHeader[0];

        // Skip the first line (request line) and parse headers
        var headerList = new ArrayList<HttpRequestHeader>();
        
        for (int i = 1; i < lines.length; i++) {

            var line = lines[i];
            
            // Empty line indicates end of headers
            if (line.isEmpty()) break;
            
            // Parse header (format: "Name: Value")
            var colonIndex = line.indexOf(':');
            if (colonIndex != -1) {

                var name = line.substring(0, colonIndex).trim();
                var value = line.substring(colonIndex + 1).trim();
                headerList.add(new HttpRequestHeader(name, value));
            }
        }
        
        return headerList.toArray(new HttpRequestHeader[0]);
    }

    /**
     * Parse the input string to create an HttpRequest object with path extraction.
     * @param input the input string representing the request
     * @return an HttpRequest object
     */
    public HttpRequest(String input) {

        this.requestType = extractType(input);
        this.parameters = extractParameters(input);
        this.headers = extractHeaders(input);
        this.path = extractPath(input);
    }

    /**
     * Serialize the request into a string format.
     * @return the serialized request string
     */
    @Override
    public String serialize() {
        
        var sb = new StringBuilder();
        sb.append(requestType == null ? "UNKNOWN" : requestType.getName()).append(" ");
        sb.append(path != null ? path : "/").append(" HTTP/1.1\r\n");
        
        if (headers != null) for (var header : headers)
            sb.append(header.getName()).append(": ").append(header.getValue()).append("\r\n");
        
        sb.append("\r\n");
        
        if (parameters != null && parameters.length > 0) for (int i = 0; i < parameters.length; i++) {

            var param = parameters[i];
            sb.append(param.getName()).append("=").append(param.getValue());
            if (i < parameters.length - 1) sb.append("&");
        }

        return sb.toString();
    }

    /**
     * Extract the path from the HTTP request line.
     * @param input the input string representing the request
     * @return the path (e.g., "/about")
     */
    private String extractPath(String input) {

        if (input == null || input.isEmpty()) return "/";

        var lines = input.split("\\r?\\n");
        if (lines.length == 0) return "/";

        // Parse request line: "GET /path?query HTTP/1.1"
        var requestLine = lines[0];
        var parts = requestLine.split("\\s+");
        
        if (parts.length < 2) return "/";

        var uri = parts[1]; // e.g., "/path?key1=value1"
        
        // Remove query string if present
        var queryIndex = uri.indexOf('?');
        if (queryIndex != -1) return uri.substring(0, queryIndex);
        
        return uri;
    }
}
