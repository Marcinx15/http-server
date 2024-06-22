import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class Main {
  public static void main(String[] args) {
      ExecutorService executorService = Executors.newFixedThreadPool(64);
      try (ServerSocket serverSocket = new ServerSocket(4221)) {
          serverSocket.setReuseAddress(true);
          while (true) {
              Socket clientSocket = serverSocket.accept();
              executorService.submit(() -> {
                  try {
                      handleSingleConnection(clientSocket);
                  } catch (IOException e) {
                      System.out.println("IOException: " + e.getMessage());
                  }
              });
          }
     } catch (IOException e) {
          System.out.println("IOException: " + e.getMessage());
     }
  }

    private static void handleSingleConnection(Socket clientSocket) throws IOException {
        System.out.println("accepted new connection");
        HttpRequest request = readRequest(clientSocket.getInputStream());
        HttpResponse response = buildResponse(request);
        clientSocket.getOutputStream().write(response.toBytes());
    }

    private static HttpRequest readRequest(InputStream inputStream) throws IOException {
        BufferedReader requestReader = new BufferedReader(new InputStreamReader(inputStream));
        return HttpRequest.fromRequestLines(readAllLines(requestReader));
    }

    private static List<String> readAllLines(BufferedReader requestReader) throws IOException {
        String line;
        List<String> lines = new ArrayList<>();
        while (!(line = requestReader.readLine()).isEmpty())
            lines.add(line);
        return lines;
    }

    private static HttpResponse buildResponse(HttpRequest request) {
        String[] pathParts = request.path.substring(1).split("/");
        if (request.path.equals("/")) {
            return new HttpResponse(HttpStatusCode.OK, Collections.emptyMap(), null);
        } else if (request.path.equals("/user-agent")) {
            return userAgentResponse(request.headers.get("user-agent"));
        } else if (pathParts.length == 2 && pathParts[0].equals("echo")) {
            return echoResponse(pathParts[1]);
        } else {
            return new HttpResponse(HttpStatusCode.NOT_FOUND, Collections.emptyMap(), null);
        }
    }

    private static HttpResponse echoResponse(String echoValue) {
        return new HttpResponse(
                HttpStatusCode.OK,
                Map.of(
                        "Content-Type", "text/plain",
                        "Content-Length", String.valueOf(echoValue.length())
                ),
                echoValue
        );
    }

    private static HttpResponse userAgentResponse(String userAgentValue) {
        return new HttpResponse(
                HttpStatusCode.OK,
                Map.of(
                        "Content-Type", "text/plain",
                        "Content-Length", String.valueOf(userAgentValue.length())
                ),
                userAgentValue
        );
    }

}


