package nz.timo.websocket.http;

import nz.timo.websocket.binary.BinaryReaderListener;
import nz.timo.websocket.binary.BinaryReader;
import nz.timo.websocket.binary.BinaryWriter;

import java.net.URI;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.*;

public class HTTPWebSocketBinaryConnection implements BinaryReader, BinaryWriter, BinaryReaderListener {

    private static final String HTTP_NEW_LINE = "\r\n";
    private static final String SEC_WEBSOCKET_VERSION = "13";

    private final Random random = new Random();
    private final BinaryReader parentReader;
    private final BinaryWriter parentWriter;
    private final HTTPHeaderDecoder decoder = new HTTPHeaderDecoder();
    private final Queue<ByteBuffer> toSendLater = new LinkedList<>();
    private BinaryReaderListener onReceivedCallback;

    private byte[] nonce;

    private enum State {
        NOT_CONNECTED,
        CONNECTING,
        CONNECTED,
    }

    private State state = State.NOT_CONNECTED;

    public HTTPWebSocketBinaryConnection(BinaryReader parentReader, BinaryWriter parentWriter) {
        this.parentReader = parentReader;
        parentReader.setOnReceived(this);
        this.parentWriter = parentWriter;
    }

    public void connect(URI uri, Map<String, String> headers) {
        if(state != State.NOT_CONNECTED) {
            throw new IllegalStateException("Already connecting or connected!");
        }

        if(!uri.getScheme().equals("ws") && !uri.getScheme().equals("wss")) {
            throw new IllegalArgumentException("Not a WebSocket URL");
        }

        HTTPRequestHeader header = new HTTPRequestHeader(uri.getPath(), uri.getHost(), uri.getPort(), HTTPMethod.GET);

        for(Map.Entry<String, String> e : headers.entrySet()) {
            header.setHeader(e.getKey(), e.getValue());
        }

        header.setHeader("Upgrade", "websocket");
        header.setHeader("Connection", "Upgrade");
        header.setHeader("Sec-WebSocket-Version", Constants.SEC_WEBSOCKET_VERSION);
        nonce = new byte[16];
        random.nextBytes(nonce);
        header.setHeader("Sec-WebSocket-Key", Base64.getEncoder().encodeToString(nonce));

        parentWriter.sendData(header.encode());
        state = State.CONNECTING;
    }

    @Override
    public void setOnReceived(BinaryReaderListener callback) {
        this.onReceivedCallback = callback;
    }

    @Override
    public void sendData(ByteBuffer toSend) {
        if (state == State.CONNECTED) {
            parentWriter.sendData(toSend);
        } else if(state == State.CONNECTING) {
            toSendLater.offer(toSend);
        } else {
            throw new IllegalStateException("Not connected, can't send data");
        }
    }

    @Override
    public void disconnect() {
        parentWriter.disconnect();
    }

    @Override
    public void onReceived(ByteBuffer data) {
        if(state == State.CONNECTING) {
            HTTPResponseHeader response = decoder.tryDecode(data);
            if(response != null) {
                if(response.getStatusCode() == Constants.HTTP_SWITCHING_PROTOCOLS) {
                    state = State.CONNECTED;

                    while(!toSendLater.isEmpty()) {
                        parentWriter.sendData(toSendLater.poll());
                    }
                }
            }
        }

        if(state == State.CONNECTED && onReceivedCallback != null) {
            onReceivedCallback.onReceived(data);
        }
    }
}
