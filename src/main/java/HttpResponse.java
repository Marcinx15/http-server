import org.apache.commons.lang3.ArrayUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.zip.GZIPOutputStream;

public class HttpResponse {
    private static final String HTTP_VERSION = "HTTP/1.1";
    private final HttpStatusCode statusCode;
    private final Map<String, String> headers;
    private final byte[] body;
    private final boolean shouldCompress;

    public HttpResponse(HttpStatusCode statusCode, Map<String, String> headers, byte[] body) {
        this.statusCode = statusCode;
        this.body = body;
        this.headers = new HashMap<>(headers);
        this.shouldCompress = false;
    }

    public HttpResponse(HttpStatusCode statusCode, Map<String, String> headers, byte[] body, boolean shouldCompress) {
        this.statusCode = statusCode;
        this.body = body;
        this.headers = new HashMap<>(headers);
        this.shouldCompress = shouldCompress;
    }

    public byte[] toBytes() throws IOException {
        byte[] bodyResponse = body;
        if (shouldCompress) {
            headers.put("Content-Encoding", "gzip");
            bodyResponse = compressBody();
        }
        if (body != null) headers.put("Content-Length", String.valueOf(bodyResponse.length));

        String statusLine = getStatusLine();
        String headersSection = headers.entrySet().stream()
                .map(entry -> entry.getKey() + ": " + entry.getValue() + "\r\n")
                .collect(Collectors.joining());
        String response = String.join("\r\n", statusLine, headersSection, "");

        return ArrayUtils.addAll(response.getBytes(), bodyResponse);
    }

    private byte[] compressBody() throws IOException {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             GZIPOutputStream gzipOutputStream = new GZIPOutputStream(outputStream)) {
            gzipOutputStream.write(body);
            gzipOutputStream.finish();
            return outputStream.toByteArray();
        }
    }

    private String getStatusLine() {
        return HTTP_VERSION + " " + statusCode;
    }
}

