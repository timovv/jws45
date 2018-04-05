package nz.timo.websocket.ssl;

import nz.timo.websocket.Schedulable;
import nz.timo.websocket.ScheduledTaskContext;
import nz.timo.websocket.Scheduler;
import nz.timo.websocket.WebSocketConnectionException;
import nz.timo.websocket.binary.BinaryWriter;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLException;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.Queue;

public class SSLEngineWriter implements BinaryWriter, Schedulable {

    private final SSLEngine engine;
    private final Queue<ByteBuffer> sendingQueue = new LinkedList<>();
    private final BinaryWriter parent;

    private ByteBuffer ciphertextBuffer;
    private ByteBuffer plaintextBuffer;

    public SSLEngineWriter(SSLEngine engine, BinaryWriter parent, Scheduler scheduler) {
        this.engine = engine;
        this.parent = parent;

        ciphertextBuffer = ByteBuffer.allocate(2 * engine.getSession().getPacketBufferSize());
        plaintextBuffer = ByteBuffer.allocate(2 * engine.getSession().getApplicationBufferSize());

        scheduler.scheduleRepeating(this);
    }

    @Override
    public void sendData(ByteBuffer toSend) {
        sendingQueue.add(toSend);
    }

    @Override
    public void disconnect() {
        engine.closeOutbound();
        parent.disconnect();
    }

    @Override
    public void invoke(ScheduledTaskContext ctx) {
        plaintextBuffer.compact();
        Util.fillBuffer(sendingQueue, plaintextBuffer);
        plaintextBuffer.flip();
        ciphertextBuffer.clear();

        SSLEngineResult result;
        try {
            result = engine.wrap(plaintextBuffer, ciphertextBuffer);
        } catch(SSLException e) {
            throw new WebSocketConnectionException(e);
        }

        switch(result.getStatus()) {
            case OK:
                ciphertextBuffer.flip();
                if(result.bytesProduced() > 0) {
                    ByteBuffer copy = ByteBuffer.allocate(ciphertextBuffer.remaining());
                    copy.put(ciphertextBuffer).flip();
                    parent.sendData(copy);
                }
                break;
            case CLOSED:
                break;
            case BUFFER_OVERFLOW:
                throw new WebSocketConnectionException("SSLEngine buffer overflow");
            case BUFFER_UNDERFLOW:
                break;
        }
    }
}
