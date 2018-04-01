package nz.timo.websocket.message;

import java.nio.ByteBuffer;

public class ControlMessageHandler implements IncomingConnection, OutgoingConnection, WebSocketMessageHandler {

    private final OutgoingConnection outgoingConnection;
    private WebSocketMessageHandler handler;
    private boolean disconnecting = false;

    public ControlMessageHandler(IncomingConnection incomingConnection, OutgoingConnection outgoingConnection) {
        this.outgoingConnection = outgoingConnection;
        incomingConnection.setHandler(this);
    }

    @Override
    public void setHandler(WebSocketMessageHandler handler) {
        this.handler = handler;
    }

    @Override
    public void sendText(String message) {
        outgoingConnection.sendText(message);
    }

    @Override
    public void sendBinary(ByteBuffer data) {
        outgoingConnection.sendBinary(data);
    }

    @Override
    public void sendPing(ByteBuffer data) {
        outgoingConnection.sendPing(data);
    }

    @Override
    public void sendPong(ByteBuffer data) {
        outgoingConnection.sendPong(data);
    }

    @Override
    public void disconnect(ConnectionCloseInfo info) {
        disconnecting = true;
        outgoingConnection.disconnect(info);
    }

    @Override
    public void onTextReceived(String text) {
        if(handler != null) {
            handler.onTextReceived(text);
        }
    }

    @Override
    public void onBinaryReceived(ByteBuffer binary) {
        if(handler != null) {
            handler.onBinaryReceived(binary);
        }
    }

    @Override
    public void onDisconnected(ConnectionCloseInfo info) {
        if(!disconnecting) {
            outgoingConnection.disconnect(info);
        }

        if (handler != null) {
            handler.onDisconnected(info);
        }
    }

    @Override
    public void onPing(ByteBuffer pingData) {
        outgoingConnection.sendPong(pingData);
        if(handler != null) {
            handler.onPing(pingData);
        }
    }

    @Override
    public void onPong(ByteBuffer pongData) {
        if(handler != null) {
            handler.onPong(pongData);
        }
    }
}
