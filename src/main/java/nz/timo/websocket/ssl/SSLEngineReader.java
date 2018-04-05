package nz.timo.websocket.ssl;

import nz.timo.websocket.Schedulable;
import nz.timo.websocket.ScheduledTaskContext;
import nz.timo.websocket.Scheduler;
import nz.timo.websocket.WebSocketConnectionException;
import nz.timo.websocket.binary.BinaryReader;
import nz.timo.websocket.binary.BinaryReaderListener;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLException;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.Queue;

public class SSLEngineReader implements BinaryReader, BinaryReaderListener, Schedulable {

    private final SSLEngine engine;
    private ByteBuffer ciphertextBuffer;
    private ByteBuffer plaintextBuffer;
    private BinaryReaderListener callback;
    private final Queue<ByteBuffer> buffers = new LinkedList<>();

    private boolean disconnecting = false;

    public SSLEngineReader(SSLEngine engine, BinaryReader parent, Scheduler scheduler) {
        parent.setOnReceived(this);
        this.engine = engine;
        ciphertextBuffer = ByteBuffer.allocate(2 * engine.getSession().getPacketBufferSize());
        ciphertextBuffer.flip();
        plaintextBuffer = ByteBuffer.allocate(2 * engine.getSession().getApplicationBufferSize());
        scheduler.scheduleRepeating(this);
    }

    @Override
    public void invoke(ScheduledTaskContext ctx) {
        ciphertextBuffer.compact();
        Util.fillBuffer(buffers, ciphertextBuffer);
        ciphertextBuffer.flip();
        plaintextBuffer.clear();

        SSLEngineResult result;
        try {
            result = engine.unwrap(ciphertextBuffer, plaintextBuffer);
        } catch (SSLException e) {
            throw new WebSocketConnectionException(e);
        }

        plaintextBuffer.flip();

        switch (result.getStatus()) {
            case OK:
                if(result.bytesProduced() > 0) {
                    handleOK();
                }
                break;
            case CLOSED:
                handleClosed();
                break;
            case BUFFER_OVERFLOW:
                // Given the size of ciphertextBuffer, this shouldn't happen unless something has gone horribly wrong.
                throw new WebSocketConnectionException("SSLEngine buffer overflow");
            case BUFFER_UNDERFLOW:
                /*
                 * TODO: we just wait for more data to come in here, however if this happens we should probably stop
                 * polling unwrap() until more data comes in.
                 */
                break;
        }
    }

    @Override
    public void setOnReceived(BinaryReaderListener callback) {
        this.callback = callback;
    }

    @Override
    public void onReceived(ByteBuffer data) {
        buffers.add(data);
    }

    @Override
    public void onDisconnected() {
        if(!disconnecting) {
            disconnecting = true;
            callback.onDisconnected();
        }
    }

    private void handleOK() {
        ByteBuffer copy = ByteBuffer.allocate(plaintextBuffer.remaining());
        copy.put(plaintextBuffer).flip();
        if (callback != null) {
            callback.onReceived(copy);
        }
    }

    private void handleClosed() {
        if(!disconnecting) {
            try {
                engine.closeInbound();
            } catch(SSLException e) {
                throw new WebSocketConnectionException(e);
            }
        }

        onDisconnected();
    }
}
