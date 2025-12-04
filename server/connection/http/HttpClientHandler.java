package server.connection.http;

import common.Request;
import common.Response;
import common.http.HttpRequest;
import common.http.HttpResponse;
import common.http.response.HttpResponseBody;
import common.http.response.HttpResponseHeader;
import common.http.response.HttpResponseType;
import common.response.ResponseHeader;
import server.connection.ClientHandler;

import java.nio.charset.StandardCharsets;
import java.util.List;

public class HttpClientHandler extends ClientHandler {

    private List<HttpServer.RoutedMethod> routes;

    /**
     * Constructor for HttpClientHandler.
     * @param clientSocket the socket connected to the client
     */
    public HttpClientHandler(java.net.Socket clientSocket, List<HttpServer.RoutedMethod> routes) { 
        
        super(clientSocket);
        this.routes = routes;
    }

    /**
     * Parse the incoming HTTP request string into a Request object.
     * @param input the raw HTTP request string
     * @return the parsed Request object
     */
    @Override
    protected Request parseRequest(String input) { return new HttpRequest().parse(input); }

    /**
     * Create a response based on the incoming request.
     * @param request the incoming HTTP request
     * @return the generated HTTP response
     */
    @Override
    protected Response createResponse(Request request) {

        var path = extractPath(request);
        for (var handler : routes) if (handler.getPath().equals(path)) {
                
            try { return handler.handle(request); }
            catch (Exception e) {

                e.printStackTrace();
                break;
            }
        }
        return handleNotFound(request);
    }

    /**
     * Extract the path from the request.
     * @param request the HTTP request
     * @return the path (e.g., "/about")
     */
    private String extractPath(Request request) {

        if (request instanceof HttpRequest) return ((HttpRequest) request).getPath();
        return "/";
    }

    /**
     * Default handler for routes that are not found.
     * @param request the HTTP request
     * @return a 404 Not Found response
     */
    protected Response handleNotFound(Request request) {

        String content = "<html><body><h1>404 - Not Found</h1><p>The requested resource was not found.</p></body></html>";
        
        return new HttpResponse(
            HttpResponseType.NOT_FOUND,
            new ResponseHeader[] {
                new HttpResponseHeader("Content-Type", "text/html; charset=UTF-8"),
                new HttpResponseHeader("Content-Length", String.valueOf(content.getBytes(StandardCharsets.UTF_8).length))
            },
            new HttpResponseBody(content)
        );
    }
}
