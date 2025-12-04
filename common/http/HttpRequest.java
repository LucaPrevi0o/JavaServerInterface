package common.http;

import common.Request;
import common.http.request.HttpRequestHeader;
import common.http.request.HttpRequestParameter;
import common.http.request.HttpRequestType;
import java.util.ArrayList;

/**
 * Class representing an HTTP request.
 * Extends the {@link Request Request} class.
 */
public class HttpRequest extends Request {

    private String path;
    private HttpRequestType requestType;
    private HttpRequestParameter[] parameters;
    private HttpRequestHeader[] headers;

    /** Default constructor for HttpRequest. */
    public HttpRequest() { this(null, null, null); }

    public HttpRequestType getHttpRequestType() { return requestType; }
    public HttpRequestParameter[] getParameters() { return parameters; }
    public HttpRequestHeader[] getHeaders() { return headers; }

    /**
     * Constructor for HttpRequest.
     * @param requestType the type of the HTTP request
     * @param parameters the parameters of the HTTP request
     * @param headers the headers of the HTTP request
     */
    public HttpRequest(HttpRequestType requestType, HttpRequestParameter[] parameters, HttpRequestHeader[] headers) { 

        this.requestType = requestType;
        this.parameters = parameters;
        this.headers = headers;
    }

    /**
     * Get the path from the HTTP request.
     * @return the request path (e.g., "/about")
     */
    public String getPath() { return path; }

    /**
     * Get the request type from the input string.
     * @param input the input string representing the request
     * @return the request type
     */
    protected HttpRequestType getRequestType(String input) {

        // Implementation for extracting HTTP request type from input
        if (input == null || input.isEmpty()) return null;

        String method = input.trim().split("\\s+")[0].toUpperCase();
        try { return HttpRequestType.valueOf(method); }
        catch (IllegalArgumentException e) { return null; }
    }

    /**
    * Get the request parameters from the input string.
    * @param input the input string representing the request
    * @return an array of request parameters
    */
    protected HttpRequestParameter[] getRequestParameters(String input) {

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
        
        for (int i = 0; i < params.length; i++) {

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
    protected HttpRequestHeader[] getRequestHeaders(String input) {

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
     * Create a new HttpRequest instance.
     * @param type the request type
     * @param params the request parameters
     * @param headers the request headers
     * @return a new HttpRequest instance
     */
    protected HttpRequest createRequest(HttpRequestType type, HttpRequestParameter[] params, HttpRequestHeader[] headers) {
        return new HttpRequest(type, params, headers);
    }

    /**
     * Parse the input string to create an HttpRequest object with path extraction.
     * @param input the input string representing the request
     * @return an HttpRequest object
     */
    @Override
    public HttpRequest parse(String input) {

        var type = getRequestType(input);
        var params = getRequestParameters(input);
        var headers = getRequestHeaders(input);
        var request = createRequest(type, params, headers);
        request.path = extractPath(input);
        return request;
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
