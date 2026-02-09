package org.pq.codec;

import org.pq.api.FORMAT;
import org.pq.api.OID;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
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

    public static void encodeBB(final ByteBuffer bb, long bbPtr, Object[] params, int[] oids, int[] formats, int resultFormat) {

        rewind(bb);
        bb.order(ByteOrder.LITTLE_ENDIAN);

        int nParams = params.length;

        // nParams
        putInt(bb, nParams);

        // paramTypes
        for (int i = 0; i < nParams; i++) {
            putInt(bb, oids[i]);
        }

        // paramValues
        int posPtr = bb.position();
        skip(bb, 8 * nParams);

        // paramLengths
        int posLen = bb.position();
        skip(bb, 4 * nParams);

        // paramFormats
        for (int i = 0; i < nParams; i++) {
            putInt(bb, formats[i]);
        }

        // resultFormat
        putInt(bb, resultFormat);

        // update length and pointers
        int len;
        int pos;
        FORMAT format;
        for (int i = 0; i < nParams; i++) {
            format = FORMAT.of(formats[i]);
            pos = bb.position();
            bb.order(ByteOrder.BIG_ENDIAN);
            switch (format) {
                case TXT -> encodeText(oids[i], params[i], bb);
                case BIN -> encodeBin(oids[i], params[i], bb);
            }
            len = pos - bb.position();
            bb.order(ByteOrder.LITTLE_ENDIAN);
            bb.putInt(posLen + i * 4, len);
            bb.putLong(posPtr + i * 8, bbPtr + pos);
        }

    }




}
