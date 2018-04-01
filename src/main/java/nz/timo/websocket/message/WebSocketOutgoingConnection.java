package nz.timo.websocket.message;

import nz.timo.websocket.Scheduler;
import nz.timo.websocket.frame.Frame;
import nz.timo.websocket.frame.FrameWriter;

import java.nio.ByteBuffer;
import java.util.List;

public class WebSocketOutgoingConnection implements OutgoingConnection {

    private static final long CONNECTION_CLOSE_DELAY_MILLIS = 1000L;

    private final FrameWriter frameWriter;
    private final MessageEncoder encoder;
    private final Scheduler scheduler;

    public WebSocketOutgoingConnection(FrameWriter writer, boolean isClient, Scheduler scheduler) {
        frameWriter = writer;
        encoder = new MessageEncoder(isClient);
        this.scheduler = scheduler;
    }

    @Override
    public void sendText(String message) {
        frameWriter.sendData(encoder.encodeText(message));
    }

    @Override
    public void sendBinary(ByteBuffer data) {
        frameWriter.sendData(encoder.encodeBinary(data));
    }

    @Override
    public void sendPing(ByteBuffer data) {
        frameWriter.sendData(encoder.encodePing(data));
    }

    @Override
    public void sendPong(ByteBuffer data) {
        frameWriter.sendData(encoder.encodePong(data));
    }

    @Override
    public void disconnect(ConnectionCloseInfo info) {
        frameWriter.sendData(encoder.encodeCloseConnection(info));
        scheduler.scheduleLater(ctx -> frameWriter.disconnect(), CONNECTION_CLOSE_DELAY_MILLIS);
    }
}
