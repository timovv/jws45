package nz.timo.websocket.frame;

import java.nio.ByteBuffer;

public class HeaderStartFrameBuildingState implements FrameBuildingState {
    private final Frame frame;
    private boolean alreadyReadFirstByte = false;
    private byte first = 0, second = 0;

    public HeaderStartFrameBuildingState(Frame startFrame) {
        this.frame = startFrame;
    }

    @Override
    public boolean readFrom(ByteBuffer buffer) {
        if(buffer.hasRemaining()) {
            if(alreadyReadFirstByte) {
                second = buffer.get();
                updateFrame();
                return true;
            } else {
                first = buffer.get();
                alreadyReadFirstByte = true;
                return false;
            }
        }

        return false;
    }

    @Override
    public Frame getCurrentFrame() {
        return frame;
    }

    private void updateFrame() {
        boolean isFinalFragment = (first & Constants.FIN_BIT) != 0;
        byte opcode = (byte)(first & Constants.OPCODE_MASK);
        boolean isMasked = (second & Constants.MASK_BIT) != 0;
        byte payloadLength = (byte)(second & Constants.PAYLOAD_LENGTH_MASK);

        frame.setFinalFragment(isFinalFragment);
        frame.setOpcode(Opcode.fromCode(opcode));
        frame.setIsMasked(isMasked);
        // States further along the pipeline can handle special cases for this.
        frame.setPayloadLength(payloadLength);
    }
}
