package jsi.connection;

import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.IOException;
import java.net.ServerSocket;
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

            System.out.println("Server started on: " + socket.getInetAddress().getHostAddress() + ":" + port);
            onServerStarted();
            while (true) {

                var clientSocket = socket.accept();
                new Thread(() -> {

                    try (
                        var in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), StandardCharsets.UTF_8));
                        var out = new PrintWriter(clientSocket.getOutputStream())
                    ) {

                        var request = in.readLine();
                        var response = handleRequest(parseRequest(request));
                        out.println(response.serialize());
                        out.flush();
                    } catch (IOException e) {  e.printStackTrace(); }
                    finally {

                        try { clientSocket.close(); } 
                        catch (IOException e) { e.printStackTrace(); }
                    }
                }).start();
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
}