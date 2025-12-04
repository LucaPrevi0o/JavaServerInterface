package server.connection;

import common.Request;
import common.Response;

public abstract class ClientHandler implements Runnable {
    
    /**
     * Parse the incoming request string into a Request object.
     * @param input the input string representing the request
     * @return the parsed Request object
     */
    protected abstract Request parseRequest(String input);

    /**
     * Create a Response object based on the given Request.
     * @param request the Request object
     * @return the created Response object
     */
    protected abstract Response createResponse(Request request);
}
