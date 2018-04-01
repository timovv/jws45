package nz.timo.websocket.frame;

import java.nio.ByteBuffer;

public interface FrameBuildingState {
    boolean readFrom(ByteBuffer buffer);

    Frame getCurrentFrame();
}
