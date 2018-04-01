package nz.timo.websocket.message;

import java.nio.ByteBuffer;

public interface OutgoingConnection {
    void sendText(String message);

    void sendBinary(ByteBuffer data);

    void sendPing(ByteBuffer data);

    void sendPong(ByteBuffer data);
    
    default void disconnect() {
        disconnect(StatusCode.NORMAL_CLOSURE);
    }

    default void disconnect(StatusCode statusCode) {
        disconnect(new ConnectionCloseInfo(statusCode));
    }

    void disconnect(ConnectionCloseInfo info);
}
