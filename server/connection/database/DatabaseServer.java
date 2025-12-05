package server.connection.database;

import server.connection.ConnectionServer;

/**
 * Abstract class representing a database server.
 * Extends ConnectionServer to handle database client connections.
 * @param <T> the type of DatabaseClientHandler
 */
public abstract class DatabaseServer<T extends DatabaseClientHandler> extends ConnectionServer<T> {

    private DatabaseEngine databaseEngine;
    private StorageEngine storageEngine;

    /**
     * Constructor for DatabaseServer.
     * @param port the port number
     */
    public DatabaseServer(int port, DatabaseEngine databaseEngine, StorageEngine storageEngine) {

        super(port);
        this.databaseEngine = databaseEngine;
        this.storageEngine = storageEngine;
    }

    /**
     * Get the DatabaseEngine instance.
     * @return the DatabaseEngine
     */
    public DatabaseEngine getDatabaseEngine() { return databaseEngine; }

    /**
     * Get the StorageEngine instance.
     * @return the StorageEngine
     */   
    public StorageEngine getStorageEngine() { return storageEngine; }
}
