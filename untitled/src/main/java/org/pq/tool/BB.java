package org.pq.tool;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class BB {

    public static ByteBuffer putString(final ByteBuffer bb, final String value, final Charset charset) {
        bb.put(value.getBytes(charset));
        return bb;
    }

    public static ByteBuffer putString(final ByteBuffer bb, final String value) {
        bb.put(value.getBytes(StandardCharsets.UTF_8));
        return bb;
    }

    public static ByteBuffer skip(final ByteBuffer bb, final int len) {
        final int pos = bb.position();
        bb.position(pos + len);
        return bb;
    }
}