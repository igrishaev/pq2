package org.pq.api;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class PQClientTest {

    public static String connInfo = "host=localhost port=15432 dbname=test user=test password=test";

    @Test
    public void simpleTest(){
        try (PQClient client = PQClient.of(connInfo)) {
            assertEquals(CONNECTION.OK, client.status());
            client.begin();
            client.query("create temp table foo (id integer, data text)").close();
            client.query("insert into foo values (1, 'test')").close();
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

        try {
            client.query("select 1");
            assertEquals(1, 2);
        } catch (PQError e) {
            assertEquals("connection is closed", e.getMessage());
        }
//        var result =
//        assertEquals(result, 1);
//        try (PQClient client = PQClient.of("foobar=535")) {
//            assertEquals(client.status(), CONNECTION.OK);
//        }
    }
}
