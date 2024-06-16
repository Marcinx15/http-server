import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {
  public static void main(String[] args) {
    ServerSocket serverSocket;
    Socket clientSocket;

     try {
       serverSocket = new ServerSocket(4221);
       serverSocket.setReuseAddress(true);
       clientSocket = serverSocket.accept(); // Wait for connection from client.
       System.out.println("accepted new connection");
       HttpRequest request = readRequest(clientSocket.getInputStream());
       HttpResponse response = buildResponse(request);
       clientSocket.getOutputStream().write(response.toBytes());
     } catch (IOException e) {
       System.out.println("IOException: " + e.getMessage());
     }
  }

    private static HttpRequest readRequest(InputStream inputStream) throws IOException {
        BufferedReader requestReader = new BufferedReader(new InputStreamReader(inputStream));
        return HttpRequest.fromString(requestReader.readLine());
    }

    private static HttpResponse buildResponse(HttpRequest request) {
        return new HttpResponse(request.path.equals("/") ? HttpStatusCode.OK : HttpStatusCode.NOT_FOUND);
    }
}


