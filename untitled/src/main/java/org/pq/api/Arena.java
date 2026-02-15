package org.pq.api;

import org.pq.Native2;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

public record Arena (
        ByteBuffer bb,
        long ptr,
        ByteOrder BO_JVM,
        ByteOrder BO_CPP,
        long NULL
) {

    public static Arena of(final int size) {
        final ByteBuffer bb = ByteBuffer.allocateDirect(size);
        final int initStatus = Native2.initByteBuffer(bb);
        if (initStatus != 0) {
            throw PQError.error("byte buffer init error, code: %s", initStatus);
        }
        final ByteOrder BO_JVM = ByteOrder.BIG_ENDIAN;

        final byte lead = bb.get(0);
        final ByteOrder BO_CPP = (lead == 1) ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN;
        bb.order(BO_CPP);

        bb.getInt();
        final long bbPtr = bb.getLong();
        final long NULL = bb.getLong();

        return new Arena(bb, bbPtr, BO_JVM, BO_CPP, NULL);
    }

    public void rewind() {
        bb.rewind();
    }
    public void orderJVM() {
        bb.order(BO_JVM);
    }
    public void orderCPP() {
        bb.order(BO_CPP);
    }
    public int getInt() {
        return bb.getInt();
    }
    public long getLong() {
        return bb.getLong();
    }
    @SuppressWarnings("unused")
    void debug(final int len) {
        final byte[] ba = new byte[len];
        bb().get(0, ba);
        System.out.println(Arrays.toString(ba));
    }
}
