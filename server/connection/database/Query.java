package server.connection.database;

import common.Request;

public abstract class Query extends Request {

    /**
     * Execute the query using the provided DatabaseEngine.
     * @param databaseEngine the DatabaseEngine to use
     * @return the QueryResult
     */
    public abstract QueryResult execute(DatabaseEngine databaseEngine);
}
