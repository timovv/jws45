package nz.timo.websocket.binary;

import nz.timo.websocket.PollAgainPriority;
import nz.timo.websocket.Pollable;
import nz.timo.websocket.PollableRegistrar;
import nz.timo.websocket.WebSocketConnectionException;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.util.function.Consumer;

/**
 * Binary reader that reads off an NIO channel.
 * @author Timo van Veenendaal
 */
public class NIOBinaryReader implements BinaryReader, Pollable {

    private final ReadableByteChannel channel;
    private final PollableRegistrar registrar;
    private final ByteBuffer buffer;
    private BinaryDataReceivedHandler callback = null;

    public NIOBinaryReader(ReadableByteChannel channel, PollableRegistrar registrar, int bufferSize) {
        this.channel = channel;
        registrar.register(this);
        this.registrar = registrar;

        // good to use allocateDirect as this buffer is long-lived (as long as this connection).
        // allocateDirect provides better performance in cases like this.
        this.buffer = ByteBuffer.allocateDirect(bufferSize);
    }

    /**
     * Set the callback to be called when this reader receives data.
     * IMPORTANT:
     * The ByteBuffer given in the callback is NOT SAFE TO CACHE, and after the callback's execution is complete,
     * the data in the buffer may change AT ANY TIME, for performance reasons.
     * If you need to keep the data, make a copy.
     * @param callback
     */
    @Override
    public void setOnReceived(BinaryDataReceivedHandler callback) {
        this.callback = callback;
    }

    @Override
    public PollAgainPriority poll() {
        if(callback == null) {
            // why are we even here if there's no callback.
            return PollAgainPriority.LOW;
        }

        int count;
        try {
            count = channel.read(buffer);
        } catch(IOException e) {
            throw new WebSocketConnectionException("Error occurred while reading bytes", e);
        }

        if(count == 0) {
            return PollAgainPriority.NORMAL;
        }

        // If there's no space remaining in the buffer, chances are there's more data available to read,
        // so make sure we come back here quick. Otherwise it's likely we're out of data to read and we don't need
        // to come back super quickly.
        PollAgainPriority priority = buffer.hasRemaining() ? PollAgainPriority.NORMAL : PollAgainPriority.HIGHEST;

        buffer.flip();
        callback.onReceived(buffer.asReadOnlyBuffer());

        // reset the buffer so we can use it again
        buffer.clear();

        return priority;
    }
}
