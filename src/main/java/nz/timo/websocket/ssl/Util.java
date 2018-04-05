package nz.timo.websocket.ssl;

import java.nio.ByteBuffer;
import java.util.Queue;

final class Util {
    private Util() {

    }

    static void fillBuffer(Queue<ByteBuffer> src, ByteBuffer dst) {
        while(!src.isEmpty() && dst.hasRemaining()) {
            ByteBuffer next = src.peek();
            int oldLimit = next.limit();
            if(next.remaining() > dst.remaining()) {
                next.limit(next.position() + dst.remaining());
            }

            dst.put(next);
            next.limit(oldLimit);

            if(!next.hasRemaining()) {
                src.poll();
            }
        }
    }
}
