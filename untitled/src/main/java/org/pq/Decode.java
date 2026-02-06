package org.pq;

import javax.swing.plaf.PanelUI;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.UUID;

public class Decode {

    public static String readString(final ByteBuffer bb, final int len) {
        final byte[] ba = new byte[len];
        bb.get(ba);
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
        int len = bb.getInt();

        return switch (oid) {
            case OID.INT8 -> switch (frmt) {
                case Native.FORMAT.TXT -> {
                    final var s = readString(bb, len);
                    yield Integer.parseInt(s);
                }
                case Native.FORMAT.BIN -> bb.getLong();
            };

            case OID.INT4 -> bb.getInt(); // TODO
            case OID.TEXT -> readString(bb, len);
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
                    final var s = readString(bb, len);
                    yield UUID.fromString(s);
                }
            };
            default -> throw new RuntimeException("aaa");
        };
    }

    public static int encodeVal(final ByteBuffer bb, Object param, final int oid, final Native.FORMAT format) {
        return switch (oid) {
            case OID.INT4 -> switch (format) {
                case TXT -> {
                    if (param instanceof Integer i) {
                        var s = i.toString();
                        var ba = s.getBytes(StandardCharsets.UTF_8);
                        bb.put(ba);
                        yield ba.length;
                    } else {
                        throw new RuntimeException("aaa");
                    }
                }
                case BIN -> {
                    if (param instanceof Integer i) {
                        bb.putInt(i);
                        yield 4;
                    } else {
                        throw new RuntimeException("aaa");
                    }
                }
            };
            default -> throw new RuntimeException("bbb");
        };
    }

    public static void encodeValues(final ByteBuffer bb, long ptr, Object[] params, int[] oids, int[] formats, int format) {

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
        bb.position(posPtr + 8 * nParams);

        // paramLengths
        int posLen = bb.position();
        bb.position(posLen + 4 * nParams);

        // paramFormats
        for (int i = 0; i < nParams; i++) {
            bb.putInt(formats[i]);
        }

        // resultFormat
        // bb.putInt(format);

        // length and pointers
        int len;
        int pos;
        for (int i = 0; i < nParams; i++) {
            pos = bb.position();
            bb.order(ByteOrder.BIG_ENDIAN);
            len = encodeVal(bb, params[i], oids[i], Native.FORMAT.of(formats[i]));
            bb.order(ByteOrder.LITTLE_ENDIAN);
            bb.putInt(posLen + i * 4, len);
            bb.putLong(posPtr + i * 8, ptr + pos);
        }

    }

    public static void main(String... args) {
        var bb = ByteBuffer.allocate(128);
        encodeValues(bb, 0, new Object[]{1, 99, 3}, new int[]{23,23,23}, new int[]{0,0,0}, 1);
        System.out.println(Arrays.toString(bb.array()));
    }

}
