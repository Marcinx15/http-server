public class HttpRequest {
    public final HttpRequestMethod method;
    public final String path;

    private HttpRequest(HttpRequestMethod method, String path) {
        this.method = method;
        this.path = path;
    }

    public static HttpRequest fromString(String request) {
        String requestLine = request.split("\r\n")[0];
        String[] requestParts = requestLine.split(" ");
        HttpRequestMethod method = HttpRequestMethod.valueOf(requestParts[0].toUpperCase());
        return new HttpRequest(method, requestParts[1]);
    }

}

