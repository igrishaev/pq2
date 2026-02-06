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

    public void test() throws SQLException {
        int num;
        Object obj;
        String s;
        byte[] bs;
        PreparedStatement stmt = conn.prepareStatement("select now()::date as a from generate_series(1, 9999) as seq(x)");
        ResultSet rs = stmt.executeQuery();
        while (rs.next()) {
            // obj = rs.getObject(1);
            // obj = rs.getObject(2);
            //obj = rs.getObject(3);
            // num = rs.getInt(1);
//            s = rs.getString(1);
//            s = rs.getString(1);
//            s = rs.getString(1);
//            s = rs.getString(1);
//            s = rs.getString(1);
//            s = rs.getString(1);
//            s = rs.getString(1);
//            s = rs.getString(1);
//            s = rs.getString(1);
//            s = rs.getString(1);
            // // s = rs.getString(1);
            // s = rs.getString(1);
            for (int j = 0; j < 10; j++) {
                obj = rs.getObject(1);
            }
//            obj = rs.getObject(1);
//            obj = rs.getObject(1);
//            obj = rs.getObject(1);
//            obj = rs.getObject(1);
//            obj = rs.getObject(1);
//            obj = rs.getObject(1);
//            obj = rs.getObject(1);
//            obj = rs.getObject(1);
//            obj = rs.getObject(1);
//            obj = rs.getObject(1);
//             num = rs.getInt(1);
//            num = rs.getInt(1);
//            num = rs.getInt(1);
//            num = rs.getInt(1);
//            num = rs.getInt(1);
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
        var t = new JDBCTest();
        t.bench();
    }


}
