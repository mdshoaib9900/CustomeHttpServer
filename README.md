# HttpServer
# Custom HTTP Server in Java

A simple multithreaded HTTP server implemented in Java that supports handling of GET, POST, PUT, and DELETE requests.  
This project demonstrates core networking concepts, request handling, and file serving without any external frameworks.

---

## Features

- Handles HTTP methods: **GET**, **POST**, **PUT**, and **DELETE**
- Serves static files such as HTML, CSS, JS, and images from a public directory
- Reads and writes data to a file (`data.txt`) for POST and PUT requests
- Multithreaded: handles multiple client connections concurrently
- Returns appropriate HTTP status codes and response headers
- Supports common MIME types and last-modified headers
- Handles `/favicon.ico` requests gracefully

---

## Prerequisites

- Java Development Kit (JDK) 8 or higher installed
- Git (optional, for cloning the repository)
- An HTTP client such as a web browser, [Postman](https://www.postman.com/), or `curl` for testing

---

## Getting Started

### Clone the Repository

```bash
git clone https://github.com/mdshoaib9900/CustomeHttpServer.git
cd CustomeHttpServer

Author
Mohammed Shoaib

License
This project is licensed under the MIT License. See the LICENSE file for details.
