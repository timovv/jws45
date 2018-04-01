package nz.timo.websocket.frame;

public interface FrameReceivedHandler {
    void onFrameReceived(Frame frame);

    default void onDisconnected() {}
}
