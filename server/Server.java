package server;

public abstract class Server {
    
    /**
     * Start the server.
     */
    public abstract void start();

    /**
     * Stop the server.
     */
    public abstract void stop();

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
