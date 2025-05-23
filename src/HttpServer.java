import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class HttpServer {
    public HttpServer serverEstablishment() throws IOException {
        int port=8090;
        ServerSocket serverSocket=new ServerSocket(port);
        System.out.println("Server started on port " + port);

        while(true){
            Socket clientSocket=serverSocket.accept();
            System.out.println("client connected "+clientSocket.getInetAddress());

            BufferedReader bufferedReader=new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));


            BufferedWriter bufferedWriter=new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));

            String line;
            while(!(line=bufferedReader.readLine()).isEmpty()){
                System.out.println(line);
            }
            String response = "HTTP/1.1 200 OK\r\nContent-Type: text/plain\r\n\r\nHello from Shoaib's server!";
            bufferedWriter.write(response);
            bufferedWriter.flush();
            clientSocket.close();

        }

    }


}
