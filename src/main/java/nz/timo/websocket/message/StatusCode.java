package nz.timo.websocket.message;

import java.util.HashMap;
import java.util.Map;

public class StatusCode {
    public static final StatusCode NORMAL_CLOSURE = new StatusCode(1000, "Normal Closure");
    public static final StatusCode GOING_AWAY = new StatusCode(1001, "Going Away");
    public static final StatusCode PROTOCOL_ERROR = new StatusCode(1002, "Protocol error");
    public static final StatusCode UNSUPPORTED_DATA = new StatusCode(1003, "Unsupported Data");
    public static final StatusCode NO_STATUS_RECEIVED = new StatusCode(1005, "No Status Received");
    public static final StatusCode ABNORMAL_CLOSURE = new StatusCode(1006, "Abnormal Closure");
    public static final StatusCode INVALID_FRAME_PAYLOAD_DATA = new StatusCode(1007, "Invalid payload data");
    public static final StatusCode POLICY_VIOLATION = new StatusCode(1008, "Policy Violation");
    public static final StatusCode MESSAGE_TOO_BIG = new StatusCode(1009, "Message Too Big");
    public static final StatusCode MANDATORY_EXTENSION = new StatusCode(1010, "Mandatory Extension");
    public static final StatusCode INTERNAL_SERVER_ERROR = new StatusCode(1011, "Internal Server Error");
    public static final StatusCode TLS_HANDSHAKE = new StatusCode(1015, "TLS handshake");

    private static final Map<Integer, StatusCode> statusCodes = new HashMap<>();

    static {
        registerStatusCode(NORMAL_CLOSURE);
        registerStatusCode(GOING_AWAY);
        registerStatusCode(PROTOCOL_ERROR);
        registerStatusCode(UNSUPPORTED_DATA);
        registerStatusCode(NO_STATUS_RECEIVED);
        registerStatusCode(ABNORMAL_CLOSURE);
        registerStatusCode(INVALID_FRAME_PAYLOAD_DATA);
        registerStatusCode(POLICY_VIOLATION);
        registerStatusCode(MESSAGE_TOO_BIG);
        registerStatusCode(MANDATORY_EXTENSION);
        registerStatusCode(INTERNAL_SERVER_ERROR);
        registerStatusCode(TLS_HANDSHAKE);
    }


    private final int code;
    private final String description;

    private StatusCode(int code, String description) {
        this.code = code;

        this.description = description;
    }

    public static StatusCode getStatusCode(int code) {
        return getStatusCode(code, "Unknown");
    }

    public static StatusCode getStatusCode(int code, String defaultDescription) {
        StatusCode out = statusCodes.get(code);
        if(out == null) {
            out = new StatusCode(code, defaultDescription);
            registerStatusCode(out);
        }

        return out;
    }

    private static void registerStatusCode(StatusCode code) {
        if(statusCodes.containsKey(code.getCode())) {
            throw new IllegalArgumentException("StatusCode with that code already exists");
        }

        statusCodes.put(code.getCode(), code);
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}
