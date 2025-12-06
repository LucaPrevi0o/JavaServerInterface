TODO - remake from scratch **a lot** of times during development

# Java Server Interface

A highly modular, protocol-agnostic framework for building custom server implementations in Java. From HTTP web servers to database engines, build anything that listens on a socket.

## Philosophy

Java Server Interface is built on a simple principle: **separate concerns through abstraction layers**. Instead of providing a monolithic server solution, the framework offers building blocks that you can compose, extend, or replace based on your needs.

## Core Concepts

### Layered Architecture

The framework follows a three-layer abstraction model:

```
┌─────────────────────────────────────┐
│  Layer 3: Protocol                  │  ← Your application logic (HTTP, Database, Custom)
├─────────────────────────────────────┤
│  Layer 2: Transport                 │  ← Connection handling (TCP, UDP, etc.)
├─────────────────────────────────────┤
│  Layer 1: Generic Server            │  ← Lifecycle management (start, stop, hooks)
└─────────────────────────────────────┘
```

**Each layer is optional.** You can:
- Use all three layers for maximum convenience
- Skip layers to implement your own transport or protocol
- Mix and match: use our HTTP implementation but write custom transport
- Start from scratch with just the `Server` base class

### Modularity by Design

Every component is designed to be:
- **Composable**: Combine building blocks in any configuration
- **Replaceable**: Swap implementations without touching other layers
- **Extensible**: Add features through inheritance or composition
- **Protocol-agnostic**: No assumptions about what you're building

## Features

### Framework Capabilities

- **Pure Java**: Zero external dependencies, runs on any JVM
- **Protocol-Agnostic**: Build HTTP, database, game, or custom protocol servers
- **Flexible Threading**: Default thread-per-client model, easily customizable
- **Lifecycle Hooks**: Inject custom behavior at server startup, shutdown, and connection events
- **Type-Safe**: Generics ensure compile-time safety between server and handler types

### Included Implementations

Out of the box, you get:

- **HTTP Server**: Annotation-based routing, static file serving, full HTTP/1.1 support
- **Database Server**: SQL parsing, in-memory execution, JSON persistence, transaction support
- **Request/Response Abstractions**: Extensible models for any protocol

## Quick Start

### Example 1: HTTP Server (Pre-built)

The provided HTTP server implementation is based off of the `HttpServer` class, which uses custom `@Route`-defined methods as entry points for the server routing.

Each route method implemented in the HTTP server can either redirect to a static HTML page, or build a dynamic response returned to the client.

```java
import server.connection.http.HttpServer;
import server.connection.http.Route;
import common.http.*;

public class MyWebServer extends HttpServer {
    
    public MyWebServer(int port) {
        super(port);
    }
    
    @Route(path = "/")
    public HttpResponse home(HttpRequest request) {
        return createHtmlResponse(HttpResponseType.OK, 
            "<h1>Hello, World!</h1>");
    }
    
    @Route(path = "/about", staticResource = "static/about.html")
    public HttpResponse about(HttpRequest request) {
        return null; // File served automatically
    }
    
    public static void main(String[] args) {
        new MyWebServer(8080).start();
    }
}
```

### Example 2: Database Server (Pre-built)

A database server is nothing more that something listening for requests of data.
Its actual implementation - *the database engine, the storage engine, the query parsing system* - is completely hidden away.
The `DatabaseServer` instance just needs to know which kind of server is going to use - a **MySQL** server running on a **JSON**-based persistent database, a **NoSQL** server running on **XML** files... anything is possible.

```java
import server.connection.database.*;
import server.connection.database.mysql.*;
import server.connection.database.json.JsonStorageEngine;

public class Main {
    public static void main(String[] args) throws Exception {
        // Create storage engine (optional - can use in-memory)
        StorageEngine storage = new JsonStorageEngine();
        
        // Create database engine
        DatabaseEngine engine = new MySqlDatabaseEngine(storage);
        
        // Start server
        DatabaseServer server = new MySqlDatabaseServer(3306, engine, storage);
        server.start();
        
        // Client can now connect and send SQL:
        // CREATE TABLE users (id, name, email)
        // INSERT INTO users VALUES ('1', 'Alice', 'alice@example.com')
        // SELECT * FROM users WHERE name = 'Alice'
    }
}
```

### Example 4: Custom TCP Protocol

If you need a customizable server accessible from a static address/port combination, don't worry.
Just extend `ConnectionServer` for TCP-based protocols:

```java
import server.connection.ConnectionServer;
import server.connection.ClientHandler;
import java.net.Socket;

public class MyProtocolServer extends ConnectionServer<MyClientHandler> {
    
    public MyProtocolServer(int port) {
        super(port);
    }
    
    @Override
    protected MyClientHandler createClientHandler(Socket clientSocket) {
        return new MyClientHandler(clientSocket);
    }
}

class MyClientHandler extends ClientHandler {
    // Implement parseRequest() and createResponse()
    // Implement run() for connection lifecycle
}
```

### Example 3: Custom Protocol Server

If you need a fully-customizable architecture, based on a custom-defined protocol, you can implement it as well.
Extend `Server` directly for complete control:

```java
import server.Server;
import java.net.DatagramSocket;
import java.net.DatagramPacket;

public class UdpChatServer extends Server {
    
    public UdpChatServer(int port) {
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
                handleMessage(packet);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public void stop() {
        // Cleanup logic
    }
    
    private void handleMessage(DatagramPacket packet) {
        // Your protocol implementation
    }
}
```

## Architecture Deep Dive

### Layer 1: Generic Server (`Server.java`)

The foundation. Defines:
- Port management
- Lifecycle methods (`start()`, `stop()`)
- Hook methods (`onBeforeStart()`, `onServerStarted()`)

**No assumptions about transport or protocol.**

### Layer 2: Transport (`ConnectionServer<T>`)

Handles TCP socket connections:
- Client acceptance loop
    - Every client is handled on a separated thread using a `ClientHandler` object.
    - The actual `ClientHandler` is the runnable instance, providing the request/response workflow.
- Thread-per-client model (customizable)
- Generic file I/O utilities
- Type-safe handler binding

**Knows about sockets, not protocols.**

### Layer 3: Protocol Implementations

#### HTTP (`HttpServer`)
- Annotation-based routing (`@Route`)
- HTTP request parsing (method, path, headers, parameters)
- Response serialization (status, headers, body)
- Static file serving
- Extensible error handling

#### Database (`DatabaseServer`)
- Customizable request parsing for different DBMS implementations
- Separation between the `DatabaseEngine`, `StorageEngine` and `DatabaseClientHandler`
    - The `DatabaseEngine` is the actual runner of the server: it decides which kind of requests are accepted, and how they should be formatted.
    - The `StorageEngine` decides how the data is stored with persistence on the server, how to access it in a safe way and how to parse back the response for the client.
    - The `DatabaseClientHandler` is the thread which runs for every client asking for a query to be executed on the server.

## Extensibility Points

### 1. Custom Protocols

Implement your own protocol by extending `ConnectionServer`:

```java
public class WebSocketServer extends ConnectionServer<WebSocketHandler> {
    @Override
    protected WebSocketHandler createClientHandler(Socket socket) {
        return new WebSocketHandler(socket);
    }
}
```

### 2. Custom Storage Engines

Implement `StorageEngine` for different persistence backends:

```java
public class PostgresStorageEngine implements StorageEngine {
    @Override
    public void saveTable(String tableName, List<Map<String, Object>> rows) {
        // Save to PostgreSQL
    }
    
    @Override
    public List<Map<String, Object>> loadTable(String tableName) {
        // Load from PostgreSQL
    }
    
    // ... implement other methods
}
```

### 3. Custom Threading Models

Override `onClientConnected()` for connection pooling:

```java
public class PooledServer extends ConnectionServer<MyHandler> {
    private ExecutorService pool = Executors.newFixedThreadPool(10);
    
    @Override
    protected void onClientConnected(MyHandler handler) {
        pool.submit(handler);
    }
}
```

### 4. Middleware and Request Processing

Extend handlers to add middleware:

```java
public class LoggingHttpHandler extends HttpClientHandler {
    @Override
    protected HttpResponse createResponse(Request request) {
        log.info("Request: " + request);
        HttpResponse response = super.createResponse(request);
        log.info("Response: " + response.getResponseType());
        return response;
    }
}
```

## Project Structure

```
├── common/                      # Protocol-agnostic abstractions
│   ├── Request.java             # Base request interface
│   ├── Response.java            # Base response interface
│   ├── http/                    # HTTP-specific implementations
│   └── request/, response/      # Component interfaces
│
├── server/
│   ├── Server.java              # Layer 1: Generic server base
│   └── connection/
│       ├── ConnectionServer.java    # Layer 2: TCP transport
│       ├── ClientHandler.java       # Connection handler interface
│       │
│       ├── http/                    # Layer 3: HTTP protocol
│       │   ├── HttpServer.java
│       │   ├── HttpClientHandler.java
│       │   └── Route.java           # Routing annotation
│       │
│       └── database/                # Layer 3: Database protocol
│           ├── DatabaseServer.java
│           ├── DatabaseEngine.java   # Query execution interface
│           ├── StorageEngine.java    # Persistence interface
│           ├── mysql/                # MySQL-compatible implementation
│           └── json/                 # JSON storage backend
│
├── jmake                        # Build script (compile, run, jar)
├── .gitignore
├── LICENSE-CC-BY-NC-SA
└── README.md
```

## Building and Running

The project includes a simple build script (`jmake`) that works without external dependencies:

```bash
# Compile all sources
./jmake

# Compile and run (default Main class)
./jmake run

# Compile and run with custom main class
./jmake run -n MyServer

# Build executable JAR
./jmake jar

# Clean build artifacts
./jmake clean
```

## Design Principles

### 1. Separation of Concerns
Each layer handles one responsibility. Transport doesn't know about protocols. Protocols don't know about sockets.

### 2. Open/Closed Principle
Open for extension, closed for modification. Extend classes without changing the framework.

### 3. Dependency Inversion
High-level modules (protocols) don't depend on low-level modules (transport). Both depend on abstractions.

### 4. Interface Segregation
Small, focused interfaces (`StorageEngine`, `DatabaseEngine`) instead of monolithic ones.

### 5. Liskov Substitution
Any `ConnectionServer` implementation can replace another without breaking client code.

## Use Cases

### Web Development
Build REST APIs, static file servers, or full web applications with annotation-based routing.

### Database Systems
Create in-memory databases, caching layers, or protocol adapters for existing databases.

### Game Servers
Implement custom binary protocols for real-time multiplayer games.

### IoT and Embedded
Build lightweight protocol servers for device communication.

### Education
Learn server architecture, network programming, and design patterns through hands-on implementation.

## Limitations and Trade-offs

This is an **educational and prototyping framework**, not a production-ready solution:

- **No async I/O**: Uses blocking I/O with thread-per-client model
- **Basic HTTP**: No HTTP/2, WebSocket upgrade, or compression
- **Simple SQL**: Regex-based parsing, limited operator support
- **No security**: No TLS/SSL, authentication, or rate limiting
- **In-memory first**: Database persistence is optional and basic
- **Limited scalability**: Thread-per-client model doesn't scale to thousands of connections

For production use, consider frameworks like Netty (async I/O), Jetty (HTTP), or H2 (SQL).

## Performance Considerations

- **Threading**: Default thread-per-client. Consider pooling for high connection counts.
- **I/O**: Blocking sockets. For high throughput, implement async I/O layer.
- **Parsing**: Regex-based SQL parsing. For complex queries, use a proper parser generator.
- **Storage**: JSON serialization is simple but slow. Implement binary formats for performance.

## Contributing

Contributions are welcome! This framework is designed to be:
- **Easy to understand**: Clear abstractions, minimal magic
- **Easy to extend**: Add new protocols, storage engines, or features
- **Easy to modify**: Change any layer without breaking others

## License

This project is licensed under the **Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International License (CC BY-NC-SA 4.0)**.

### What this means:

**You can:**
- Use the framework for personal, educational, or non-commercial projects
- Modify and extend the code
- Share your modifications with others

**You cannot:**
- Use the framework for commercial purposes without permission
- Distribute modified versions under a different license

**You must:**
- Give appropriate credit to the original author
- Indicate if changes were made
- Distribute your modifications under the same CC BY-NC-SA 4.0 license

For the full license text, see the [LICENSE-CC-BY-NC-SA](LICENSE-CC-BY-NC-SA) file.

## Support and Community

- **Issues**: Report bugs or request features via GitHub Issues
- **Discussions**: Ask questions or share your implementations
- **Documentation**: JavaDoc comments throughout the codebase

## Acknowledgments

Built with **love** as a learning resource for understanding server architecture, network programming, and software design patterns.

---

**Ready to build?** Start with the examples above, explore the codebase, and extend the framework to create your own server implementations!