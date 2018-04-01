package nz.timo.websocket.binary;

import java.nio.ByteBuffer;

public interface BinaryReaderListener {
    void onReceived(ByteBuffer data);

    default void onDisconnected() {}
}
