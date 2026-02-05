package org.pq.jdbc;

import org.pq.Native;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.UUID;

public class PQTest {

    private long conn;
    private ByteBuffer bb;
    private long ptr;

    public PQTest() {
        this.bb = ByteBuffer.allocateDirect(6400);
        this.bb.order(ByteOrder.BIG_ENDIAN);
        this.ptr = Native.getBBAddress(bb);
        this.conn = Native.PQconnectdb("host=localhost port=5432 dbname=book user=book password=book");
    }

    public static Object parseVal(final ByteBuffer bb) {
        bb.rewind();
        int isNull = bb.getInt();
        if (isNull == 1) {
            return null;
        }
        int format = bb.getInt();
        int oid = bb.getInt();
        int len = bb.getInt();

        return switch (oid) {
            case 20 -> bb.getLong();
            case 23 -> bb.getInt();
            case 25 -> {
                byte[] buf = new byte[len];
                bb.get(buf);
                yield new String(buf, StandardCharsets.UTF_8);
            }
            case 1082 -> {
                int days = bb.getInt();
                yield LocalDate.ofEpochDay(days);
            }
            case 2950 -> {
                long bits1 = bb.getLong();
                long bits2 = bb.getLong();
                yield new UUID(bits1, bits2);
            }
            default -> throw new RuntimeException("aaa");
        };
    }

    public void test() {
        int num;
        Object[] arr;
        String s;
        byte[] bs = new byte[14];

        Object obj;
        long bits1, bits2;
        var result = Native.PQexec(conn, "select now()::date from generate_series(1, 9999) as seq(x)");
        var tuples = Native.PQntuples(result);
        for (int row = 0; row < tuples; row++) {

            // Native.writeBBPTR(ptr);
            // this.bb.get(0, bs);
            // s = this.bb.asCharBuffer().toString();
            // s = new String(bs, StandardCharsets.UTF_8);


            // this.bb.reset();
//            this.bb.rewind();
//            Native.fetchField(result, ptr, row, 0);
//            bb.getInt();
//            bb.getInt();
//            bb.getInt();
//            bb.getInt();
//            bb.getInt();
//
//            this.bb.rewind();
//            Native.fetchField(result, ptr, row, 0);
//            bb.getInt();
//            bb.getInt();
//            bb.getInt();
//            bb.getInt();
//            bb.getInt();
//
//            this.bb.rewind();
//            Native.fetchField(result, ptr, row, 0);
//            bb.getInt();
//            bb.getInt();
//            bb.getInt();
//            bb.getInt();
//            bb.getInt();
//
//            this.bb.rewind();
//            Native.fetchField(result, ptr, row, 0);
//            bb.getInt();
//            bb.getInt();
//            bb.getInt();
//            bb.getInt();
//            bb.getInt();
//
//            this.bb.rewind();
//            Native.fetchField(result, ptr, row, 0);
//            bb.getInt();
//            bb.getInt();
//            bb.getInt();
//            bb.getInt();
//            bb.getInt();

            for (int j = 0; j < 10; j++) {
//                Native.getInt(result, row, 0);
                Native.fetchField(result, ptr, row, 0);
                obj = parseVal(this.bb);
            }


//            Native.fetchField(result, ptr, row, 1);
//            obj = parseVal(this.bb);
//            Native.fetchField(result, ptr, row, 2);
//            obj = parseVal(this.bb);
//            Native.fetchField(result, ptr, row, 3);
//            obj = parseVal(this.bb);
//            Native.fetchField(result, ptr, row, 4);
//            obj = parseVal(this.bb);
//            Native.fetchField(result, ptr, row, 5);
//            obj = parseVal(this.bb);
//            Native.fetchField(result, ptr, row, 6);
//            obj = parseVal(this.bb);
//            Native.fetchField(result, ptr, row, 7);
//            obj = parseVal(this.bb);
//            Native.fetchField(result, ptr, row, 8);
//            obj = parseVal(this.bb);
//            Native.fetchField(result, ptr, row, 9);
//            obj = parseVal(this.bb);



//            this.bb.rewind();
//            System.out.println(obj);
//            System.out.println(bb.getInt());
//            System.out.println(bb.getInt());
//            System.out.println(bb.getInt());
//            System.out.println(bb.getInt());
//            System.out.println(bb.getInt());
//            System.out.println(bb.getInt());

//             if (row > 10) {System.exit(0);}


            // s = new String(bs);
            // System.out.println(s);

            // bits1 = Native.asLong(result, i, 0);
//            bits2 = Native.asLong(result, i, 0, 8);
//
//            obj = new UUID(bits1, bits2);

            // bb = Native.getBB(result, i, 0);
//            bs = Native.getBytes(result, i, 0);
            // s = Native.getString(result, i, 0);
            // arr = Native.getTuple(result, i);
//            num = Native.getInt(result, row, 0);
//            num = Native.getInt(result, row, 1);
//            num = Native.getInt(result, row, 2);
//            num = Native.getInt(result, row, 3);
//            num = Native.getInt(result, row, 4);
//            num = Native.getInt(result, row, 5);
//            num = Native.getInt(result, row, 6);
//            num = Native.getInt(result, row, 7);
//            num = Native.getInt(result, row, 8);
//            num = Native.getInt(result, row, 9);

            // Native.PQgetisnull(result, i, 0);
            // obj = Native.getValue(result, i, 0);
        }
    }

    public void bench() throws SQLException {
        long start = System.currentTimeMillis();
        for (int i = 0; i < 1000; i++) {
            test();
        }
//        var bb = ByteBuffer.allocateDirect(99);

        // System.out.println(Native.getBBAddress(bb));

        long end = System.currentTimeMillis();
        System.out.println(end - start);
//        System.out.println(bb.get());
    }

    public static void main(String... args) throws SQLException {
        var t = new PQTest();
        t.bench();
    }
}
