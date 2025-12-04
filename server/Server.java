package server;

public abstract class Server {

    private final int port;
    
    /**
     * Start the server.
     */
    public abstract void start();

    /**
     * Stop the server.
     */
    public abstract void stop();

    /**
     * Get the port number on which the server is listening.
     * @return the port number
     */
    public int getPort() { return port; }

    /**
     * Constructor for Server.
     * @param port the port number
     */
    public Server(int port) { this.port = port; }

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
}
