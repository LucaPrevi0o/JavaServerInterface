package server.connection.database;

public interface DatabaseEngine {

    /**
     * Execute a query and return a response.
     * @param query the Query to execute
     * @return the Response from the execution
     */
    QueryResult execute(Query query);

    /**
     * Begin a transaction.
     */
    void beginTransaction();

    /**
     * Commit the current transaction.
     */
    void commit();

    /**
     * Rollback the current transaction.
     */
    void rollback();

    /**
     * Get the schema of the database.
     * @return the Schema
     */
    Schema getSchema();
}