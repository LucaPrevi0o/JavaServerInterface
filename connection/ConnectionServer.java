package jsi.connection;

import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import jsi.Request;
import jsi.Server;

import java.nio.charset.StandardCharsets;

/**
 * Abstract server class providing basic server functionalities.
 * 
 * Every Server implementation is linked to a specific ClientHandler.
 * It is used to manage client connections and handle requests.
 */
public abstract class ConnectionServer extends Server {

    private final int port;

    /**
     * Start the server to listen for incoming connections.
     */
    public void start() {
        
        System.out.println("Server is starting...");
        onBeforeStart();

        try (var socket = new ServerSocket(port)) {

            System.out.println("Server started on port " + port);
            onServerStarted();
            while (true) {

                try (
                    var clientSocket = socket.accept();
                    var in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), StandardCharsets.UTF_8));
                    var out = new PrintWriter(clientSocket.getOutputStream())
                ) {
                
                    new Thread(() -> {

                        try {
                            
                            var request = in.readLine();
                            var response = handleRequest(parseRequest(request));
                            out.println(response.serialize());
                            out.flush();
                        } catch (IOException e) { e.printStackTrace(); }
                    }).start();
                }
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

    /**
     * Parse the incoming request string into a Request object.
     * @param input the input string representing the request
     * @return the parsed Request object
     */
    protected abstract Request parseRequest(String input);

    /**
     * Constructor for Server.
     * @param port The port number on which the server will listen.
     */
    public ConnectionServer(int port) { this.port = port; }

    /**
     * Read a file from the filesystem and return its content.
     * This is a generic file reading method that any server implementation can use.
     * 
     * @param filePath the path to the file (relative or absolute)
     * @return the file content as a string
     * @throws IOException if the file cannot be read
     */
    protected String readFile(String filePath) throws IOException {
        
        Path path = Paths.get(filePath);
        return Files.readString(path, StandardCharsets.UTF_8);
    }
}