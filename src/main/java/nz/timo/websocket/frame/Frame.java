package nz.timo.websocket.frame;

import java.nio.ByteBuffer;

public class Frame {
    private boolean fin;
    private boolean mask;
    private Opcode opcode;
    private int payloadLength;
    private int maskingKey;
    private ByteBuffer data;

    public Frame() {

    }

    public boolean isFinalFragment() {
        return fin;
    }

    public void setFinalFragment(boolean fin) {
        this.fin = fin;
    }

    public boolean isDataMasked() {
        return mask;
    }

    public void setIsMasked(boolean mask) {
        this.mask = mask;
    }

    public Opcode getOpcode() {
        return opcode;
    }

    public void setOpcode(Opcode opcode) {
        this.opcode = opcode;
    }

    public int getPayloadLength() {
        return payloadLength;
    }

    public void setPayloadLength(int payloadLength) {
        this.payloadLength = payloadLength;
    }

    public int getMaskingKey() {
        return maskingKey;
    }

    public void setMaskingKey(int maskingKey) {
        this.maskingKey = maskingKey;
    }

    public ByteBuffer getData() {
        return data;
    }

    public void setData(ByteBuffer data) {
        this.data = data;
    }
}
