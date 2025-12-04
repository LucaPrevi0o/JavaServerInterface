# Java Server Interface

A modular, extensible framework for building custom server implementations in Java, from HTTP servers to protocol-specific network applications.

## Overview

Java Server Interface is a flexible server framework built on a highly modular architecture that allows developers to:
- Extend a generic `Server` base class for any protocol or transport layer
- Build TCP-based servers using the `ConnectionServer` implementation
- Create HTTP servers with annotation-based routing
- Implement custom protocols (UDP, WebSocket, or proprietary protocols)
- Reuse common patterns across different server types

## Features

- **Protocol-Agnostic Architecture**: Generic `Server` class decoupled from transport implementation
- **Modular Design**: Layer your server from generic → transport-specific → protocol-specific
- **TCP Support**: Built-in `ConnectionServer` for socket-based connections
- **HTTP Implementation**: Ready-to-use HTTP server with `@Route` annotations and static file serving
- **Extensibility**: Easy to implement custom protocols and transport layers
- **Lightweight**: No external dependencies, pure Java implementation

## Architecture

The framework follows a layered architecture pattern:

```
Server (abstract)
    ↓
ConnectionServer (TCP/Socket-based)
    ↓
HttpServer (HTTP protocol)
    ↓
YourCustomServer (your implementation)
```

**Base Layer - `Server`**: 
- Protocol and transport agnostic
- Defines lifecycle methods (`start()`, `stop()`)
- Provides hooks for customization (`onBeforeStart()`, `onServerStarted()`)

**Transport Layer - `ConnectionServer`**:
- TCP-based implementation using Java Sockets
- Manages client connections and threading
- Can be extended for other transport protocols (UDP, custom)

**Protocol Layer - `HttpServer`**:
- HTTP-specific request/response handling
- Annotation-based routing system
- Static file serving capabilities

## Project Structure

```
├── server/               # Server implementations
│   ├── Server.java       # Generic abstract server base
│   └── connection/       # TCP-based server implementation
│       └── http/         # HTTP protocol layer
├── common/               # Shared request/response classes
│   ├── http/             # HTTP-specific implementations
│   ├── request/          # Generic request handling
│   └── response/         # Generic response handling
└── static/               # Static resources (HTML, CSS, etc.)
```

## Quick Start

### Example 1: HTTP Server (Built-in)

```java
import server.http.HttpServer;
import server.http.Route;
import common.http.HttpRequest;
import common.http.HttpResponse;

public class MyServer extends HttpServer {
    
    public MyServer(int port) {
        super(port);
    }
    
    @Route(path = "/")
    public HttpResponse home(HttpRequest request) {
        return new HttpResponse("Welcome to my server!");
    }
    
    @Route(path = "/about", staticResource = "static/about.html")
    public HttpResponse about(HttpRequest request) {
        return null; // Static resource will be served
    }
    
    public static void main(String[] args) {
        MyServer server = new MyServer(8080);
        server.start();
    }
}
```

### Example 2: Custom Protocol Server

You can extend the generic `Server` class directly for custom protocols:

```java
public class UdpServer extends Server {
    
    public UdpServer(int port) {
        super(port);
    }
    
    @Override
    public void start() {
        onBeforeStart();
        
        try (DatagramSocket socket = new DatagramSocket(getPort())) {
            onServerStarted();
            byte[] buffer = new byte[1024];
            
            while (true) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                handlePacket(packet);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public void stop() {
        // Cleanup
    }
    
    private void handlePacket(DatagramPacket packet) {
        // Your custom protocol logic
    }
}
```

### Example 3: Custom TCP Server

Or extend `ConnectionServer` for TCP-based custom protocols:

```java
public class MyTcpServer extends ConnectionServer<MyClientHandler> {
    
    public MyTcpServer(int port) {
        super(port);
    }
    
    @Override
    protected MyClientHandler createClientHandler(Socket clientSocket) {
        return new MyClientHandler(clientSocket);
    }
}
```

## Extensibility

The framework is designed for maximum flexibility:

- **Custom Transport**: Implement `Server` directly for any transport (UDP, WebSocket, etc.)
- **Custom Protocol**: Extend `ConnectionServer` for TCP-based custom protocols  
- **Mixed Implementations**: Combine different server types in the same application
- **Lifecycle Hooks**: Override `onBeforeStart()`, `onServerStarted()`, etc. for custom behavior

## Usage

### For HTTP Servers

- Use `@Route` annotations to map URL paths to handler methods
- Serve static files with the `staticResource` parameter
- Access request/response through `HttpRequest` and `HttpResponse` objects

### For Custom Servers

- Extend `Server` for full control over transport and protocol
- Extend `ConnectionServer<T>` for TCP-based servers with client handlers
- Implement your own request/response classes following the `common` package patterns

## License

This project is open source and available under the MIT License.

## Contributing

Contributions are welcome! Feel free to submit issues or pull requests. 