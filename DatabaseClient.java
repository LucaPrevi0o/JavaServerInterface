package jsi;
import jsi.connection.database.Query;
import jsi.connection.database.QueryResult;

public abstract class DatabaseClient extends ConnectionClient {
    
    public DatabaseClient(String host, int port) {
        super(host, port);
    }

    /**
     * Execute a query on the database server.
     * @param query the Query object to execute
     * @return the QueryResult from the server
     */
    public QueryResult executeQuery(Query query) {
        return (QueryResult) getResponse(query);
    }

    /**
     * Execute a raw query string on the database server.
     * @param queryString the SQL query string
     * @return the QueryResult from the server
     */
    public abstract QueryResult executeQuery(String queryString);
}
