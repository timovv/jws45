package nz.timo.websocket;

import nz.timo.websocket.binary.BinaryReader;
import nz.timo.websocket.binary.BinaryWriter;
import nz.timo.websocket.binary.NIOBinaryReader;
import nz.timo.websocket.binary.NIOBinaryWriter;
import nz.timo.websocket.frame.AsyncFrameReader;
import nz.timo.websocket.frame.FrameReader;
import nz.timo.websocket.frame.FrameWriter;
import nz.timo.websocket.frame.WebSocketFrameWriter;
import nz.timo.websocket.http.HTTPWebSocketBinaryConnection;
import nz.timo.websocket.message.*;
import nz.timo.websocket.ssl.SSLEngineBinaryConnection;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.channels.SocketChannel;
import java.util.Collections;

public abstract class WebSocketClient implements WebSocketMessageHandler {
    private static final int BUFFER_SIZE = 8192;

    private final OutgoingConnection connection;
    private final PollableScheduler scheduler;

    public WebSocketClient(String endpoint) {
        this(safeURICreate(endpoint));
    }

    private static URI safeURICreate(String uri) {
        try {
            return new URI(uri);
        } catch(URISyntaxException e) {
            throw new WebSocketConnectionException(e);
        }
    }

    public WebSocketClient(URI endpoint) {
        boolean secure = endpoint.getScheme().equals("wss");
        int port = getPort(endpoint);
        SocketAddress address = new InetSocketAddress(endpoint.getHost(), port);
        SocketChannel channel;
        try {
            channel = SocketChannel.open(address);
            channel.configureBlocking(false);
        } catch(IOException e) {
            throw new WebSocketConnectionException(e);
        }

        PollableScheduler scheduler = new PollableScheduler();
        BinaryReader reader = new NIOBinaryReader(channel, scheduler, BUFFER_SIZE);
        BinaryWriter writer = new NIOBinaryWriter(channel, scheduler);

        if(secure) {
            SSLEngineBinaryConnection connection = new SSLEngineBinaryConnection(reader, writer, scheduler, endpoint.getHost(), port);
            reader = connection;
            writer = connection;
        }

        HTTPWebSocketBinaryConnection httpLayer = new HTTPWebSocketBinaryConnection(reader, writer);
        httpLayer.connect(endpoint, Collections.emptyMap());

        FrameReader frameReader = new AsyncFrameReader(httpLayer);
        FrameWriter frameWriter = new WebSocketFrameWriter(httpLayer);

        IncomingConnection incomingConnection = new WebSocketIncomingConnection(frameReader);
        OutgoingConnection outgoingConnection = new WebSocketOutgoingConnection(frameWriter, true, scheduler);

        ControlMessageHandler cmh = new ControlMessageHandler(incomingConnection, outgoingConnection);
        cmh.setHandler(this);
        this.connection = cmh;
        this.scheduler = scheduler;
    }

    public OutgoingConnection getConnection() {
        return connection;
    }

    protected Scheduler getScheduler() {
        return scheduler;
    }

    private int getPort(URI uri) {
        if(uri.getPort() != -1) {
            return uri.getPort();
        }

        switch(uri.getScheme()) {
            case "wss":
                return 443;
            case "ws":
            default:
                return 80;
        }
    }

    public final void pollOnce() {
        scheduler.poll();
    }

    public final void pollForever() {
        while(true) {
            pollOnce();
        }
    }

}
