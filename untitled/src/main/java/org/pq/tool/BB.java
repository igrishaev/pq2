package org.pq.tool;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class BB {

    public static int putShort(final ByteBuffer bb, final short s) {
        final int pos = bb.position();
        bb.putShort(s);
        return bb.position() - pos;
    }

    public static int putInt(final ByteBuffer bb, final int i) {
        final int pos = bb.position();
        bb.putInt(i);
        return bb.position() - pos;
    }

    public static int putLong(final ByteBuffer bb, final long l) {
        final int pos = bb.position();
        bb.putLong(l);
        return bb.position() - pos;
    }

    public static int put(final ByteBuffer bb, final byte[] ba) {
        final int pos = bb.position();
        bb.put(ba);
        return bb.position() - pos;
    }

    public static int putFloat(final ByteBuffer bb, final float f) {
        final int pos = bb.position();
        bb.putFloat(f);
        return bb.position() - pos;
    }

    public static int putDouble(final ByteBuffer bb, final double d) {
        final int pos = bb.position();
        bb.putDouble(d);
        return bb.position() - pos;
    }

    public static int putString(final ByteBuffer bb, final String value) {
        final int pos = bb.position();
        bb.put(value.getBytes(StandardCharsets.UTF_8));
        return bb.position() - pos;
    }
}