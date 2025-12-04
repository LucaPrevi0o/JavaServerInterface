# Java Server Interface

A lightweight, annotation-based framework for building HTTP servers in Java with custom client/server architecture support.

## Overview

Java Server Interface is a minimalist web framework that provides:
- Simple HTTP server implementation with routing
- Annotation-based route mapping (`@Route`)
- Static file serving
- Custom request/response handling
- Extensible client-server architecture

## Features

- **Annotation-Based Routing**: Use `@Route` annotations to map URL paths to handler methods
- **Static Resource Serving**: Easily serve HTML, CSS, and other static files
- **HTTP Protocol Support**: Built-in HTTP request/response handling
- **Extensible Architecture**: Abstract base classes for custom server implementations
- **Lightweight**: No external dependencies, pure Java implementation

## Project Structure

```
├── server/          # Server base classes
│   └── http/        # HTTP server and routing
├── common/          # Shared request/response classes
│   ├── http/        # HTTP-specific implementations
│   ├── request/     # Request handling
│   └── response/    # Response handling
```

## Quick Start

### Creating an HTTP Server

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

### Running the Server

Compile and run your server:

```bash
javac -d java MyServer.java
java -cp java MyServer
```

Visit `http://localhost:8080` in your browser.

## Usage

### Route Annotation

The `@Route` annotation supports:
- `path`: URL path to handle (e.g., "/", "/about", "/api/users")
- `staticResource`: Optional path to serve static files

### Request/Response

The framework provides HTTP-specific classes:
- `HttpRequest`: Parse and access HTTP request data
- `HttpResponse`: Build HTTP responses with headers and body
- `HttpRequestType`: HTTP methods (GET, POST, etc.)
- `HttpResponseType`: HTTP status codes

## License

This project is open source and available under the MIT License.

## Contributing

Contributions are welcome! Feel free to submit issues or pull requests. 