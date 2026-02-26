package org.pq;


import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.*;
import java.sql.SQLException;

public class JDBCTest {

    private Connection conn;

    public JDBCTest() throws SQLException {
        conn = DriverManager.getConnection("jdbc:postgresql://localhost:15432/test?user=test&password=test");
    }

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
              generate_series(1,99999) as s(x)
            
            """;

    public void test() throws SQLException {
        Object obj;
        PreparedStatement stmt = conn.prepareStatement(sql);
        ResultSet rs = stmt.executeQuery();
        while (rs.next()) {
            for (int col = 1; col < 9; col++) {
                obj = rs.getObject(col);
            }
        }
    }

    public void bench() throws SQLException {
        long start = System.currentTimeMillis();
        Runtime runtime = Runtime.getRuntime();
        long usedMemoryBefore = runtime.totalMemory() - runtime.freeMemory();
        System.out.println("Used Memory before: " + usedMemoryBefore / 1000000);
        // working code here
        for (int i = 0; i < 100; i++) {
            test();
        }
        long usedMemoryAfter = runtime.totalMemory() - runtime.freeMemory();
        System.out.println("Memory increased: " + (usedMemoryAfter-usedMemoryBefore) / 1000000);
        long end = System.currentTimeMillis();
        System.out.println(end - start);
    }

    public static void main(String... args) throws SQLException {
        var t = new JDBCTest();
        t.bench();
    }


}
