package org.pq.codec;

import java.nio.ByteBuffer;

public class NumberBin {

    public static short decodeShort(final ByteBuffer bb) {
        return bb.getShort();
    }

    public static int decodeInt(final ByteBuffer bb) {
        return bb.getInt();
    }

    public static long decodeLong(final ByteBuffer bb) {
        return bb.getLong();
    }

    public static float decodeFloat(final ByteBuffer bb) {
        return bb.getFloat();
    }

    public static double decodeDouble(final ByteBuffer bb) {
        return bb.getDouble();
    }

    public static byte decodeByte(final ByteBuffer bb) {
        return bb.get();
    }



}
