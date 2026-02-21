package org.pq.tool;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class BB {

    public static String getLenString(final ByteBuffer bb) {
        final int len = bb.getInt();
        final byte[] ba = new byte[len];
        bb.get(ba);
        return new String(ba, StandardCharsets.UTF_8);
    }

    public static ByteBuffer putString(final ByteBuffer bb, final String value, final Charset charset) {
        bb.put(value.getBytes(charset));
        return bb;
    }

    public static void putString(final ByteBuffer bb, final String value) {
        bb.put(value.getBytes(StandardCharsets.UTF_8));
    }

    public static void skip(final ByteBuffer bb, final int len) {
        final int pos = bb.position();
        bb.position(pos + len);
    }
}