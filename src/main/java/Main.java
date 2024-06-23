import javax.swing.text.html.Option;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class Main {
    private static String fileDirectory;

  public static void main(String[] args) {
      fileDirectory = (args.length >= 2 && "--directory".equals(args[0])) ? args[1] : "";
      ExecutorService executorService = Executors.newFixedThreadPool(64);
      try (ServerSocket serverSocket = new ServerSocket(4221)) {
          serverSocket.setReuseAddress(true);
          while (true) {
              Socket clientSocket = serverSocket.accept();
              executorService.submit(() -> {
                  try {
                      handleRequest(clientSocket);
                  } catch (IOException e) {
                      System.out.println("IOException: " + e.getMessage());
                  }
              });
          }
     } catch (IOException e) {
          System.out.println("IOException: " + e.getMessage());
     }
  }

    private static void handleRequest(Socket clientSocket) throws IOException {
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
        List<String> lines = new ArrayList<>();

        String line;
        while (!(line = requestReader.readLine()).isEmpty()) lines.add(line);
        lines.add(line);

        Optional<Integer> contentLength = lines.stream()
                .map(l -> l.split(":"))
                .filter(l -> l.length == 2 && l[0].equalsIgnoreCase("content-length"))
                .findFirst()
                .map(l -> Integer.valueOf(l[1].trim()));

        if (contentLength.filter(it -> it != 0).isPresent()) {
            StringBuilder bodyLine = new StringBuilder();
            for (int i = 0; i < contentLength.get(); i++) {
                bodyLine.append((char) requestReader.read());
            }
            lines.add(bodyLine.toString());
        }

        return lines;
    }

    private static HttpResponse buildResponse(HttpRequest request) throws IOException {
        if (request.method == HttpRequestMethod.GET) return buildGetResponse(request);
        else if (request.method == HttpRequestMethod.POST) return buildPostResponse(request);
        else throw new IllegalArgumentException("Unsupported HTTP method: " + request.method);
    }

    private static HttpResponse buildGetResponse(HttpRequest request) throws IOException {
        String[] pathParts = request.path.substring(1).split("/");
        if (request.path.equals("/")) {
            return new HttpResponse(HttpStatusCode.OK, Collections.emptyMap(), null);
        } else if (request.path.equals("/user-agent")) {
            return userAgentResponse(request.headers.get("user-agent"));
        } else if (pathParts.length == 2 && pathParts[0].equals("echo")) {
            return echoResponse(request, pathParts[1]);
        } else if (pathParts.length == 2 && pathParts[0].equals("files")) {
            return filesGetResponse(pathParts[1]);
        } else {
            return new HttpResponse(HttpStatusCode.NOT_FOUND, Collections.emptyMap(), null);
        }
    }

    private static HttpResponse buildPostResponse(HttpRequest request) throws IOException {
        String[] pathParts = request.path.substring(1).split("/");
        if (pathParts.length == 2 && pathParts[0].equals("files")) {
            return filesPostResponse(request, pathParts[1]);
        } else {
            return new HttpResponse(HttpStatusCode.NOT_FOUND, Collections.emptyMap(), null);
        }
    }

    private static HttpResponse echoResponse(HttpRequest request, String echoValue) {
        String acceptEncodingHeader = request.headers.get("accept-encoding");
        boolean compress = acceptEncodingHeader != null && acceptEncodingHeader.contains("gzip");
        HashMap<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "text/plain");
        headers.put("Content-Length", String.valueOf(echoValue.length()));
        if (compress) headers.put("Content-Encoding", "gzip");
        return new HttpResponse(
                HttpStatusCode.OK,
                headers,
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

    private static HttpResponse filesGetResponse(String filename) throws IOException {
        Path path = Paths.get(fileDirectory, filename);
        if (!Files.exists(path)) {
            return new HttpResponse(HttpStatusCode.NOT_FOUND, Collections.emptyMap(), null);
        } else  {
            byte[] content = Files.readAllBytes(path);
            return new HttpResponse(
                    HttpStatusCode.OK,
                    Map.of(
                            "Content-Type", "application/octet-stream",
                            "Content-Length", String.valueOf(content.length)
                        ),
                    new String(content)
                );
            }
    }

    private static HttpResponse filesPostResponse(HttpRequest request, String filename) throws IOException {
        Path path = Paths.get(fileDirectory, filename);
        try (FileOutputStream outputStream = new FileOutputStream(path.toFile())) {
            outputStream.write(request.body.getBytes());
        }
        return new HttpResponse(HttpStatusCode.CREATED, Collections.emptyMap(), null);
    }

}
