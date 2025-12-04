package server.connection;

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
public abstract class ConnectionServer<T extends ClientHandler> extends server.Server {

    /**
     * Start the server to listen for incoming connections.
     */
    public void start() {
        
        System.out.println("Server is starting...");
        onBeforeStart();

        try (var socket = new ServerSocket(getPort())) {

            System.out.println("Server started on port " + getPort());
            onServerStarted();
            while (true) {

                var clientSocket = socket.accept();
                var handler = createClientHandler(clientSocket);
                onClientConnected(handler);
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

    /**
     * Stop the server.
     */
    public void stop() {}

    /**
     * Create a ClientHandler instance for the connected client.
     * @param clientSocket the socket connected to the client
     * @return the ClientHandler instance
     */
    protected abstract T createClientHandler(Socket clientSocket);

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
    public ConnectionServer(int port) { super(port); }

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