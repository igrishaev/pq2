package org.pq;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.UUID;

public class Decode {

    public static String readString(final ByteBuffer bb) {
        final int len = bb.get(12);
        final byte[] ba = new byte[len];
        bb.get(16, ba);
        return new String(ba, StandardCharsets.UTF_8);
    }

    public static Object parseVal(final ByteBuffer bb) {
        bb.rewind();
        int isNull = bb.getInt();
        if (isNull == 1) {
            return null;
        }
        int format = bb.getInt();

        Native.FORMAT frmt = Native.FORMAT.of(format);

        int oid = bb.getInt();

        return switch (oid) {
            case OID.INT8 -> switch (frmt) {
                case Native.FORMAT.TXT -> {
                    final var s = readString(bb);
                    yield Integer.parseInt(s);
                }
                case Native.FORMAT.BIN -> bb.getLong();
            };

            case OID.INT4 -> bb.getInt(); // TODO
            case OID.TEXT -> readString(bb);
            case OID.DATE -> {
                int days = bb.getInt();
                yield LocalDate.ofEpochDay(days); // TODO
            }
            case OID.UUID -> switch (frmt) {
                case Native.FORMAT.BIN -> {
                    long bits1 = bb.getLong();
                    long bits2 = bb.getLong();
                    yield new UUID(bits1, bits2);
                }
                case Native.FORMAT.TXT -> {
                    final var s = readString(bb);
                    yield UUID.fromString(s);
                }
            };
            default -> throw new RuntimeException("aaa");
        };
    }

    public static int encodeVal(final ByteBuffer bb, final int oid, final int format) {
        // nparams
        // oids
        // values
        // lengths
        // formats
        // result format
        return 0;
    }

    public static void encodeValues(final ByteBuffer bb, int[] oids, int[] formats, final Object[] values) {
        
    }

}
