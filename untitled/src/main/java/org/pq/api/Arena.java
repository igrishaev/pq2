package org.pq.api;

import org.pq.Native2;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
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
        final long ptr = bb.getLong();
        final long NULL = bb.getLong();

        return new Arena(bb, ptr, BO_JVM, BO_CPP, NULL);
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
    public void putInt(final int i) {
        bb.putInt(i);
    }
    public void putInt(final int index, final int i) {
        bb.putInt(index, i);
    }
    public void putLong(final int index, final long l) {
        bb.putLong(index, l);
    }
    public int position() {
        return bb.position();
    }
    public void skip(final int len) {
        final int pos = bb.position();
        bb.position(pos + len);
    }
    public String getLenString() {
        final int len = bb.getInt();
        final byte[] ba = new byte[len];
        bb.get(ba);
        return new String(ba, StandardCharsets.UTF_8);
    }
    public void get(final byte[] ba) {
        bb.get(ba);
    }
    public String getString(final int len) {
        final byte[] ba = new byte[len];
        bb.get(ba);
        return new String(ba, StandardCharsets.UTF_8);
    }
    public String getString(final int index, final int len) {
        final byte[] ba = new byte[len];
        bb.get(index, ba);
        return new String(ba, StandardCharsets.UTF_8);
    }
    @SuppressWarnings("unused")
    void debug(final int len) {
        final byte[] ba = new byte[len];
        bb.get(0, ba);
        System.out.println(Arrays.toString(ba));
    }
}
