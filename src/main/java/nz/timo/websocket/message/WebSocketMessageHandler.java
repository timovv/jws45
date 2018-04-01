package nz.timo.websocket.message;

import java.nio.ByteBuffer;

public interface WebSocketMessageHandler {
    void onTextReceived(String text);

    void onBinaryReceived(ByteBuffer binary);

    void onDisconnected(ConnectionCloseInfo info);

    default void onPing(ByteBuffer pingData) {}

    default void onPong(ByteBuffer pongData) {}
}
