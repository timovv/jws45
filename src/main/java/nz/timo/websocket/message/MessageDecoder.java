package nz.timo.websocket.message;

import nz.timo.websocket.frame.Frame;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class MessageDecoder {

    public MessageDecoder() {

    }

    public String decodeText(List<Frame> frames) {
        return StandardCharsets.UTF_8.decode(decodeGeneric(frames)).toString();
    }

    public ConnectionCloseInfo decodeClose(List<Frame> frames) {
        ByteBuffer bb = decodeGeneric(frames);
        StatusCode statusCode = StatusCode.NO_STATUS_RECEIVED;
        String description = null;
        if(bb.remaining() >= 2) {
            short s = bb.getShort();
            statusCode = StatusCode.getStatusCode((int) s);
            if(bb.remaining() > 0) {
                description = StandardCharsets.UTF_8.decode(bb).toString();
            }
        }

        if(description == null || description.isEmpty()) {
            description = statusCode.getDescription();
        }

        return new ConnectionCloseInfo(statusCode, description);
    }

    public ByteBuffer decodeGeneric(List<Frame> frames) {
        int length = frames.stream().mapToInt(Frame::getPayloadLength).sum();
        ByteBuffer out = ByteBuffer.allocate(length);

        for(Frame frame : frames) {
            out.put(frame.getData());
        }

        out.flip();
        return out;
    }
}
