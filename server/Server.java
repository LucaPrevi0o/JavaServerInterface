package server;

import common.Response;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;

/**
 * Abstract server class providing basic server functionalities.
 * 
 * Every Server implementation is linked to a specific ClientHandler.
 * It is used to manage client connections and handle requests.
 */
public abstract class Server<T extends ClientHandler> {

    private int port;

    /**
     * Get the port number on which the server is listening.
     * @return the port number
     */
    public int getPort() { return port; }

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

                var clientSocket = socket.accept();
                var handler = createClientHandler(clientSocket);
                onClientConnected(handler);
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

    /**
     * Create a ClientHandler instance for the connected client.
     * @param clientSocket the socket connected to the client
     * @return the ClientHandler instance
     */
    protected abstract T createClientHandler(Socket clientSocket);

    /**
     * Hook method called before the server starts.
     * 
     * This method can be overridden by subclasses to perform any setup or initialization
     * tasks before the server begins accepting connections. It is empty by default.
     */
    protected void onBeforeStart() {}

    /**
     * Hook method called after the server has started.
     * 
     * This method can be overridden by subclasses to perform any actions
     * after the server has started. It is empty by default.
     */
    protected void onServerStarted() {}

    /**
     * Hook method called when a client connects.
     * 
     * This method can be overridden by subclasses to perform any actions
     * when a client connects to the server. It is empty by default.
     */
    protected void onClientConnected(T clientHandler) { new Thread(clientHandler).start(); }

    /**
     * Constructor for Server.
     * @param port The port number on which the server will listen.
     */
    public Server(int port) { this.port = port; }

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

    /**
     * Read a file from the filesystem and create a protocol-specific response.
     * Subclasses must implement this to create appropriate responses for their protocol.
     * 
     * @param filePath the path to the file (relative or absolute)
     * @param contentType the MIME type of the file
     * @return the protocol-specific response
     */
    protected abstract Response serveFile(String filePath, String contentType);
}