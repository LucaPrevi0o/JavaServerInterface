package jsi.connection.database;

import jsi.Request;
import jsi.connection.ConnectionServer;

/**
 * Abstract class representing a database server.
 * Extends ConnectionServer to handle database client connections.
 */
public abstract class DatabaseServer extends ConnectionServer {

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
     * Handle incoming requests and execute database queries.
     * @param query the incoming Request
     * @return the QueryResult from executing the query
     */
    @Override
    public QueryResult handleRequest(Request query) {

        if (query instanceof Query q) return databaseEngine.execute(q);
        return null;
    }
}
