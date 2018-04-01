package nz.timo.websocket.frame;

public interface FrameReader {
    void setOnReceived(FrameReceivedHandler handler);
}
