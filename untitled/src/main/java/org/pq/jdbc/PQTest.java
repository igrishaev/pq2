package org.pq.jdbc;

import org.pq.Native;

import java.nio.ByteBuffer;
import java.sql.SQLException;

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
        var result = Native.PQexec(conn, "select x::text as a from generate_series(1, 9999) as seq(x)");
        var len = Native.PQntuples(result);
        for (int i = 0; i < len; i++) {
            // bb = Native.getBB(result, i, 0);
//            bs = Native.getBytes(result, i, 0);
            s = Native.getString(result, i, 0);
////            s = Native.getString(result, i, 0);
////            s = Native.getString(result, i, 0);
            // arr = Native.getTuple(result, i);
            // num = Native.getInt(result, i, 0);
            // num = Native.getInt(result, i, 0);
            // num = Native.getValue(result, i, 0);
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
