package nz.timo.websocket.frame;

public final class Constants {

    public static final byte FIN_BIT = (byte)0x80;
    public static final byte OPCODE_MASK = (byte)0x0f;
    public static final byte MASK_BIT = (byte)0x80;
    public static final byte PAYLOAD_LENGTH_MASK = (byte)0x7f;
    public static final int PAYLOAD_LENGTH_16_BIT_INDICATOR = 126;
    public static final int PAYLOAD_LENGTH_64_BIT_INDICATOR = 127;

    private Constants() {}
}
