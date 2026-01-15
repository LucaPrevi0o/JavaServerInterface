package jsi.connection.http;

import java.util.List;

import jsi.Request;
import jsi.Response;
import jsi.connection.ConnectionServer;
import jsi.connection.http.response.HttpResponseBody;
import jsi.connection.http.response.HttpResponseHeader;
import jsi.connection.http.response.HttpResponseType;
import jsi.response.ResponseHeader;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Abstract HTTP server implementation with routing support.
 * Extend this class and register routes in the registerRoutes() method.
 */
public abstract class HttpServer extends ConnectionServer {
    /*
     * Inner class representing a routed method.
     */
    public class RoutedMethod {

        String path;
        Method method;
        String staticResource;

        /**
         * Get the path this method handles.
         * @return the path
         */
        public String getPath() { return path; }

        /**
         * Constructor for RoutedMethod.
         * @param path the URL path
         * @param method the method handling the route
         * @param staticResource the static resource path (if any)
         */
        public RoutedMethod(String path, Method method, String staticResource) {

            this.path = path;
            this.method = method;
            this.staticResource = staticResource;
        }

        /**
         * Handle the request by either serving a static file or invoking the method.
         * @param request the incoming HTTP request
         * @return the generated HTTP response
         * @throws Exception if an error occurs during method invocation
         */
        public HttpResponse handleRequest(Request request) throws Exception {

            method.setAccessible(true);
            if (staticResource == null || staticResource.isEmpty()) return (HttpResponse) method.invoke(HttpServer.this, request);
            
            var response = (HttpResponse) method.invoke(HttpServer.this, request);
            var htmlResponse = serveHtmlFile(staticResource);
            if (response != null && response.getHeaders() != null) 
                for (var header : response.getHeaders()) htmlResponse.addHeader(header);
            return htmlResponse;
        }
    }

    private final List<RoutedMethod> routes = new ArrayList<>();

    /**
     * Constructor for HttpServer.
     * @param port the port number on which the server will listen
     */
    public HttpServer(int port) { super(port);  }

    /**
     * Hook method called when the server starts.
     * 
     * This method scans for methods annotated with {@code @Route} in the subclass
     * and registers them as route handlers.
     */
    protected void registerRoutes() {
        
        for (var method : this.getClass().getDeclaredMethods()) if (method.isAnnotationPresent(Route.class)) {
            
            var routeAnnotation = method.getAnnotation(Route.class);
            var path = routeAnnotation.path();
            var staticResource = routeAnnotation.staticResource();
            routes.add(new RoutedMethod(path, method, staticResource));
        }
    }

    /**
     * Parse an HTTP request string into an HttpRequest object.
     * @param input the raw HTTP request string
     * @return the parsed HttpRequest object
     */
    @Override
    public HttpRequest parseRequest(String input) { return new HttpRequest(input); }

    /**
     * Create a response based on the incoming request.
     * @param request the incoming HTTP request
     * @return the generated HTTP response
     */
    @Override
    public HttpResponse handleRequest(Request request) {

        var path = extractPath(request);
        for (var handler : routes) if (handler.getPath().equals(path)) {
            
            try { return handler.handleRequest(request); }
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
    protected HttpResponse handleNotFound(Request request) {

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

    /**
     * Hook method called after the server has started.
     * 
     * This method registers the routes defined in the subclass.
     */
    @Override
    protected void onServerStarted() { registerRoutes(); }

    /**
     * Serve an HTML file from the filesystem.
     * @param filePath the path to the HTML file (relative or absolute)
     * @return HTTP response with the file content or 500 error if file cannot be read
     */
    protected HttpResponse serveHtmlFile(String filePath) {
        return serveFile(filePath, "text/html; charset=UTF-8");
    }

    /**
     * Serve a file from the filesystem with a specific content type.
     * Uses the generic readFile() method from Server and wraps it in an HTTP response.
     * 
     * @param filePath the path to the file (relative or absolute)
     * @param contentType the MIME type of the file
     * @return HTTP response with the file content or 500 error if file cannot be read
     */
    protected HttpResponse serveFile(String filePath, String contentType) {

        try {

            var path = Paths.get(filePath);
            var content = Files.readString(path, StandardCharsets.UTF_8);
            var contentBytes = content.getBytes(StandardCharsets.UTF_8);
            
            return new HttpResponse(
                HttpResponseType.OK,
                new ResponseHeader[] {
                    new HttpResponseHeader("Content-Type", contentType),
                    new HttpResponseHeader("Content-Length", String.valueOf(contentBytes.length))
                },
                new HttpResponseBody(content)
            );
        } catch (IOException e) { return handleFileReadError(filePath, e); }
    }

    /**
     * Handle errors that occur when reading files.
     * @param filePath the path that failed to read
     * @param error the exception that occurred
     * @return a 500 Internal Server Error response
     */
    protected HttpResponse handleFileReadError(String filePath, IOException error) {

        String content = "<html><body><h1>500 - Internal Server Error</h1>" +
                        "<p>Could not read file: " + filePath + "</p>" +
                        "<p>Error: " + error.getMessage() + "</p></body></html>";
        
        return new HttpResponse(
            HttpResponseType.INTERNAL_SERVER_ERROR,
            new ResponseHeader[] {
                new HttpResponseHeader("Content-Type", "text/html; charset=UTF-8"),
                new HttpResponseHeader("Content-Length", String.valueOf(content.getBytes(StandardCharsets.UTF_8).length))
            },
            new HttpResponseBody(content)
        );
    }

    /**
     * Create an HTML response with the given content.
     * @param type the HTTP response type
     * @param content the HTML content
     * @return the HTTP response
     */
    protected Response createHtmlResponse(HttpResponseType type, String content) {

        var contentBytes = content.getBytes(StandardCharsets.UTF_8);
        return new HttpResponse(
            type,
            new ResponseHeader[] {
                new HttpResponseHeader("Content-Type", "text/html; charset=UTF-8"),
                new HttpResponseHeader("Content-Length", String.valueOf(contentBytes.length))
            },
            new HttpResponseBody(content)
        );
    }
}
