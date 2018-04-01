package nz.timo.websocket.message;

import nz.timo.websocket.frame.Frame;
import nz.timo.websocket.frame.FrameReader;
import nz.timo.websocket.frame.FrameReceivedHandler;
import nz.timo.websocket.frame.Opcode;

import java.util.LinkedList;

public class WebSocketIncomingConnection implements IncomingConnection, FrameReceivedHandler {

    private WebSocketMessageHandler handler = null;

    private final LinkedList<Frame> frames = new LinkedList<>();
    private final MessageDecoder decoder = new MessageDecoder();

    private boolean disconnecting = false;

    public WebSocketIncomingConnection(FrameReader frameReader) {
        frameReader.setOnReceived(this);
    }


    @Override
    public void setHandler(WebSocketMessageHandler handler) {
        this.handler = handler;
    }

    @Override
    public void onFrameReceived(Frame frame) {
        if (!frames.isEmpty() && frames.getFirst().getOpcode() != frame.getOpcode()) {
            // unexpected frame type
            System.err.println("Unexpected frame type received, ignoring it");
            return;
        }

        frames.addLast(frame);

        if (frame.isFinalFragment()) {
            dispatchFrames();
        }
    }

    @Override
    public void onDisconnected() {
        // this method is only called if the raw socket was disconnected.
        // only report this if we haven't reported it already
        if(!disconnecting) {
            disconnecting = true;
            handler.onDisconnected(new ConnectionCloseInfo(StatusCode.ABNORMAL_CLOSURE, "The connection was closed unexpectedly."));
        }
    }

    private void dispatchFrames() {
        if(handler != null) {
            switch (frames.getFirst().getOpcode()) {
                case BINARY:
                    handler.onBinaryReceived(decoder.decodeGeneric(frames));
                    break;
                case TEXT:
                    handler.onTextReceived(decoder.decodeText(frames));
                    break;
                case CONNECTION_CLOSE:
                    disconnecting = true;
                    handler.onDisconnected(decoder.decodeClose(frames));
                    break;
                case PING:
                    handler.onPing(decoder.decodeGeneric(frames));
                    break;
                case PONG:
                    handler.onPong(decoder.decodeGeneric(frames));
                    break;
            }
        }

        frames.clear();
    }
}
