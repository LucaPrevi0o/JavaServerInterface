package jsi;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public abstract class ConnectionClient extends Client {
    
    private String host;
    private int port;

    public ConnectionClient(String host, int port) {

        this.host = host;
        this.port = port;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public Response getResponse(Request request) {
        
        try {

            var socket = new Socket(host, port);
            try (
                var in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                var out = new PrintWriter(socket.getOutputStream())
            ) {

                // Send the request as a string (serialize it)
                String requestString = serializeRequest(request);
                out.println(requestString);
                out.flush();

                var responseString = in.readLine();
                return parseResponse(responseString);
            } finally {
                socket.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Serialize the request to a string to send to the server.
     * Default implementation calls toString(), but can be overridden.
     * @param request the request to serialize
     * @return the serialized request string
     */
    protected String serializeRequest(Request request) {
        return request.toString();
    }

    public abstract Response parseResponse(String input);
}
