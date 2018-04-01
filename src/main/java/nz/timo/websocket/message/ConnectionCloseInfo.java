package nz.timo.websocket.message;

public class ConnectionCloseInfo {

    private final StatusCode statusCode;
    private final String message;

    public ConnectionCloseInfo(StatusCode statusCode) {
        this(statusCode, "");
    }

    public ConnectionCloseInfo(StatusCode statusCode, String message) {
        this.statusCode = statusCode;
        this.message = message;
    }

    public StatusCode getStatusCode() {
        return statusCode;
    }

    public String getMessage() {
        return message;
    }
}
