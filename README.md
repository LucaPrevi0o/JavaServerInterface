# Java Server Interface (JSI)

A highly modular, protocol-agnostic framework for building custom server implementations in Java. From HTTP web servers to database engines, build anything that listens on a socket.

> **[Read the Complete Wiki](../../wiki)** for in-depth documentation, examples, and guides.

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

> **Learn more:** [Architecture Overview](../../wiki/Architecture-Overview) | [Core Abstractions](../../wiki/Core-Abstractions)

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
- **Lifecycle Hooks**: Inject custom behavior at server startup and other key points
- **Layered Architecture**: Use all layers or skip to any level for maximum control

### Included Implementations

- **HTTP Server** ([docs](../../wiki/HTTP-Server)): Annotation-based routing, static file serving, HTTP/1.1 support
- **Database Server** ([docs](../../wiki/Database-Server)): Query execution, JSON/XML storage, MySQL-compatible parsing
- **Request/Response Abstractions** ([docs](../../wiki/Core-Abstractions)): Extensible models for any protocol

## Project Structure

### Example 1: HTTP Server

Build a web server with annotation-based routing:

```java
import com.lucaprevioo.jsi.connection.http.*;
import com.lucaprevioo.jsi.connection.http.response.HttpResponseType;

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

> **Learn more:** [HTTP Server Documentation](../../wiki/HTTP-Server)

### Example 2: Database Server

Create a database server with JSON persistence:

```java
import com.lucaprevioo.jsi.connection.database.*;
import com.lucaprevioo.jsi.connection.database.json.JsonStorageEngine;
import com.lucaprevioo.jsi.connection.database.mysql.MySqlServer;

public class Main {
    public static void main(String[] args) throws Exception {
        // Create storage engine (JSON or XML)
        StorageEngine storage = new JsonStorageEngine("./database");

        // Create database engine
        DatabaseEngine engine = new DatabaseEngine(storage);

        // Start MySQL-compatible server
        DatabaseServer server = new MySqlServer(3306, engine);
        server.start();

        // Client can now connect and send SQL:
        // SELECT * FROM users WHERE name = 'Alice'
        // INSERT INTO users (id, name) VALUES ('1', 'Alice')
    }
}
```

> **Learn more:** [Database Server Documentation](../../wiki/Database-Server)

### Example 3: Custom Protocol Server

Extend `ConnectionServer` for TCP-based custom protocols:

```java
import com.lucaprevioo.jsi.Request;
import com.lucaprevioo.jsi.Response;
import com.lucaprevioo.jsi.connection.ConnectionServer;

public class EchoServer extends ConnectionServer {

    public EchoServer(int port) {
        super(port);
    }

    @Override
    protected Request parseRequest(String input) {
        return new EchoRequest(input);
    }

    @Override
    public Response handleRequest(Request request) {
        String message = ((EchoRequest) request).getMessage();
        return new EchoResponse("ECHO: " + message);
    }

    public static void main(String[] args) {
        new EchoServer(9000).start();
    }
}
```

> **Learn more:** [ConnectionServer Documentation](../../wiki/ConnectionServer)

## Documentation

### Wiki Pages

- **[Home](../../wiki/Home)** - Wiki overview and navigation
- **[Getting Started](../../wiki/Getting-Started)** - Installation and quick start
- **[Architecture Overview](../../wiki/Architecture-Overview)** - Layered design philosophy
- **[Core Abstractions](../../wiki/Core-Abstractions)** - Server, Client, Request, Response
- **[ConnectionServer](../../wiki/ConnectionServer)** - TCP transport layer
- **[HTTP Server](../../wiki/HTTP-Server)** - HTTP protocol implementation
- **[Database Server](../../wiki/Database-Server)** - Database query execution

### Quick Links

- New to JSI? → [Getting Started](../../wiki/Getting-Started)
- Want to understand the design? → [Architecture Overview](../../wiki/Architecture-Overview)
- Building an HTTP server? → [HTTP Server Guide](../../wiki/HTTP-Server)
- Building a database? → [Database Server Guide](../../wiki/Database-Server)

## Architecture Overview

### Three-Layer Model

```
┌─────────────────────────────────────┐
│  Layer 3: Protocol                  │  ← HttpServer, DatabaseServer
│  (Application Logic)                │     Your custom protocol
├─────────────────────────────────────┤
│  Layer 2: Transport                 │  ← ConnectionServer
│  (TCP Socket Management)            │     Thread-per-client
├─────────────────────────────────────┤
│  Layer 1: Foundation                │  ← Server (abstract base)
│  (Lifecycle & Hooks)                │     start(), stop()
└─────────────────────────────────────┘
```

Each layer can be used independently or combined for maximum flexibility.

> **Deep dive:** [Architecture Overview](../../wiki/Architecture-Overview)

## Key Features

### Framework Capabilities

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

> **Full documentation:** [Getting Started Guide](../../wiki/Getting-Started)

## Design Principles

JSI exemplifies SOLID design principles:

- **Separation of Concerns**: Each layer handles one responsibility
- **Open/Closed Principle**: Open for extension, closed for modification
- **Dependency Inversion**: High-level modules depend on abstractions, not implementations
- **Interface Segregation**: Small, focused interfaces instead of monolithic ones
- **Liskov Substitution**: Any implementation can replace another without breaking code

> **Deep dive:** [Architecture Overview](../../wiki/Architecture-Overview)

## Use Cases & Examples

- **Web Development**: REST APIs, static file servers, full web applications ([HTTP Server](../../wiki/HTTP-Server))
- **Database Systems**: In-memory databases, caching layers, protocol adapters ([Database Server](../../wiki/Database-Server))
- **Custom Protocols**: Game servers, IoT communication, messaging systems ([ConnectionServer](../../wiki/ConnectionServer))
- **Education**: Learn server architecture, network programming, design patterns

## Limitations

This is an **educational and prototyping framework**, not production-ready:

- No async I/O (uses blocking I/O with thread-per-client)
- No HTTP/2, WebSocket, or compression
- Basic SQL parsing (regex-based, limited operators)
- No security (TLS/SSL, authentication, rate limiting)
- Limited scalability (thread-per-client doesn't scale to 10,000+ connections)

For production, consider: **Netty** (async I/O), **Jetty** (HTTP), **H2** (SQL)

## Contributing & Community

Contributions are welcome! This framework is designed to be:
- **Easy to understand**: Clear abstractions, minimal magic
- **Easy to extend**: Add new protocols, storage engines, or features
- **Easy to modify**: Change any layer without breaking others

**Resources:**
- [Complete Wiki Documentation](../../wiki)
- [Report Issues](../../issues)
- [Discussions](../../discussions)
- JavaDoc comments throughout the codebase

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

---

## Ready to Start?

1. **New to JSI?** → [Getting Started Guide](../../wiki/Getting-Started)
2. **Understand the design?** → [Architecture Overview](../../wiki/Architecture-Overview)
3. **Build an HTTP server?** → [HTTP Server Guide](../../wiki/HTTP-Server)
4. **Build a database?** → [Database Server Guide](../../wiki/Database-Server)
5. **Explore the code?** → Browse the [source files](.)

**Complete documentation:** [Wiki Home](../../wiki)