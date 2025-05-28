import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        try {
            HttpServer server = new HttpServer(8080);
            server.serverEstablishment();
        }catch (IOException e){
            System.out.println("server error "+e.getMessage());
        }
    }

}
