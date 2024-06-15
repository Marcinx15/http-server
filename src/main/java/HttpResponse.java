public class HttpResponse {
    private static final String HTTP_VERSION = "HTTP/1.1";
    private final HttpStatusCode statusCode;

    public HttpResponse(HttpStatusCode statusCode) {
        this.statusCode = statusCode;
    }

    public byte[] toBytes() {
        String response = HTTP_VERSION + " " + statusCode + "\r\n" + "\r\n";
        return response.getBytes();
    }
}

