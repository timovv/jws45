package nz.timo.websocket.message;

public interface IncomingConnection {
    void setHandler(WebSocketMessageHandler handler);
}
