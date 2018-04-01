package nz.timo.websocket.message;

import nz.timo.websocket.frame.Frame;
import nz.timo.websocket.frame.Opcode;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class MessageEncoder {

    private final boolean mask;
    private final Random maskingRandom = new SecureRandom();

    public MessageEncoder(boolean mask) {
        this.mask = mask;
    }

    public Frame encodeText(String text) {
        return encode(Opcode.TEXT, StandardCharsets.UTF_8.encode(text));
    }

    public Frame encodeBinary(ByteBuffer data) {
        return encode(Opcode.BINARY, data);
    }

    public Frame encodePing(ByteBuffer data) {
        return encode(Opcode.PING, data);
    }

    public Frame encodePong(ByteBuffer data) {
        return encode(Opcode.PONG, data);
    }

    public Frame encodeCloseConnection(ConnectionCloseInfo info) {
        ByteBuffer encodedText = StandardCharsets.UTF_8.encode(info.getMessage());
        ByteBuffer data = ByteBuffer.allocate(encodedText.remaining() + 2);
        int code = info.getStatusCode().getCode();
        // opcode as 2 byte int and then the text itself
        data.put((byte)((code >>> 8) & 0xff)).put((byte)(code & 0xff)).put(encodedText);
        data.flip();
        return encode(Opcode.CONNECTION_CLOSE, data);
    }

    private Frame encode(Opcode opcode, ByteBuffer data) {
        Frame frame = new Frame();
        int maskingKey = maskingRandom.nextInt();
        ByteBuffer maybeMasked = setupMasking(data, maskingKey);

        frame.setOpcode(opcode);

        if(mask) {
            frame.setMaskingKey(maskingKey);
            frame.setIsMasked(true);
        }

        frame.setData(maybeMasked);
        frame.setPayloadLength(maybeMasked.remaining());
        frame.setFinalFragment(true);
        return frame;
    }

    private ByteBuffer setupMasking(ByteBuffer in, int maskingKey) {

        ByteBuffer out;

        if(mask) {
            out = ByteBuffer.allocate(in.remaining());
            while(in.remaining() >= Integer.BYTES) {
                out.putInt(in.getInt() ^ maskingKey);
            }

            if(in.hasRemaining()) {
                byte[] remaining = new byte[in.remaining()];
                byte[] keyBytes = new byte[Integer.BYTES];
                ByteBuffer.wrap(keyBytes).putInt(maskingKey);
                in.get(remaining, 0, remaining.length);
                for (int i = 0; i < remaining.length; ++i) {
                    out.put((byte)(remaining[i] ^ keyBytes[i]));
                }
            }

            out.flip();
        } else {
            out = in;
        }

        return out;
    }
}
