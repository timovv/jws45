jws45
=====

simple java websocket client.
needs documentation. needs testing. **not recommended for production use, this is a personal project**.

Features:
* non-blocking model based on `java.nio`
* tls/ssl support using `javax.ssl.SSLEngine`
* no third-party dependencies
* (mostly) rfc 6455 compliant

Goals:

This library was intended to make a client for the Discord chat system. I'm still working on this and it might take a
while so I've decided to share the websocket part now - when the rest of the Discord stuff is done this will become
part of that project instead.

Simple usage example: it prints binary messages received to console in hex and echoes back text messages:

    client = new WebSocketClient("wss://localhost:9999/") {
        @Override
        public void onTextReceived(String text) {
            System.out.println("Received a text message: " + text);
            getConnection().sendText(text);
        }

        @Override
        public void onBinaryReceived(ByteBuffer binary) {
            System.out.print("Receved a binary message: ");
            while(binary.hasRemaining()) {
                System.out.printf("%2x ", binary.get());
            }

            System.out.println();
        }

        @Override
        public void onDisconnected(ConnectionCloseInfo info) {
            System.out.println("Disconnected: " + info.getStatusCode());
        }
    };
    
    client.pollForever();
