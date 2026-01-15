package jsi.connection;

import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import jsi.Server;

import java.nio.charset.StandardCharsets;

/**
 * Abstract server class providing basic server functionalities.
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
                onClientConnected(clientSocket);
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

    /**
     * Hook method called when a client connects.
     * 
     * This method can be overridden by subclasses to perform any actions
     * when a client connects.
     * 
     * By default, it starts a new thread to handle the client connection.
     * When the client disconnects, it calls the {@code onClientDisconnected()} hook.
     * 
     * @param clientSocket the socket of the connected client
     */
    public void onClientConnected(Socket clientSocket) {

        System.out.println("Client connected: " + clientSocket.getInetAddress().getHostAddress() + ":" + clientSocket.getPort());
        new Thread(() -> {

            try (
                var in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), StandardCharsets.UTF_8));
                var out = new PrintWriter(clientSocket.getOutputStream())
            ) {

                var request = extractRequest(clientSocket);
                var response = handleRequest(parseRequest(request));
                out.println(response.serialize());
                out.flush();
            } catch (IOException e) { e.printStackTrace(); }
            finally {

                try { 
                    
                    clientSocket.close(); 
                    onClientDisconnected(clientSocket);
                } catch (IOException e) { e.printStackTrace(); }
            }
        }).start();
    }

    /**
     * Extract the request from the client socket.
     * 
     * Reads the input stream until the end of the headers and body based on Content-Length.
     * @param clientSocket the socket of the connected client
     * @return the extracted request as a string
     * @throws IOException if an I/O error occurs
     */
    protected String extractRequest(Socket clientSocket) throws IOException {

        var input = clientSocket.getInputStream();
        var headerBaos = new ByteArrayOutputStream();
        var ch = 0;
        var state = 0; 

        while ((ch = input.read()) != -1) {

            headerBaos.write(ch);
            if (state == 0 && ch == '\r') state = 1;
            else if (state == 1 && ch == '\n') state = 2;
            else if (state == 2 && ch == '\r') state = 3;
            else if (state == 3 && ch == '\n') { break; }
            else state = (ch == '\r') ? 1 : 0;
        }

        var headers = new String(headerBaos.toByteArray(), StandardCharsets.UTF_8);
        var contentLength = 0;
        for (var hLine : headers.split("\r\n")) if (hLine.toLowerCase().startsWith("content-length:"))
            try { contentLength = Integer.parseInt(hLine.split(":", 2)[1].trim()); }
            catch (NumberFormatException ignored) {}

        var body = new byte[0];
        if (contentLength > 0) body = input.readNBytes(contentLength);
        return headers + (body.length > 0 ? new String(body, StandardCharsets.UTF_8) : "");
    }

    /**
     * Hook method called when a client disconnects.
     * 
     * This method can be overridden by subclasses to perform any actions
     * when a client disconnects. It is empty by default.
     */
    public void onClientDisconnected(Socket clientSocket) {}

    /**
     * Constructor for ConnectionServer.
     * @param port The port number on which the server will listen.
     */
    public ConnectionServer(int port) { this.port = port; }
}