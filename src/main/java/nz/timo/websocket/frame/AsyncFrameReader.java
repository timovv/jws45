package nz.timo.websocket.frame;

import nz.timo.websocket.binary.BinaryReaderListener;
import nz.timo.websocket.binary.BinaryReader;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.function.Function;

public class AsyncFrameReader implements FrameReader, BinaryReaderListener {

    private static final List<Function<Frame, FrameBuildingState>> FRAME_BUILDING_STATES = new ArrayList<>();

    private FrameReceivedHandler frameReceivedHandler;

    private FrameBuildingState currentState = null;
    private Queue<Function<Frame, FrameBuildingState>> states = new LinkedList<>(FRAME_BUILDING_STATES);

    static {
        FRAME_BUILDING_STATES.add(HeaderStartFrameBuildingState::new);
        FRAME_BUILDING_STATES.add(PayloadLengthFrameBuildingState::new);
        FRAME_BUILDING_STATES.add(MaskingKeyFrameBuildingState::new);
        FRAME_BUILDING_STATES.add(ReadDataFrameBuildingState::new);
    }

    public AsyncFrameReader(BinaryReader reader) {
        reader.setOnReceived(this);
    }

    @Override
    public void setOnReceived(FrameReceivedHandler handler) {
        this.frameReceivedHandler = handler;
    }

    @Override
    public void onReceived(ByteBuffer data) {
        if(currentState == null) {
            currentState = states.poll().apply(new Frame());
        }

        while(data.hasRemaining()) {
            if(currentState.readFrom(data)) {
                if(states.isEmpty()) {
                    dispatchFrame(currentState.getCurrentFrame());
                    states.addAll(FRAME_BUILDING_STATES);
                    currentState = states.poll().apply(new Frame());
                } else {
                    currentState = states.poll().apply(currentState.getCurrentFrame());
                }
            }
        }

    }

    @Override
    public void onDisconnected() {
        frameReceivedHandler.onDisconnected();
    }

    private void dispatchFrame(Frame frame) {
        if(frameReceivedHandler != null) {
            frameReceivedHandler.onFrameReceived(frame);
        }
    }
}
