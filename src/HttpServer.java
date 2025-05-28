import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.*;
import java.nio.file.attribute.FileTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class HttpServer {

    private final int port;

    public HttpServer(int port) {
        this.port = port;
    }

    public void serverEstablishment() throws IOException {
        ServerSocket serverSocket = new ServerSocket(port);
        System.out.println("🚀 Server started on port " + port);

        while (true) {
            Socket clientSocket = serverSocket.accept();
            new Thread(() -> {
                System.out.println(" Client connected: " + clientSocket.getInetAddress() +
                        "  Thread: " + Thread.currentThread().getName());
                try {
                    handleClient(clientSocket);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }

    private void send404Response(Socket clientSocket) throws IOException {
        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
        String errorMsg = "<h1>404 Not Found</h1>";
        out.write("HTTP/1.1 404 Not Found\r\n");
        out.write("Content-Type: text/html\r\n");
        out.write("Content-Length: " + errorMsg.length() + "\r\n");
        out.write("\r\n");
        out.write(errorMsg);
        out.flush();
        clientSocket.close();
    }

    private void send405Response(Socket clientSocket) throws IOException {
        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
        String errorMsg = "<h1>405 Method Not Allowed</h1>";
        out.write("HTTP/1.1 405 Method Not Allowed\r\n");
        out.write("Content-Type: text/html\r\n");
        out.write("Content-Length: " + errorMsg.length() + "\r\n");
        out.write("\r\n");
        out.write(errorMsg);
        out.flush();
        clientSocket.close();
    }

    private void sendFileResponse(Socket clientSocket, File file, String contentType) throws IOException {
        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
        byte[] content = Files.readAllBytes(file.toPath());
        out.write("HTTP/1.1 200 OK\r\n");
        out.write("Content-Type: " + contentType + "\r\n");
        out.write("Content-Length: " + content.length + "\r\n");

        FileTime lastModifiedTime = Files.getLastModifiedTime(file.toPath());
        String lastModifiedStr = DateTimeFormatter.RFC_1123_DATE_TIME.format(
                ZonedDateTime.ofInstant(lastModifiedTime.toInstant(), ZoneId.systemDefault())
        );
        out.write("Last-Modified: " + lastModifiedStr + "\r\n");
        out.write("\r\n");
        out.flush();

        clientSocket.getOutputStream().write(content);
        clientSocket.getOutputStream().flush();
    }

    private void checkFav(Socket clientSocket, String path) throws IOException {
        if (path.equals("/favicon.ico")) {
            File favicon = new File("src/Public/favicon.ico");
            if (favicon.exists() && !favicon.isDirectory()) {
                sendFileResponse(clientSocket, favicon, "image/x-icon");
                System.out.println(" Sent favicon.ico");
            } else {
                send404Response(clientSocket);
                System.out.println(" favicon.ico not found");
            }
        }
    }

    private void handleGetRequest(Socket clientSocket, String path) throws IOException {
        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));

        if (path.equals("/favicon.ico")) {
            checkFav(clientSocket, path);
            return;
        }

        if (path.equals("/")) {
            path = "/Public/index.html";
        } else if (!path.contains(".")) {
            path += ".html";
        }

        File file = new File("src/Public" + path);
        System.out.println(" Looking for file: " + file.getPath());

        if (file.exists() && !file.isDirectory()) {
            String mimeType = Files.probeContentType(file.toPath());
            if (mimeType == null) {
                if (file.getName().endsWith(".css")) mimeType = "text/css";
                else if (file.getName().endsWith(".js")) mimeType = "application/javascript";
                else if (file.getName().endsWith(".html")) mimeType = "text/html";
                else if (file.getName().endsWith(".png")) mimeType = "image/png";
                else if (file.getName().endsWith(".jpg") || file.getName().endsWith(".jpeg")) mimeType = "image/jpeg";
                else mimeType = "application/octet-stream";
            }

            sendFileResponse(clientSocket, file, mimeType);
        } else {
            send404Response(clientSocket);
        }
    }

    private void handlePostRequest(Socket clientSocket, BufferedReader in) throws IOException {
        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));

        String line;
        while ((line = in.readLine()) != null && !line.isEmpty());

        StringBuilder body = new StringBuilder();
        while (in.ready() && (line = in.readLine()) != null) {
            body.append(line).append("\n");
        }

        FileWriter fw = new FileWriter("src/Public/data.txt", true);
        fw.write(body.toString());
        fw.close();

        out.write("HTTP/1.1 201 Created\r\n\r\n");
        out.flush();
        clientSocket.close();
    }

    private void handlePutRequest(Socket clientSocket, BufferedReader in) throws IOException {
        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));

        String line;
        while ((line = in.readLine()) != null && !line.isEmpty());

        StringBuilder body = new StringBuilder();
        while (in.ready() && (line = in.readLine()) != null) {
            body.append(line).append("\n");
        }

        FileWriter fw = new FileWriter("src/Public/data.txt", false); // Overwrite existing data
        fw.write(body.toString());
        fw.close();

        out.write("HTTP/1.1 200 OK\r\n\r\n");
        out.flush();
        clientSocket.close();
    }

    private void handleDeleteRequest(Socket clientSocket) throws IOException {
        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
        File file = new File("src/Public/data.txt");

        if (file.exists()) {
            file.delete();
            out.write("HTTP/1.1 200 OK\r\n\r\n");
            out.write("File deleted.\r\n");
        } else {
            out.write("HTTP/1.1 404 Not Found\r\n\r\n");
            out.write("File not found.\r\n");
        }

        out.flush();
        clientSocket.close();
    }

    private void handleClient(Socket clientSocket) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        String requestLine = in.readLine();

        if (requestLine == null || requestLine.trim().isEmpty()) {
            System.out.println("Empty or null request received. Closing connection.");
            clientSocket.close();
            return;
        }

        System.out.println(" Request: " + requestLine);

        String[] tokens = requestLine.split(" ");
        if (tokens.length < 2) {
            clientSocket.close();
            return;
        }

        String method = tokens[0];
        String path = tokens[1];

        switch (method) {
            case "GET":
                handleGetRequest(clientSocket, path);
                break;
            case "POST":
                handlePostRequest(clientSocket, in);
                break;
            case "PUT":
                handlePutRequest(clientSocket, in);
                break;
            case "DELETE":
                handleDeleteRequest(clientSocket);
                break;
            default:
                send405Response(clientSocket);
        }
    }
}

