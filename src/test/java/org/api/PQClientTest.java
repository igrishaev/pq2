package org.api;

import org.junit.Test;
import org.pq.api.*;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class PQClientTest {

    public static String connInfo = "host=localhost port=15432 dbname=test user=test password=test";

    @Test
    public void testItWorks(){
        try (PQClient client = PQClient.of(connInfo)) {
            assertEquals(CONNECTION.OK, client.status());
            var res = client.query("select 1 as col");
            while (res.next()) {
                assertEquals(1, res.getColumn(0));
            }
            res.close();
        }
    }

    @Test
    public void testSimpleTransaction(){
        try (PQClient client = PQClient.of(connInfo)) {
            client.query("create temp table foo (id integer, data text)").close();
            client.begin();
            client.query("insert into foo values (1, 'test')").close();
            client.rollback();
            var res = client.query("select * from foo");
            assertTrue(res.isEmpty());
            assertFalse(res.next());
            assertThrows(PQError.class, () -> res.getColumn(0));
        }
    }

    @Test
    public void testCloseClient() {
        var client = PQClient.of(connInfo);
        client.close();
        client.close();
        assertThrows(PQError.class, () -> client.query("select 1"));
        assertThrows(PQError.class, client::commit);
        assertThrows(PQError.class, client::rollback);
        assertThrows(PQError.class, client::begin);
        assertEquals(connInfo, client.connInfo());
        assertThrows(PQError.class, () -> client.queryChunked("select 1", 10));
        assertThrows(PQError.class, client::status);
        assertThrows(PQError.class, client::txStatus);

    }

    @Test
    public void testQuerySimple() {
        try (var client = PQClient.of(connInfo);
             var res = client.query("select x from generate_series(1, 5) as seq(x)")) {
            assertFalse(res.isEmpty());
            assertEquals(5, res.affectedRows());
            assertEquals("SELECT 5", res.commandName());
            assertEquals(5, res.rowNumber());
        }
    }

    @Test
    public void testQueryChunked() {
        try (var client = PQClient.of(connInfo);
             var res = client.queryChunked("select x from generate_series(1, 5) as seq(x)", 3)) {
            // these are unknown yet
            assertFalse(res.isEmpty());
            assertEquals(-1, res.affectedRows());
            assertEquals("", res.commandName());
            // consume and get
            assertEquals(3, res.rowNumber());
            while (res.next()) {
                res.getColumn(0);
            }
            // TODO
            assertTrue(res.isEmpty());
            assertEquals(-1, res.affectedRows());
            assertEquals("", res.commandName());
            assertEquals(0, res.rowNumber());



        }
    }

    @Test
    public void testQueryChunkedResult() {
        try (var client = PQClient.of(connInfo);
             var res = client.queryChunked("select x from generate_series(1, 5) as seq(x)", 3)) {
            final List<Object> result = new ArrayList<>();
            while (res.next()) {
                result.add(res.getColumn(0));
            }
            assertEquals(result, List.of(1, 2, 3, 4, 5));
        }
    }

    @Test
    public void testQueryChunkedColumns() {
        try (var client = PQClient.of(connInfo);
             var res = client.queryChunked("select x, x from generate_series(1, 99) as seq(x)", 10)) {
            assertEquals(2, res.colNumber());
            assertEquals(10, res.rowNumber());
            assertEquals(10, res.rowNumberTotal());
            while (res.next()) {
                assertEquals(2, res.colNumber());
                for (int col: res.iterCols()) {
                    res.getColumn(col);
                }
            }
            assertEquals(99, res.rowNumberTotal());
        }
    }

    @Test
    public void testQueryChunkedAsList() {
        try (var client = PQClient.of(connInfo);
             var res = client.queryChunked("select x, x from generate_series(1, 99) as seq(x)", 10)) {
//            res.next();
//            assertEquals(List.of(1, 1), res.rowAsList());
            while (res.next()) {
                assertEquals(List.of(1, 1), res.rowAsList());
            }
        }
    }

    @Test
    public void resultClosedTest() {
        var client = PQClient.of(connInfo);
        var result = client.query("select x from generate_series(1, 3) as seq(x)");
        client.close();

        result.next();
        assertEquals(1, result.getColumn(0));

        assertEquals(3, result.affectedRows());
        assertEquals("SELECT 3", result.commandName());

        result.close();
        assertThrows(PQError.class, result::reset);
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
