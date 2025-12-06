package jsi.connection.database;

import jsi.Request;
import jsi.connection.ConnectionServer;
import jsi.connection.database.engine.DatabaseEngine;
import jsi.connection.database.storage.StorageEngine;

/**
 * Abstract class representing a database server.
 * Extends ConnectionServer to handle database client connections.
 * @param <T> the type of DatabaseClientHandler
 */
public abstract class DatabaseServer extends ConnectionServer {

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

    /**
     * Parse the incoming request string into a Query object.
     * @param input the input string representing the request
     * @return the parsed Query object
     */
    @Override
    protected abstract Query parseRequest(String input);

    /**
     * Handle the incoming request and return the query result.
     * @param request the incoming request
     * @return the result of the query execution
     */
    @Override
    public QueryResult handleRequest(Request request) {
        
        if (request instanceof Query q) {

            var query = parseRequest(q.getRawQuery());
            return databaseEngine.execute(query);
        }
        return null;
    }
}
