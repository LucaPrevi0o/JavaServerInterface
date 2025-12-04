package server.connection.database;

import java.net.Socket;

import common.Request;
import server.connection.ClientHandler;

public abstract class DatabaseClientHandler extends ClientHandler {

    protected final Socket clientSocket;
    private DatabaseEngine databaseEngine;
    
    /**
     * Constructor for DatabaseClientHandler.
     */
    public DatabaseClientHandler(Socket clientSocket, DatabaseEngine databaseEngine) {

        this.clientSocket = clientSocket;
        this.databaseEngine = databaseEngine;
    }

    /**
     * Parse the incoming request string into a Query object.
     * @param input the input string representing the request
     * @return the parsed Query object
     */
    @Override
    protected abstract Query parseRequest(String input);

    /**
     * Create a QueryResult object based on the given Query.
     * @param request the Request object
     * @return the created QueryResult object
     */
    @Override
    protected QueryResult createResponse(Request request) {
        
        var query = parseRequest(request.toString());
        return databaseEngine.execute(query);
    }
}
