package org.pq.api;

import org.pq.Native2;

import java.util.List;

import static org.pq.api.PQError.*;

public record PQClient (
    long ptr,
    String connInfo,
    Arena arena,
    byte[] counter
) implements AutoCloseable {

    public static CONNECTION connStatus(final long connPtr) {
        return CONNECTION.of(Native2.connStatus(connPtr));
    }

    public static PGRES resStatus(final long resPtr) {
        return PGRES.of(Native2.resStatus(resPtr));
    }

    public static PQClient of(final String connInfo) {

        final Arena arena = Arena.of(CONST.BB_SIZE);

        final long ptr = Native2.connect(connInfo);
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
                final String message = Native2.connError(ptr);
                throw PQError.error(message);
            }
            default -> throw PQError.error("wrong connection status: %s", connStatus);
        };
    }

    private String getStmtName() {
        return "s" + ++counter[0];
    }

    public Result query(final String query) {
        final long resPtr = Native2.query(this.ptr, query);
        final PGRES status = resStatus(resPtr);
        if (status != PGRES.TUPLES_OK) {
            throw error("query has failed: code: %s, SQL: %s", status, query);
        }
        return Result.of(resPtr, arena);
    }

    public Stmt2 prepare(final String query) {
        final String stmtName = getStmtName();
        long resPtr;
        PGRES status;
        String message;

        resPtr = Native2.prepare(ptr, stmtName, query);
        status = resStatus(resPtr);
        Native2.closeResult(resPtr);
        if (status != PGRES.COMMAND_OK) {
            message = Native2.connError(ptr);
            throw error("prepare error: %s, query: %s", message, query);
        }

        resPtr = Native2.describe(ptr, stmtName);
        status = resStatus(resPtr);
        if (status == PGRES.COMMAND_OK) {
            Native2.serializePrepared(resPtr, arena.ptr());
            Native2.closeResult(resPtr);
            return Stmt2.of(this, stmtName, query, arena);
        } else {
            Native2.closeResult(resPtr);
            message = Native2.connError(ptr);
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
        Native2.closeConnection(ptr);
    }

    public static void main(String... args) {
        final String connInfo = "host=localhost port=5432 dbname=book user=book password=book";
        try (final PQClient client = PQClient.of(connInfo);
             final Stmt2 stmt = client.prepare("select x, x + $1::int4 from generate_series(1, 3) as seq(x)");
             final Result res = stmt.execute(List.of(55))) {
            while (res.next()) {
                for (int col: res.iterCols()) {
                    System.out.println(res.getColumn(col));
                }
            }

            try (final Result r = client.query("select x * 1000 from generate_series(1, 3) as seq(x)")) {
                while (r.next()) {
                    System.out.println(r.getColumn(0));
                }
            }
        }
    }
}
