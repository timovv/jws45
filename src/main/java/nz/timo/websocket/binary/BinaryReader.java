package nz.timo.websocket.binary;

public interface BinaryReader {
    void setOnReceived(BinaryReaderListener callback);
}
