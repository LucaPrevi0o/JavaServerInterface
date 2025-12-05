package server.connection.database;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import common.Request;
import server.connection.ClientHandler;

/**
 * Abstract class for handling database client connections.
 */
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
     * Get the DatabaseEngine instance.
     * @return the DatabaseEngine
     */
    protected DatabaseEngine getDatabaseEngine() { return databaseEngine; }

    /**
     * Main run loop for handling client requests.
     */
    @Override
    public void run() {

        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {
            
            String line;
            while ((line = in.readLine()) != null) {

                if (line.trim().isEmpty()) continue;
                try {

                    var query = parseRequest(line);
                    var response = query.execute(getDatabaseEngine());
                    out.println(response.serialize());
                } catch (Exception e) { out.println("ERROR: " + e.getMessage()); }
            }
        } catch (IOException e) { e.printStackTrace(); }
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
