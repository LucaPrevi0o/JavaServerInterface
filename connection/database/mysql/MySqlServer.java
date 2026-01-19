package com.lucaprevioo.jsi.connection.database.mysql;

import com.lucaprevioo.jsi.Request;
import com.lucaprevioo.jsi.connection.database.DatabaseEngine;
import com.lucaprevioo.jsi.connection.database.DatabaseServer;
import com.lucaprevioo.jsi.connection.database.mysql.query.MySqlQuery;
import com.lucaprevioo.jsi.connection.database.query.QueryResult;

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

    /**
     * Parse the incoming request string into a MySqlQuery.
     * @param input the raw request string
     * @return the parsed MySqlQuery
     */
    @Override
    protected Request parseRequest(String input) { return new MySqlQuery(input); }
}
