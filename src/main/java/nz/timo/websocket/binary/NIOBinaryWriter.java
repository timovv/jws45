package nz.timo.websocket.binary;

import nz.timo.websocket.*;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Queue;
import java.util.function.Consumer;

/**
 * A BinaryWriter based on an NIO channel.
 */
public class NIOBinaryWriter implements BinaryWriter, Schedulable {
    private Queue<ByteBuffer> toWrite = new LinkedList<>();
    private final WritableByteChannel channel;

    private enum State {
        OPEN,
        CLOSING,
        CLOSED
    }

    private State state = State.OPEN;

    public NIOBinaryWriter(WritableByteChannel channel, Scheduler scheduler) {
        Objects.requireNonNull(channel);
        Objects.requireNonNull(scheduler);

        scheduler.scheduleRepeating(this);
        this.channel = channel;
    }


    @Override
    public void sendData(ByteBuffer toSend) {
        if(state == State.CLOSING) {
            throw new WebSocketConnectionException("Attempted to write data while connection is closing.");
        } else if(state == State.CLOSED) {
            throw new WebSocketConnectionException("Attempted to write data while connection is closed.");
        }

        toWrite.offer(toSend);
    }

    @Override
    public void disconnect() {
        state = State.CLOSING;
    }

    @Override
    public void invoke(ScheduledTaskContext ctx) {
        ByteBuffer next = toWrite.peek();

        if(!channel.isOpen()) {
            ctx.setShouldRepeat(false);
            System.err.println("Channel closed unexpectedly");
            return;
        }

        if(next == null) {
            if(state == State.CLOSING) {
                tryCloseConnection();
                ctx.setShouldRepeat(false);
            }

            return;
        }

        try {
            channel.write(next);
        } catch(IOException e) {
            state = State.CLOSED;
            ctx.setShouldRepeat(false);
            return;
        }


        if(!next.hasRemaining()) {
            toWrite.remove();
        }
    }

    private void tryCloseConnection() {
        try {
            channel.close();
        } catch(IOException e) {
            throw new WebSocketConnectionException("Error while closing connection", e);
        } finally {
            state = State.CLOSED;
        }
    }
}
