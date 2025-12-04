package server.connection.database;

import server.connection.ConnectionServer;

public abstract class DatabaseServer extends ConnectionServer<DatabaseClientHandler> {

    private DatabaseEngine databaseEngine;

    /**
     * Constructor for DatabaseServer.
     * @param port the port number
     */
    public DatabaseServer(int port, DatabaseEngine databaseEngine) {

        super(port);
        this.databaseEngine = databaseEngine;
    }

    /**
     * Get the DatabaseEngine instance.
     * @return the DatabaseEngine
     */
    public DatabaseEngine getDatabaseEngine() { return databaseEngine; }

    /**
     * Create a DatabaseClientHandler instance for the connected client.
     * @param clientSocket the socket connected to the client
     * @return the DatabaseClientHandler instance
     */
    @Override
    protected DatabaseClientHandler createClientHandler(java.net.Socket clientSocket) {
        return new DatabaseClientHandler(clientSocket);
    }
    
}
