package org.pq;

import org.pq.api.PGResult;
import org.pq.api.PQClient;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.sql.SQLException;

public class PQTest {

    private PQClient client;

    public static String sql = """
            select
              x::int4                  as int4,
              x::int8                  as int8,
              x::numeric               as numeric,
              x::text || 'foobar'      as line,
              x > 100500               as bool,
              now()                    as ts,
              now()::date              as date,
              now()::time              as time,
              '2024-01-13 21:08:57.593323+05:30'::timestamptz,
              '2024-01-13 21:08:57.593323+05:30'::timestamp,
              null                     as nil
            from
              generate_series(1,9999) as s(x)
            
            """;

    public PQTest() {
        this.client = PQClient.of("host=localhost port=15432 dbname=test user=test password=test");
    }

    public void test() {

        Object obj;
        try (PGResult res = client.exec(sql)) {
            for (int row: res) {
                for (int col = 1; col < 11; col++) {
                    obj = res.getObject(row, col);
                }
            }
        }

        // Decode.encodeValues(bb, ptr, new Object[]{999, 99, 3}, new int[]{23,23,23}, new int[]{1,1,1}, 0);
        // long res = Native.execWithParams(conn, "select $1 as a, $2 as b, $3 as c", ptr);

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
