package org.pq.api;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

public record Arena (
        ByteBuffer bb,
        long bbPtr,
        ByteOrder BO_JVM,
        ByteOrder BO_CPP,
        long NULL
) {
    public void rewind() {
        bb.rewind();
    }
    public void orderJVM() {
        bb.order(BO_JVM);
    }
    public void orderCPP() {
        bb.order(BO_CPP);
    }
    @SuppressWarnings("unused")
    void debug(final int len) {
        final byte[] ba = new byte[len];
        bb().get(0, ba);
        System.out.println(Arrays.toString(ba));
    }
}
