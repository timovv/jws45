package nz.timo.websocket.frame;

import java.nio.ByteBuffer;

public class ReadDataFrameBuildingState implements FrameBuildingState {

    private static final int INTEMEDIARY_BUFFER_SIZE = 512;

    private final Frame currentFrame;
    private final ByteBuffer data;

    public ReadDataFrameBuildingState(Frame currentFrame) {
        this.currentFrame = currentFrame;

        data = ByteBuffer.allocate(currentFrame.getPayloadLength());
        currentFrame.setData(data);
    }

    @Override
    public boolean readFrom(ByteBuffer buffer) {
        if(currentFrame.getPayloadLength() <= 0) {
            return true;
        }

        byte[] intermediary = new byte[INTEMEDIARY_BUFFER_SIZE];
        while(buffer.hasRemaining() && data.hasRemaining()) {
            int count = Math.min(data.remaining(), Math.min(buffer.remaining(), intermediary.length));
            buffer.get(intermediary, 0, count);
            data.put(intermediary, 0, count);
        }

        if(data.hasRemaining()) {
            return false;
        } else {
            data.flip();
            return true;
        }
    }

    @Override
    public Frame getCurrentFrame() {
        return currentFrame;
    }
}
