package nz.timo.websocket.frame;

public interface FrameWriter {
    void sendData(Frame frame);

    void disconnect();
}
