package nz.timo.websocket.http;

import nz.timo.websocket.WebSocketConnectionException;
import nz.timo.websocket.binary.BinaryReaderListener;
import nz.timo.websocket.binary.BinaryReader;
import nz.timo.websocket.binary.BinaryWriter;

import java.net.URI;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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

    private String encodedNonce;

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

        if(!uri.getScheme().equals(Constants.URL_SCHEME_WEBSOCKET)
                && !uri.getScheme().equals(Constants.URL_SCHEME_WEBSOCKET_SECURE)) {
            throw new IllegalArgumentException("Not a WebSocket URL");
        }

        HTTPRequestHeader header = new HTTPRequestHeader(uri.getPath(), uri.getHost(), uri.getPort(), HTTPMethod.GET);

        for(Map.Entry<String, String> e : headers.entrySet()) {
            header.setHeader(e.getKey(), e.getValue());
        }

        header.setHeader(Constants.HEADER_NAME_UPGRADE, Constants.CONNECTION_UPGRADE_TYPE);
        header.setHeader(Constants.HEADER_NAME_CONNECTION, Constants.WEBSOCKET_CONNECTION_UPGRADE);
        header.setHeader(Constants.HEADER_NAME_SEC_WEBSOCKET_VERSION, Constants.SEC_WEBSOCKET_VERSION);
        byte[] nonce = new byte[16];
        random.nextBytes(nonce);

        encodedNonce = Base64.getEncoder().encodeToString(nonce);
        header.setHeader(Constants.HEADER_NAME_SEC_WEBSOCKET_KEY, encodedNonce);

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
                    String secWebsocketAccept = response.getHeaderFields().get(Constants.HEADER_NAME_SEC_WEBSOCKET_ACCEPT);
                    if(!secWebsocketAccept.equals(getExpectedAccept())) {
                        throw new WebSocketConnectionException("Invalid value for Sec-WebSocket-Accept");
                    }

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

    @Override
    public void onDisconnected() {
        if(onReceivedCallback != null) {
            onReceivedCallback.onDisconnected();
        }
    }

    private String getExpectedAccept() {
        String value = encodedNonce + Constants.WEBSOCKET_MAGIC_STRING;
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA1");
        } catch(NoSuchAlgorithmException e) {
            throw new WebSocketConnectionException(e);
        }

        byte[] sha1sum = digest.digest(value.getBytes());
        return Base64.getEncoder().encodeToString(sha1sum);
    }
}
