package nz.timo.websocket.frame;

import nz.timo.websocket.binary.BinaryWriter;

import java.nio.ByteBuffer;
import java.util.Objects;

public class WebSocketFrameWriter implements FrameWriter {

    private final BinaryWriter binaryWriter;

    public WebSocketFrameWriter(BinaryWriter writer) {
        this.binaryWriter = writer;
    }

    @Override
    public void sendData(Frame frame) {
        Objects.requireNonNull(frame);

        ByteBuffer header = ByteBuffer.allocate(calculateLength(frame));

        // first part of the header
        byte firstByte = 0;
        if(frame.isFinalFragment()) {
            firstByte |= Constants.FIN_BIT;
        }
        firstByte |= frame.getOpcode().getCode();

        byte secondByte = 0;
        if(frame.isDataMasked()) {
            secondByte |= Constants.MASK_BIT;
        }

        PayloadLengthType lengthType = getLengthType(frame.getPayloadLength());
        switch(lengthType) {
            case BIT_7:
                secondByte |= frame.getPayloadLength();
                break;
            case BIT_16:
                secondByte |= Constants.PAYLOAD_LENGTH_16_BIT_INDICATOR;
                break;
            case BIT_64:
                secondByte |= Constants.PAYLOAD_LENGTH_64_BIT_INDICATOR;
                break;
        }

        header.put(firstByte).put(secondByte);

        if(lengthType == PayloadLengthType.BIT_16) {
            // since we want an unsigned value here we have to do some ugly magic
            byte[] b = new byte[Integer.BYTES];
            ByteBuffer.wrap(b).putInt(frame.getPayloadLength());
            // grab the last two bytes from the int
            header.put(b, 2, 2);
        } else if(lengthType == PayloadLengthType.BIT_64) {
            // per specification sign bit won't be set on 64-bit payload lengths so this is OK
            header.putLong(frame.getPayloadLength());
        }

        if(frame.isDataMasked()) {
            header.putInt(frame.getMaskingKey());
        }

        header.flip();

        binaryWriter.sendData(header);
        // calling sendData on the data itself instead of combining separately saves a potentially expensive copy
        if(frame.getData() != null) {
            binaryWriter.sendData(frame.getData());
        }
    }

    @Override
    public void disconnect() {
        binaryWriter.disconnect();
    }

    private int calculateLength(Frame frame) {
        int length = 0;

        // if the length type needs to be represented in a special way then add space for that
        length += getLengthType(frame.getPayloadLength()).getBytesRequired();

        // Add length for 32-bit masking key
        if(frame.isDataMasked()) {
            length += Integer.BYTES;
        }

        // part at the very start of the header including FIN and opcode
        length += Short.BYTES;

        return length;
    }

    private PayloadLengthType getLengthType(int payloadLength) {
        // x2 because unsigned
        if(payloadLength >= 2 * Short.MAX_VALUE) {
            // 64-bit form
            return PayloadLengthType.BIT_64;
        } else if(payloadLength > 0x7f - 2) {
            return PayloadLengthType.BIT_16;
        } else {
            return PayloadLengthType.BIT_7;
        }
    }
}
