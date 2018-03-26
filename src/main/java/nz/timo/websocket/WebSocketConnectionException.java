package nz.timo.websocket;

public class WebSocketConnectionException extends RuntimeException {

    public WebSocketConnectionException(String message) {
        super(message);
    }

    public WebSocketConnectionException(Exception innerException) {
        super(innerException);
    }

    public WebSocketConnectionException(String message, Exception innerException) {
        super(message, innerException);
    }
}
