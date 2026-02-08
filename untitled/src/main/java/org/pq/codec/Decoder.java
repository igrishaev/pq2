package org.pq.codec;

import org.pq.api.OID;

import java.nio.ByteBuffer;

public class Decoder {

    public static Object decodeBin(final int oid, final int len, final ByteBuffer bb) {
        return switch (oid) {
            case OID.INT2 -> bb.getShort();
            case OID.INT4 -> bb.getInt();
            case OID.INT8 -> bb.getLong();
            case OID.FLOAT4 -> bb.getFloat();
            case OID.FLOAT8 -> bb.getDouble();
            default -> {
                final byte[] ba = new byte[len];
                bb.get(ba);
                yield ba;
            }
        };
    }

    public static Object decodeTxt(final int oid, final String string) {
        return switch (oid) {
            case OID.INT2 -> Short.parseShort(string);
            case OID.INT4 -> Integer.parseInt(string);
            case OID.INT8 -> Long.parseLong(string);
            case OID.FLOAT4 -> Float.parseFloat(string);
            case OID.FLOAT8 -> Double.parseDouble(string);
            default -> string;
        };
    }

}
