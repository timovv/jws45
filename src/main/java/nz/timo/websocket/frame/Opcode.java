package nz.timo.websocket.frame;

public enum Opcode {
    CONTINUATION    (0x0),
    TEXT            (0x1),
    BINARY          (0x2),
    CONNECTION_CLOSE(0x8),
    PING            (0x9),
    PONG            (0xa);

    private final byte code;

    private static final Opcode[] LOOKUP;

    static {
        LOOKUP = new Opcode[0x100];

        for(Opcode opcode : values()) {
            LOOKUP[opcode.getCode()] = opcode;
        }
    }

    Opcode(int code) {
        this.code = (byte)code;
    }

    public byte getCode() {
        return code;
    }

    public static Opcode fromCode(byte code) {
        return LOOKUP[code];
    }
}
