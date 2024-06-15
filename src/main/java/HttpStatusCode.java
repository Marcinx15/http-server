public enum HttpStatusCode {
    OK(200);

    public final int statusCode;

    HttpStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    @Override
    public String toString() {
        return statusCode + " " + name();
    }
}
