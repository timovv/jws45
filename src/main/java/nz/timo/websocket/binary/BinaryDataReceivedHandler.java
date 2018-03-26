package nz.timo.websocket.binary;

import java.nio.ByteBuffer;

public interface BinaryDataReceivedHandler {
    void onReceived(ByteBuffer data);
}
