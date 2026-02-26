package org.pq.api;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class PQClientTest {

    public static String connInfo = "host=localhost port=5432 dbname=book user=book password=book";

    @Test
    public void simpleTest(){
        try (PQClient client = PQClient.of(connInfo)) {
            assertEquals(CONNECTION.OK, client.status());
            client.begin();
            client.query("create temp table foo (id integer, data text)").close();
            client.query("insert into foo values (1, 'test')").close();
            client.commit();
            client.rollback();
            final PQResult res = client.query("select * from foo");
            res.next();
            assertEquals(1, res.getColumn(0));
            // System.out.println(res.getColumn(0));
            client.commit();
        }
    }

    @Test
    public void closedConnectionTest() {
        var client = PQClient.of(connInfo);
        client.close();
        client.close();

        try {
            client.query("select 1");
            assertEquals(1, 2);
        } catch (PQError e) {
            assertEquals("connection is closed", e.getMessage());
        }
    }

    @Test
    public void resultClosedTest() {
        var client = PQClient.of(connInfo);
        var result = client.query("select x from generate_series(1, 3) as seq(x)");
        client.close();

        result.next();
        assertEquals(1, result.getColumn(0));

        result.close();
        result.reset();
        // result.next();
        // assertEquals(1, result.getColumn(0));


//        try {
//            client.query("select 1");
//            assertEquals(1, 2);
//        } catch (PQError e) {
//            assertEquals("connection is closed", e.getMessage());
//        }
    }

    @Test
    public void testPrepare() {
        try (PQClient client = PQClient.of(connInfo)) {
            var stmt = client.prepare("select $1::int4 as num");
            var res = stmt.execute(List.of(999));
            res.next();
            assertEquals(999, res.getColumn(0));

            stmt.close();
            var res2 = stmt.execute(List.of(999));

        }
    }

}
