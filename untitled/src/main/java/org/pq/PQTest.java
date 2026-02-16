package org.pq;

import org.pq.api.PGResult;
import org.pq.api.PQClient;
import org.pq.api.Result;
import org.pq.api.Stmt2;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.sql.SQLException;
import java.util.List;

public class PQTest {

    private PQClient client;
    private Stmt2 statement;

    public static String sql = """
            select
              x::int4                  as int4,
              x::int8                  as int8,
              x::numeric               as numeric,
              x::text || 'foobar'      as line,
              x > 100500               as bool,
              gen_random_uuid()        as uuid,
              now()::date              as date,
              -- timestamptz
              -- time
              null                     as nil
            from
              generate_series(1,9999) as s(x)
            
            """;

    public PQTest() {
        this.client = PQClient.of("host=localhost port=15432 dbname=test user=test password=test");
        this.statement = client.prepare(sql);
    }

    public void test() {
        Object obj;
        try (final Result res = statement.execute(List.of())) {
            while (res.next()) {
                for (int col = 1; col < 8; col++) {
                    obj = res.getColumn(col);
                }
            }
        }
    }

    public void bench() throws SQLException {
        Runtime runtime = Runtime.getRuntime();
        long usedMemoryBefore = runtime.totalMemory() - runtime.freeMemory();
        System.out.println("Used Memory before: " + usedMemoryBefore / 1000000);

        long start = System.currentTimeMillis();
        for (int i = 0; i < 1000; i++) {
            test();
        }
//        var bb = ByteBuffer.allocateDirect(99);

        // System.out.println(Native.getBBAddress(bb));
        long usedMemoryAfter = runtime.totalMemory() - runtime.freeMemory();
        System.out.println("Memory increased: " + (usedMemoryAfter-usedMemoryBefore) / 1000000);

        long end = System.currentTimeMillis();
        System.out.println(end - start);
//        System.out.println(bb.get());
    }

    public static void main(String... args) throws SQLException {
        var t = new PQTest();
        t.bench();
    }
}
