package jsi;
import jsi.connection.database.query.Query;
import jsi.connection.database.query.QueryResult;

/**
 * Abstract class representing a database client.
 * Extends ConnectionClient to handle database server communication.
 */
public abstract class DatabaseClient extends ConnectionClient {
    
    /**
     * Constructor for DatabaseClient.
     * @param host the server host
     * @param port the server port
     */
    public DatabaseClient(String host, int port) { super(host, port); }

    /**
     * Get the response for a database query.
     * @param query the Query object
     * @return the QueryResult from the server
     */
    @Override
    public abstract QueryResult getResponse(Request query);

    /**
     * Execute a query on the database server.
     * @param query the Query object to execute
     * @return the QueryResult from the server
     */
    public QueryResult executeQuery(Query query) { return getResponse(query); }
}
