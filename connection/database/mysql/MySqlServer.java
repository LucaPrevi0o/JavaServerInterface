package jsi.connection.database.mysql;

import jsi.Request;
import jsi.connection.database.DatabaseEngine;
import jsi.connection.database.DatabaseServer;
import jsi.connection.database.mysql.query.MySqlQuery;
import jsi.connection.database.query.QueryResult;

public class MySqlServer extends DatabaseServer {
    
    /**
     * Constructor for MySqlServer.
     * @param port the port number
     * @param databaseEngine the DatabaseEngine instance
     */
    public MySqlServer(int port, DatabaseEngine databaseEngine) { super(port, databaseEngine); }

    /**
     * Handle incoming requests and execute MySQL queries.
     * @param request the incoming Request
     * @return the QueryResult from executing the query
     * @throws IllegalArgumentException if the request is not a MySqlQuery
     */
    @Override
    public QueryResult handleRequest(Request request) {

        if (request instanceof MySqlQuery mySqlQuery) return super.handleRequest(mySqlQuery);
        throw new IllegalArgumentException("Invalid request type: Expected MySqlQuery.");
    }

    @Override
    protected Request parseRequest(String input) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'parseRequest'");
    }
}
