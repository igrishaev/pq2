package org.pq.codec;

import org.pq.api.OID;

import java.nio.ByteBuffer;
import java.util.UUID;

import static org.pq.tool.BB.*;
import static org.pq.tool.Cast.*;

public class Encoder {

    private static RuntimeException error(final String template, Object... args) {
        return new RuntimeException(String.format(template, args));
    }

    public static int encodeBin(final int oid, final Object x, final ByteBuffer bb) {
        return switch (oid) {
            case OID.INT2 -> putShort(bb, castShort(x));
            case OID.INT4 -> putInt(bb, castInteger(x));
            case OID.INT8 -> putLong(bb, castLong(x));
            case OID.FLOAT4 -> putFloat(bb, castFloat(x));
            case OID.FLOAT8 -> putDouble(bb, castDouble(x));
            case OID.UUID -> {
                final UUID uuid = castUUID(x);
                final long bits_hi = uuid.getMostSignificantBits();
                final long bits_lo = uuid.getLeastSignificantBits();
                yield putLong(bb, bits_hi) + putLong(bb, bits_lo);
            }
            default -> throw error("Don't know how to binary-encode a value: %s", x);
        };
    }

    public static Object encodeText(final int oid, final Object x, final ByteBuffer bb) {
        return switch (oid) {
            case OID.INT2 -> putString(bb, String.valueOf(castShort(x)));
            case OID.INT4 -> putString(bb, String.valueOf(castInteger(x)));
            case OID.UUID -> putString(bb, castUUID(x).toString());
            default -> throw error("Don't know how to text-encode a value: %s", x);
        };
    }




}
