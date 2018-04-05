package nz.timo.websocket.ssl;

import nz.timo.websocket.Schedulable;
import nz.timo.websocket.ScheduledTaskContext;
import nz.timo.websocket.Scheduler;
import nz.timo.websocket.WebSocketConnectionException;
import nz.timo.websocket.binary.BinaryReader;
import nz.timo.websocket.binary.BinaryReaderListener;
import nz.timo.websocket.binary.BinaryWriter;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import java.nio.ByteBuffer;
import java.security.NoSuchAlgorithmException;

public class SSLEngineBinaryConnection implements BinaryReader, BinaryWriter, Schedulable {

    private final SSLEngine engine;
    private final BinaryReader reader;
    private final BinaryWriter writer;

    public SSLEngineBinaryConnection(BinaryReader parentReader, BinaryWriter parentWriter, Scheduler scheduler, String host, int port) {
        try {
            this.engine = SSLContext.getDefault().createSSLEngine(host, port);
        } catch(NoSuchAlgorithmException e) {
            throw new WebSocketConnectionException(e);
        }

        engine.setUseClientMode(true);
        scheduler.scheduleRepeating(this);
        reader = new SSLEngineReader(engine, parentReader, scheduler);
        writer = new SSLEngineWriter(engine, parentWriter, scheduler);
    }

    @Override
    public void invoke(ScheduledTaskContext ctx) {
        if(engine.getHandshakeStatus() == SSLEngineResult.HandshakeStatus.NEED_TASK) {
            Runnable task;
            while((task = engine.getDelegatedTask()) != null) {
                task.run();
            }

            if(engine.getHandshakeStatus() == SSLEngineResult.HandshakeStatus.NEED_TASK) {
                throw new WebSocketConnectionException("SSLEngine did not perform as expected");
            }
        }
    }

    @Override
    public void setOnReceived(BinaryReaderListener callback) {
        reader.setOnReceived(callback);
    }

    @Override
    public void sendData(ByteBuffer toSend) {
        writer.sendData(toSend);
    }

    @Override
    public void disconnect() {
        writer.disconnect();
    }
}
