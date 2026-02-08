package org.pq.api;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class BB {
    public static String getString(final ByteBuffer bb) {
        final int len = bb.getInt();
        final byte[] ba = new byte[len];
        bb.get(ba);
        return new String(ba, StandardCharsets.UTF_8);
    }
}
