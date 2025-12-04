package common.http;

import common.Request;
import common.http.request.HttpRequestHeader;
import common.http.request.HttpRequestParameter;
import common.http.request.HttpRequestType;
import common.request.RequestHeader;
import common.request.RequestParameter;
import common.request.RequestType;

/**
 * Class representing an HTTP request.
 * Extends the {@link Request Request} class.
 */
public class HttpRequest extends Request {

    private String path;

    /** Default constructor for HttpRequest. */
    public HttpRequest() { this(null, null, null); }

    /**
     * Constructor for HttpRequest.
     * @param requestType the type of the HTTP request
     * @param parameters the parameters of the HTTP request
     * @param headers the headers of the HTTP request
     */
    public HttpRequest(RequestType requestType, RequestParameter[] parameters, RequestHeader[] headers) { 
        super(requestType, parameters, headers); 
    }

    /**
     * Get the path from the HTTP request.
     * @return the request path (e.g., "/about")
     */
    public String getPath() { return path; }

    /**
     * Set the path for this HTTP request.
     * @param path the request path
     */
    private void setPath(String path) { this.path = path; }

    /**
     * Get the request type from the input string.
     * @param input the input string representing the request
     * @return the request type
     */
    @Override
    protected RequestType getRequestType(String input) {

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
    @Override
    protected RequestParameter[] getRequestParameters(String input) {

        if (input == null || input.isEmpty())  return new RequestParameter[0];

        // Split the request into lines
        var lines = input.split("\\r?\\n");
        if (lines.length == 0) return new RequestParameter[0];

        // Extract query parameters from the request line (e.g., GET /path?key=value HTTP/1.1)
        var requestLine = lines[0];
        var parts = requestLine.split("\\s+");
        
        if (parts.length < 2) return new RequestParameter[0];

        var uri = parts[1]; // e.g., "/path?key1=value1&key2=value2"
        
        // Check if there's a query string
        var queryIndex = uri.indexOf('?');
        if (queryIndex == -1) return new RequestParameter[0];

        var queryString = uri.substring(queryIndex + 1);
        var params = queryString.split("&");
        
        var requestParameters = new RequestParameter[params.length];
        
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
    @Override
    protected RequestHeader[] getRequestHeaders(String input) {

        if (input == null || input.isEmpty()) return new RequestHeader[0];

        // Split the request into lines
        var lines = input.split("\\r?\\n");
        if (lines.length <= 1) return new RequestHeader[0];

        // Skip the first line (request line) and parse headers
        var headerList = new java.util.ArrayList<RequestHeader>();
        
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
        
        return headerList.toArray(new RequestHeader[0]);
    }

    /**
     * Create a new HttpRequest instance.
     * @param type the request type
     * @param params the request parameters
     * @param headers the request headers
     * @return a new HttpRequest instance
     */
    @Override
    protected HttpRequest createRequest(RequestType type, RequestParameter[] params, RequestHeader[] headers) {
        return new HttpRequest(type, params, headers);
    }

    /**
     * Parse the input string to create an HttpRequest object with path extraction.
     * @param input the input string representing the request
     * @return an HttpRequest object
     */
    @Override
    public HttpRequest parse(String input) {

        HttpRequest request = (HttpRequest) super.parse(input);
        request.setPath(extractPath(input));
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
