package server.connection.database;

import common.Request;

/**
 * Abstract class representing a database query.
 * Extends Request to be used in client-server communication.
 */
public abstract class Query extends Request {

    /**
     * Execute the query using the provided DatabaseEngine.
     * @param databaseEngine the DatabaseEngine to use
     * @return the QueryResult
     */
    public abstract QueryResult execute(DatabaseEngine databaseEngine);
}
