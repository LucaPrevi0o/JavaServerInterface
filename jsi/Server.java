package jsi;

public abstract class Server {
    
    /**
     * Start the server.
     */
    public abstract void start();

    /**
     * Hook method called before the server starts.
     * 
     * This method can be overridden by subclasses to perform any setup or initialization
     * tasks before the server begins accepting connections. It is empty by default.
     */
    protected void onBeforeStart() {}

    /**
     * Hook method called after the server has started.
     * 
     * This method can be overridden by subclasses to perform any actions
     * after the server has started. It is empty by default.
     */
    protected void onServerStarted() {}

    /**
     * Handle a request and generate a response.
     * @param request the request to handle
     * @return the response generated from the request
     */
    public abstract Response handleRequest(Request request);

    /**
     * Parse the incoming request string into a Request object.
     * @param input the input string representing the request
     * @return the parsed Request object
     */
    protected abstract Request parseRequest(String input);
}
