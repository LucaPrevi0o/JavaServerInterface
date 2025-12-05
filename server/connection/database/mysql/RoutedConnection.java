package server.connection.database.mysql;

import server.Server;
import common.Request;
import common.Response;

public class RoutedConnection {
    
    private Server server;

    public Response handleRequest(Request request) {
        // Implementation for handling the request and routing to the appropriate database connection
        return server.handleRequest(request);
    }
}
