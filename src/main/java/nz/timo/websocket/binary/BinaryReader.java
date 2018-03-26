package nz.timo.websocket.binary;

import java.nio.ByteBuffer;
import java.util.function.Consumer;

public interface BinaryReader {
    void setOnReceived(BinaryDataReceivedHandler callback);
}
