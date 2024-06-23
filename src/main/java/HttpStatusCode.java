public enum HttpStatusCode {
    OK(200, "OK"),
    CREATED(201, "Created"),
    NOT_FOUND(404, "Not Found");

    private final int statusCode;
    private final String name;

    HttpStatusCode(int statusCode, String name) {
        this.statusCode = statusCode;
        this.name = name;
    }

    @Override
    public String toString() {
        return statusCode + " " + name;
    }
}
