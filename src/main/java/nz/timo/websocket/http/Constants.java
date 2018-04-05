package nz.timo.websocket.http;

public final class Constants {

    public static final String HTTP_VERSION_STRING = "HTTP/1.1";
    public static final String HTTP_NEW_LINE = "\r\n";
    public static final int HTTP_SWITCHING_PROTOCOLS = 101;
    public static final String SEC_WEBSOCKET_VERSION = "13";
    public static final String CONNECTION_UPGRADE_TYPE = "websocket";
    public static final String WEBSOCKET_MAGIC_STRING = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";

    public static final String HEADER_NAME_SEC_WEBSOCKET_VERSION = "Sec-WebSocket-Version";
    public static final String HEADER_NAME_SEC_WEBSOCKET_KEY = "Sec-WebSocket-Key";
    public static final String HEADER_NAME_SEC_WEBSOCKET_ACCEPT = "Sec-WebSocket-Accept";
    public static final String HEADER_NAME_CONNECTION = "Connection";
    public static final String HEADER_NAME_UPGRADE = "Upgrade";

    public static final String URL_SCHEME_WEBSOCKET = "ws";
    public static final String WEBSOCKET_CONNECTION_UPGRADE = "Upgrade";
    public static final String URL_SCHEME_WEBSOCKET_SECURE = "wss";
    public static final String DEFAULT_HTTP_REQUEST_PATH = "/";

    private Constants() {

    }
}
