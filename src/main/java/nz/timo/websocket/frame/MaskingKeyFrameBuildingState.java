package nz.timo.websocket.frame;

import java.nio.ByteBuffer;

public class MaskingKeyFrameBuildingState implements FrameBuildingState {

    private final Frame currentFrame;
    private ByteBuffer data;

    public MaskingKeyFrameBuildingState(Frame current) {
        this.currentFrame = current;

        if(currentFrame.isDataMasked()) {
            data = ByteBuffer.allocate(Integer.BYTES);
        }
    }

    @Override
    public boolean readFrom(ByteBuffer buffer) {
        if(!currentFrame.isDataMasked()) {
            return true;
        }

        while(data.hasRemaining() && buffer.hasRemaining()) {
            data.put(buffer.get());
        }

        if(data.hasRemaining()) {
            return false;
        } else {
            data.flip();
            currentFrame.setMaskingKey(data.getInt());
            return true;
        }
    }

    @Override
    public Frame getCurrentFrame() {
        return currentFrame;
    }
}
