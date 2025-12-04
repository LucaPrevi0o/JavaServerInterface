package server.connection.database;

import java.net.Socket;

import common.Request;
import common.Response;
import server.connection.ClientHandler;

public class DatabaseClientHandler extends ClientHandler {
    
    /**
     * Constructor for DatabaseClientHandler.
     */
    public DatabaseClientHandler(Socket clientSocket) {
        super(clientSocket);
    }

    @Override
    protected Request parseRequest(String input) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'parseRequest'");
    }

    @Override
    protected Response createResponse(Request request) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'createResponse'");
    }
}
