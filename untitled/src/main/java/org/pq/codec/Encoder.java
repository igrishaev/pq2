package org.pq.codec;

import org.pq.api.FORMAT;
import org.pq.api.OID;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;
import java.util.UUID;

import static org.pq.tool.BB.*;
import static org.pq.tool.Cast.*;

public class Encoder {

    private static RuntimeException error(final String template, Object... args) {
        return new RuntimeException(String.format(template, args));
    }

    public static void encodeBin(final int oid, final Object x, final ByteBuffer bb) {
        switch (oid) {
            case OID.INT2 -> bb.putShort(castShort(x));
            case OID.INT4 -> bb.putInt(castInteger(x));
            case OID.INT8 -> bb.putLong(castLong(x));
            case OID.FLOAT4 -> bb.putFloat(castFloat(x));
            case OID.FLOAT8 -> bb.putDouble(castDouble(x));
            case OID.TEXT -> putString(bb, castString(x));
            case OID.UUID -> {
                final UUID uuid = castUUID(x);
                final long bits_hi = uuid.getMostSignificantBits();
                final long bits_lo = uuid.getLeastSignificantBits();
                bb.putLong(bits_hi);
                bb.putLong(bits_lo);
            }
            default -> throw error("Don't know how to binary-encode a value: %s", x);
        }
    }

    public static void encodeText(final int oid, final Object x, final ByteBuffer bb) {
        switch (oid) {
            case OID.INT2 -> putString(bb, String.valueOf(castShort(x)));
            case OID.INT4 -> putString(bb, String.valueOf(castInteger(x)));
            case OID.INT8 -> putString(bb, String.valueOf(castLong(x)));
            case OID.FLOAT4 -> putString(bb, String.valueOf(castFloat(x)));
            case OID.FLOAT8 -> putString(bb, String.valueOf(castDouble(x)));
            case OID.TEXT -> castString(x);
            case OID.UUID -> putString(bb, castUUID(x).toString());
            default -> throw error("Don't know how to text-encode a value: %s", x);
        }
    }

    public static void encodeBB(final ByteBuffer bb, long bbPtr, Object[] params, int[] oids, int[] formats, int resultFormat) {

        bb.rewind();
        bb.order(ByteOrder.LITTLE_ENDIAN);

        int nParams = params.length;

        // nParams
        bb.putInt(nParams);

        // paramTypes
        for (int i = 0; i < nParams; i++) {
            bb.putInt(oids[i]);
        }

        // paramValues
        int posPtr = bb.position();
        skip(bb, 8 * nParams);

        // paramLengths
        int posLen = bb.position();
        skip(bb, 4 * nParams);

        // paramFormats
        for (int i = 0; i < nParams; i++) {
            bb.putInt(formats[i]);
        }

        // resultFormat
        bb.putInt(resultFormat);

        // update lengths and pointers
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
            len = bb.position() - pos;
            bb.order(ByteOrder.LITTLE_ENDIAN);
            bb.putInt(posLen + i * 4, len);
            bb.putLong(posPtr + i * 8, bbPtr + pos);
        }

    }

    public static void encodePrepared(final ByteBuffer bb, long bbPtr, final int nParams, final List<Object> params, int[] oids) {

        bb.rewind();
        bb.order(ByteOrder.LITTLE_ENDIAN);

        final int format = 1;

        // nParams
        bb.putInt(nParams);

        // paramValues
        int posPtr = bb.position();
        skip(bb, 8 * nParams);

        // paramLengths
        int posLen = bb.position();
        skip(bb, 4 * nParams);

        // paramFormats
        for (int i = 0; i < nParams; i++) {
            bb.putInt(format);
        }

        // resultFormat
        bb.putInt(format);

        // update lengths and pointers
        int len;
        int pos;
        for (int i = 0; i < nParams; i++) {
            pos = bb.position();
            bb.order(ByteOrder.BIG_ENDIAN);
            switch (FORMAT.of(format)) {
                case TXT -> encodeText(oids[i], params.get(i), bb);
                case BIN -> encodeBin(oids[i], params.get(i), bb);
            }
            len = bb.position() - pos;
            bb.order(ByteOrder.LITTLE_ENDIAN);
            bb.putInt(posLen + i * 4, len);
            bb.putLong(posPtr + i * 8, bbPtr + pos);
        }

    }




}
