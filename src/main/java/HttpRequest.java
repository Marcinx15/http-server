import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class HttpRequest {
    public final HttpRequestMethod method;
    public final String path;
    public final Map<String, String> headers;
    public final String body;

    private HttpRequest(HttpRequestMethod method, String path, Map<String, String> headers, String body) {
        this.method = method;
        this.path = path;
        this.headers = headers;
        this.body = body;
    }

    public static HttpRequest fromRequestLines(List<String> requestLines) {
        String[] statusLineParts = requestLines.getFirst().split("\r\n")[0].split(" ");
        Map<String, String> headers = requestLines.stream()
                .skip(1)
                .takeWhile(line -> !line.isEmpty())
                .map(line -> line.split(":"))
                .collect(Collectors.toMap(it -> it[0].toLowerCase(), it -> it[1].trim()));
        return new HttpRequest(
                HttpRequestMethod.valueOf(statusLineParts[0].toUpperCase()),
                statusLineParts[1],
                headers,
                !requestLines.getLast().isEmpty() ? requestLines.getLast() : null
        );
    }


}

