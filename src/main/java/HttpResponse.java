import java.util.Map;
import java.util.stream.Collectors;

public class HttpResponse {
    private static final String HTTP_VERSION = "HTTP/1.1";
    private final HttpStatusCode statusCode;
    private final Map<String, String> headers;
    private final String body;

    public HttpResponse(HttpStatusCode statusCode, Map<String, String> headers, String body) {
        this.statusCode = statusCode;
        this.headers = headers;
        this.body = body;
    }

    public byte[] toBytes() {
        String statusLine = getStatusLine();
        String headersSection = headers.entrySet().stream()
                .map(entry -> entry.getKey() + ": " + entry.getValue() + "\r\n")
                .collect(Collectors.joining());
        String response = String.join("\r\n", statusLine, headersSection, body != null ? body : "");
        System.out.println(response);
        return response.getBytes();
    }

    private String getStatusLine() {
        return HTTP_VERSION + " " + statusCode;
    }
}

