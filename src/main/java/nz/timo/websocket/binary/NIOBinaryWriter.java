package nz.timo.websocket.binary;

import nz.timo.websocket.Pollable;
import nz.timo.websocket.PollableRegistrar;
import nz.timo.websocket.PollAgainPriority;
import nz.timo.websocket.WebSocketConnectionException;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.util.LinkedList;
import java.util.Queue;
import java.util.function.Consumer;

/**
 * A BinaryWriter based on an NIO channel.
 */
public class NIOBinaryWriter implements BinaryWriter, Pollable {
    private Queue<ByteBuffer> toWrite = new LinkedList<>();
    private final WritableByteChannel channel;
    private final PollableRegistrar registrar;

    public NIOBinaryWriter(PollableRegistrar registrar, WritableByteChannel channel) {
        if(registrar == null) {
            throw new IllegalArgumentException("registrar cannot be null!");
        }
        if(channel == null) {
            throw new IllegalArgumentException("channel cannot be null!");
        }

        registrar.register(this);
        this.registrar = registrar;
        this.channel = channel;
    }


    @Override
    public void sendData(ByteBuffer toSend) {
        toWrite.offer(toSend);
    }

    @Override
    public PollAgainPriority poll() {
        ByteBuffer next = toWrite.peek();
        if(next == null) {
            return PollAgainPriority.LOWEST;
        }

        try {
            channel.write(next);
        } catch(IOException e) {
            throw new WebSocketConnectionException("Error during sending binary data", e);
        }

        if(next.hasRemaining()) {
            return PollAgainPriority.HIGHEST;
        } else {
            toWrite.remove();

            if(toWrite.isEmpty()) {
                // No work to do here
                return PollAgainPriority.LOW;
            } else {
                return PollAgainPriority.NORMAL;
            }
        }

    }
}
