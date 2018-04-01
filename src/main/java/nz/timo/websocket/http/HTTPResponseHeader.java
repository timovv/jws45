package nz.timo.websocket.http;

public class HTTPResponseHeader extends HTTPHeader {

    private final int statusCode;

    public HTTPResponseHeader(int statusCode) {
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
