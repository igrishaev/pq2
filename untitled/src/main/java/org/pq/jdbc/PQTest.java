package org.pq.jdbc;

import org.pq.Native;

import java.nio.ByteBuffer;
import java.sql.SQLException;
import java.util.UUID;

public class PQTest {

    private long conn;

    public PQTest() {
        this.conn = Native.PQconnectdb("host=localhost port=15432 dbname=test user=test password=test");
    }

    public void test() {
        int num;
        Object[] arr;
        String s;
        byte[] bs;
        ByteBuffer bb;
        Object obj;
        long bits1, bits2;
        var result = Native.PQexec(conn, "select '122b85b3-8385-48a6-9973-036cf8b04eac'::uuid as a from generate_series(1, 9999) as seq(x)");
        var len = Native.PQntuples(result);
        for (int i = 0; i < len; i++) {

            bits1 = Native.asLong(result, i, 0);
            bits2 = Native.asLong(result, i, 0, 8);

            obj = new UUID(bits1, bits2);

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
        long end = System.currentTimeMillis();
        System.out.println(end - start);
    }

    public static void main(String... args) throws SQLException {
        var t = new PQTest();
        t.bench();
    }
}
