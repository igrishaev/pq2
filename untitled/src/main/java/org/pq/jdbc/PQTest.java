package org.pq.jdbc;

import org.pq.Native;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.UUID;

public class PQTest {

    private long conn;
    private ByteBuffer bb;
    private long ptr;

    public PQTest() {
        this.bb = ByteBuffer.allocateDirect(6400);
        this.ptr = Native.getBBAddress(bb);
        this.conn = Native.PQconnectdb("host=localhost port=15432 dbname=test user=test password=test");
    }

    public void test() {
        int num;
        Object[] arr;
        String s;
        byte[] bs = new byte[14];

        Object obj;
        long bits1, bits2;
        var result = Native.PQexec(conn, "select x::text as a from generate_series(1, 9999) as seq(x)");
        var tuples = Native.PQntuples(result);
        for (int row = 0; row < tuples; row++) {

            Native.fetchField(result, ptr, row, 0);
            // Native.writeBBPTR(ptr);
            // this.bb.get(0, bs);
            // s = this.bb.asCharBuffer().toString();
            // s = new String(bs, StandardCharsets.UTF_8);

            // this.bb.reset();
            this.bb.rewind();
            System.out.println("--------------");
            System.out.println(bb.getInt());
            System.out.println(bb.getInt());
            System.out.println(bb.getInt());
            System.out.println(bb.getInt());

            if (row > 10) {System.exit(0);}


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
            // num = Native.getInt(result, i, 0);
            // Native.PQgetisnull(result, i, 0);
            // obj = Native.getValue(result, i, 0);
        }
    }

    public void bench() throws SQLException {
        long start = System.currentTimeMillis();
        for (int i = 0; i < 1000; i++) {
            test();
        }
        var bb = ByteBuffer.allocateDirect(99);

        // System.out.println(Native.getBBAddress(bb));

        long end = System.currentTimeMillis();
        System.out.println(end - start);
        System.out.println(bb.get());
    }

    public static void main(String... args) throws SQLException {
        var t = new PQTest();
        t.bench();
    }
}
