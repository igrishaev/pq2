package org.pq.api;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

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
}
