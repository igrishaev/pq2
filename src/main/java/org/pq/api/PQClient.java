package org.pq.api;

import org.pq.Native;
import org.pq.Wrapper;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.pq.api.PQError.error;

public record PQClient (
    long ptr,
    String connInfo,
    Arena arena,
    AtomicInteger counter
) implements AutoCloseable {

    public static PQClient of(final String connInfo) {

        final Arena arena = Arena.of(Const.BB_SIZE);

        final long ptr = Native.connect(connInfo);
        if (ptr == arena.NULL()) {
            throw PQError.error("PQ connection returned null");
        }
        final CONNECTION connStatus = Wrapper.connStatus(ptr);

        return switch (connStatus) {
            case OK -> new PQClient(
                    ptr,
                    connInfo,
                    arena,
                    new AtomicInteger()
            );
            case BAD -> {
                final String message = Native.connError(ptr);
                throw PQError.error(message);
            }
            default -> throw PQError.error("wrong connection status: %s", connStatus);
        };
    }

    private String getStmtName() {
        return "s" + counter.incrementAndGet();
    }

    public PQResult query(final String query) {
        final long resPtr = Native.query(this.ptr, query);
        final PGRES pgres = Wrapper.resultStatus(resPtr);
        switch (pgres) {
            case TUPLES_OK -> {}
            default -> throw error("query has failed: code: %s, SQL: %s", pgres, query);
        }
        return new PQResult(this.ptr, resPtr, arena, false);
    }

    public PQResult queryMulti(final String query, int chunkSize) {
        final int status = Native.sendQuery(ptr, query);
        if (status == 0) {
            System.out.println(status);
            final String message = Native.connError(ptr);
            throw error(message);
        }
        Native.setChunkedRowsMode(ptr, chunkSize);
        final long resPtr = Native.getResult(ptr);
        final PGRES pgres = Wrapper.resultStatus(resPtr);
        switch (pgres) {
            case TUPLES_CHUNK -> {}
            default -> {
                Native.closeResult(resPtr);
                throw error("wrong result status: %s", pgres);
            }
        }
        return new PQResult(ptr, resPtr, arena, true);
    }

    public PQStatement prepare(final String query) {
        final String stmtName = getStmtName();
        long resPtr;
        PGRES status;
        String message;

        resPtr = Native.prepare(ptr, stmtName, query);
        status = Wrapper.resultStatus(resPtr);
        Native.closeResult(resPtr);
        switch (status) {
            case COMMAND_OK -> {}
            default -> {
                message = Native.connError(ptr);
                throw error("prepare error: %s, query: %s", message, query);
            }
        }
        resPtr = Native.describe(ptr, stmtName);
        status = Wrapper.resultStatus(resPtr);
        switch (status) {
            case COMMAND_OK -> {
                final int nParams = Native.nParams(resPtr);
                final int[] paramOids = new int[nParams];
                for (int i = 0; i < nParams; i++) {
                    paramOids[i] = Native.paramOid(resPtr, i);
                }
                Native.closeResult(resPtr);
                return new PQStatement(ptr, arena, stmtName, query, nParams, paramOids);
            }
            default -> {
                Native.closeResult(resPtr);
                message = Native.connError(ptr);
                throw error("describe error: %s, statement: %s", message, stmtName);
            }
        }
    }

//    public void reset() {
//        Native.PQreset(ptr);
//    }

    public CONNECTION status() {
        return Wrapper.connStatus(ptr);
    }

    public PQTRANS txStatus() {
        return Wrapper.txStatus(ptr);
    }

    @Override
    public void close() {
        Native.closeConnection(ptr);
    }

    public static void main(String... args) {
        final String connInfo = "host=localhost port=5432 dbname=book user=book password=book";
        try (final PQClient client = PQClient.of(connInfo);
             final PQStatement stmt = client.prepare("select x, x + $1::int4 from generate_series(1, 22) as seq(x)");
             final PQResult res = stmt.executeMulti(List.of(10), 10)) {
            System.out.println(client.status());
            System.out.println(client.txStatus());
            while (res.next()) {
                for (int col: res.iterCols()) {
                    System.out.println(res.getColumn(col));
                }
            }

//            try (final PQResult r = client.query("select x * 1000 from generate_series(1, 3) as seq(x)")) {
//                while (r.next()) {
//                    System.out.println(r.getColumn(0));
//                }
//            }

//            try (final PQResult r = client.queryMulti("select x from generate_series(1, 33) as seq(x)", 10)) {
//                while (r.next()) {
//                    System.out.println(r.getColumn(0));
//                }
//            }

//            PQResult r = client.queryMulti("select x from generate_series(1, 33) as seq(x)", 10);
//            while (r.next()) {
//                System.out.println(r.getColumn(0));
//            }
//            r.close();

//            PQResult r2 = client.queryMulti("select x from generate_series(1, 99) as seq(x)");
//            while (r2.next()) {
//                System.out.println(r2.getColumn(0));
//            }
//            r2.close();
            // System.out.println(r);
        }
    }
}
