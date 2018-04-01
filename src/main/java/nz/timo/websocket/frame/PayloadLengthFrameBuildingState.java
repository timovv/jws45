package nz.timo.websocket.frame;

import java.nio.ByteBuffer;

public class PayloadLengthFrameBuildingState implements FrameBuildingState {

    private final Frame currentFrame;
    private PayloadLengthType lengthType;
    private ByteBuffer data;

    public PayloadLengthFrameBuildingState(Frame currentFrame) {
        this.currentFrame = currentFrame;

        if(currentFrame.getPayloadLength() == Constants.PAYLOAD_LENGTH_16_BIT_INDICATOR) {
            lengthType = PayloadLengthType.BIT_16;
        } else if(currentFrame.getPayloadLength() == Constants.PAYLOAD_LENGTH_64_BIT_INDICATOR) {
            lengthType = PayloadLengthType.BIT_64;
        } else {
            lengthType = PayloadLengthType.BIT_7;
        }

        data = ByteBuffer.allocate(lengthType.getBytesRequired());
    }

    @Override
    public boolean readFrom(ByteBuffer buffer) {
        if(lengthType == PayloadLengthType.BIT_7) {
            // if it's the 7-bit type, skip it since it was recorded previously.
            return true;
        }

        while(data.hasRemaining() && buffer.hasRemaining()) {
            data.put(buffer.get());
        }

        if(data.hasRemaining()) {
            return false;
        } else {
            data.flip();

            if(lengthType == PayloadLengthType.BIT_16) {
                // unsigned->signed conversion
                byte[] b = new byte[4];
                ByteBuffer buf = ByteBuffer.wrap(b).putShort((short)0).putShort(data.getShort());
                buf.flip();
                currentFrame.setPayloadLength(buf.getInt());
            } else {
                currentFrame.setPayloadLength((int)data.getLong());
            }

            return true;
        }
    }

    @Override
    public Frame getCurrentFrame() {
        return currentFrame;
    }
}
