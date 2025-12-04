package server.connection;

import java.net.Socket;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import common.Request;
import common.Response;

public abstract class ClientHandler implements Runnable {

    private final Socket clientSocket;

    /**
     * Constructor for ClientHandler.
     * @param clientSocket the socket connected to the client
     */
    public ClientHandler(Socket clientSocket) { this.clientSocket = clientSocket; }
    
    @Override
    public void run() {
        
        try (var in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), StandardCharsets.UTF_8));
             var out = new PrintWriter(clientSocket.getOutputStream(), true, StandardCharsets.UTF_8)) {
            
            // Read all input from client
            var requestBuilder = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null && !line.isEmpty())
                requestBuilder.append(line).append("\r\n");
            
            var fullRequest = requestBuilder.toString();
            
            // Parse the request
            var request = parseRequest(fullRequest);

            // Create and send response
            var response = createResponse(request);
            out.print(response.serialize());
            out.flush();
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Parse the incoming request string into a Request object.
     * @param input the input string representing the request
     * @return the parsed Request object
     */
    protected abstract Request parseRequest(String input);

    /**
     * Create a Response object based on the given Request.
     * @param request the Request object
     * @return the created Response object
     */
    protected abstract Response createResponse(Request request);
}
