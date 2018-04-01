package nz.timo.websocket.frame;

public enum PayloadLengthType {
    BIT_7(0),
    BIT_16(2),
    BIT_64(8);

    private final int bytesRequired;

    PayloadLengthType(int bytesRequired) {
        this.bytesRequired = bytesRequired;
    }

    public int getBytesRequired() {
        return bytesRequired;
    }
}
