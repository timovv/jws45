package nz.timo.websocket.binary;

import java.nio.ByteBuffer;
import java.util.function.Consumer;

public interface BinaryWriter {
    void sendData(ByteBuffer toSend);
}
