package nz.timo.websocket.binary;

import java.nio.ByteBuffer;

public class BinaryConnection implements BinaryReader, BinaryWriter {

    private final BinaryReader reader;
    private final BinaryWriter writer;

    public BinaryConnection(BinaryReader reader, BinaryWriter writer) {
        this.reader = reader;
        this.writer = writer;
    }

    @Override
    public void setOnReceived(BinaryReaderListener callback) {
        reader.setOnReceived(callback);
    }

    @Override
    public void sendData(ByteBuffer toSend) {
        writer.sendData(toSend);
    }

    @Override
    public void disconnect() {
        writer.disconnect();
    }
}
