package org.pq.api;

import org.pq.Native;

import java.util.List;

import static org.pq.api.PQError.error;

public record PQClient (
    long ptr,
    String connInfo,
    Arena arena,
    byte[] counter
) implements AutoCloseable {

    public static CONNECTION connStatus(final long connPtr) {
        return CONNECTION.of(Native.connStatus(connPtr));
    }

    public static PGRES resStatus(final long resPtr) {
        return PGRES.of(Native.resStatus(resPtr));
    }

    public static PQClient of(final String connInfo) {

        final Arena arena = Arena.of(Const.BB_SIZE);

        final long ptr = Native.connect(connInfo);
        if (ptr == arena.NULL()) {
            throw PQError.error("PQ connection returned null");
        }
        final CONNECTION connStatus = connStatus(ptr);

        return switch (connStatus) {
            case OK -> new PQClient(
                    ptr,
                    connInfo,
                    arena,
                    new byte[] {0}
            );
            case BAD -> {
                final String message = Native.connError(ptr);
                throw PQError.error(message);
            }
            default -> throw PQError.error("wrong connection status: %s", connStatus);
        };
    }

    private String getStmtName() {
        return "s" + ++counter[0];
    }

    public PQResult query(final String query) {
        final long resPtr = Native.query(this.ptr, query);
        final PGRES status = resStatus(resPtr);
        if (status != PGRES.TUPLES_OK) {
            throw error("query has failed: code: %s, SQL: %s", status, query);
        }
        return new PQResult(this.ptr, resPtr, arena, false);
    }

    public PQStatement prepare(final String query) {
        final String stmtName = getStmtName();
        long resPtr;
        PGRES status;
        String message;

        resPtr = Native.prepare(ptr, stmtName, query);
        status = resStatus(resPtr);
        Native.closeResult(resPtr);
        if (status != PGRES.COMMAND_OK) {
            message = Native.connError(ptr);
            throw error("prepare error: %s, query: %s", message, query);
        }

        resPtr = Native.describe(ptr, stmtName);
        status = resStatus(resPtr);
        if (status == PGRES.COMMAND_OK) {
            final int nParams = Native.nParams(resPtr);
            final int[] paramOids = new int[nParams];
            for (int i = 0; i < nParams; i++) {
                paramOids[i] = Native.paramOid(resPtr, i);
            }
            Native.closeResult(resPtr);
            return new PQStatement(ptr, arena, stmtName, query, nParams, paramOids);
        } else {
            Native.closeResult(resPtr);
            message = Native.connError(ptr);
            throw error("describe error: %s, statement: %s", message, stmtName);
        }
    }

//    public void reset() {
//        Native.PQreset(ptr);
//    }

//    public CONNECTION status() {
//        final int result = Native.PQstatus(ptr);
//        return CONNECTION.of(result);
//    }

//    public PQTRANS transactionStatus() {
//        final int result = Native.PQtransactionStatus(ptr);
//        return PQTRANS.of(result);
//    }

    @Override
    public void close() {
        Native.closeConnection(ptr);
    }

    public static void main(String... args) {
        final String connInfo = "host=localhost port=15432 dbname=test user=test password=test";
        try (final PQClient client = PQClient.of(connInfo);
             final PQStatement stmt = client.prepare("select x, x + $1::int4 from generate_series(1, 3) as seq(x)");
             final PQResult res = stmt.execute(List.of(55))) {
            while (res.next()) {
                for (int col: res.iterCols()) {
                    System.out.println(res.getColumn(col));
                }
            }

            try (final PQResult r = client.query("select x * 1000 from generate_series(1, 3) as seq(x)")) {
                while (r.next()) {
                    System.out.println(r.getColumn(0));
                }
            }
        }
    }
}
