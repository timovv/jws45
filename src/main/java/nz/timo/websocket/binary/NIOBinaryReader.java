package nz.timo.websocket.binary;

import nz.timo.websocket.Schedulable;
import nz.timo.websocket.ScheduledTaskContext;
import nz.timo.websocket.Scheduler;
import nz.timo.websocket.WebSocketConnectionException;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;

/**
 * Binary reader that reads off an NIO channel.
 * @author Timo van Veenendaal
 */
public class NIOBinaryReader implements BinaryReader, Schedulable {

    private final ReadableByteChannel channel;
    private final ByteBuffer buffer;
    private BinaryReaderListener callback = null;

    public NIOBinaryReader(ReadableByteChannel channel, Scheduler scheduler, int bufferSize) {
        this.channel = channel;
        scheduler.scheduleRepeating(this);
        this.buffer = ByteBuffer.allocateDirect(bufferSize);
    }

    @Override
    public void setOnReceived(BinaryReaderListener callback) {
        this.callback = callback;
    }

    @Override
    public void invoke(ScheduledTaskContext ctx) {
        if(callback == null) {
            return;
        }

        int count;
        try {
            count = channel.read(buffer);
        } catch(IOException e) {
            callback.onDisconnected();
            ctx.setShouldRepeat(false);
            return;
        }

        if(count == 0) {
            return;
        }

        if(count == -1) {
            callback.onDisconnected();
            ctx.setShouldRepeat(false);
            return;
        }

        buffer.flip();
        callback.onReceived(buffer.asReadOnlyBuffer());

        // reset the buffer so we can use it again
        buffer.clear();
    }
}
