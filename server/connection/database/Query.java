package server.connection.database;

public abstract class Query {

    private QueryType type;

    /**
     * Get the type of the query.
     * @return the QueryType
     */
    public QueryType getType() { return type; }

    /**
     * Constructor for Query.
     * @param type the QueryType
     */
    public Query(QueryType type) { this.type = type; }

    /**
     * Execute the query using the provided DatabaseEngine.
     * @param databaseEngine the DatabaseEngine to use
     * @return the QueryResult
     */
    public abstract QueryResult execute(DatabaseEngine databaseEngine);
}
