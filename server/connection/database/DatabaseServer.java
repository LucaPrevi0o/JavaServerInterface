package server.connection.database;

import server.connection.ConnectionServer;

/**
 * Abstract class representing a database server.
 * Extends ConnectionServer to handle database client connections.
 * @param <T> the type of DatabaseClientHandler
 */
public abstract class DatabaseServer<T extends DatabaseClientHandler> extends ConnectionServer<T> {

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
}
