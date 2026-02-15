package org.pq.api;

import org.pq.Native;
import org.pq.Native2;
import org.pq.codec.Encoder;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.pq.api.PQError.*;

public record PQClient (
    long connPtr,
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

        final long connPtr = Native2.connect(connInfo);
        if (connPtr == arena.NULL()) {
            throw PQError.error("PQ connection returned null");
        }
        final CONNECTION connStatus = connStatus(connPtr);

        return switch (connStatus) {
            case OK -> new PQClient(
                    connPtr,
                    connInfo,
                    arena,
                    new byte[] {0}
            );
            case BAD -> {
                final String message = Native2.connError(connPtr);
                throw PQError.error(message);
            }
            default -> throw PQError.error("wrong connection status: %s", connStatus);
        };
    }

    public PGResult exec(final String sql) {
        final long resPtr = Native.PQexec(connPtr, sql);
        if (resPtr == arena.NULL()) {
            throw PQError.error("PQExec returned null (most likely no enough memory)");
        }
        int opStatus = Native.PQresultStatus(resPtr);
        PGRES pgres = PGRES.of(opStatus);
        switch (pgres) {
            case FATAL_ERROR, NONFATAL_ERROR, BAD_RESPONSE -> {
                Native.PQclear(resPtr);
                String message = Native.PQerrorMessage(connPtr);
                throw PQError.error(message);
            }
        }
        opStatus = Native.PGresultInfo(resPtr, arena.ptr());
        if (opStatus != 0) {
            Native.PQclear(resPtr);
            throw PQError.error("PGresultInfo returned non-zero status: %s", opStatus);
        }
        return PGResult.of(arena);
    }

    private String getStmtName() {
        return "s" + ++counter[0];
    }

    public void closeStatement(final String stmtName) {
        final long result = Native2.closeStatement(connPtr, stmtName);
        final PGRES status = resStatus(result);
        // TODO: check
        Native2.closeResult(result);
        System.out.println(result);
        System.out.println(status);
    }

    public String prepare(final String query) {
        final String stmtName = getStmtName();
        long result;
        PGRES status;
        String message;

        result = Native2.prepare(connPtr, stmtName, query, arena.ptr());
        status = resStatus(result);
        Native2.closeResult(result);
        if (status != PGRES.COMMAND_OK) {
            message = Native2.connError(connPtr);
            throw error("prepare error: %s, query: %s", message, query);
        }

        result = Native2.describe(connPtr, stmtName);
        status = resStatus(result);
        Native2.closeResult(result);
        if (status != PGRES.COMMAND_OK) {
            message = Native2.connError(connPtr);
            throw error("describe error: %s, statement: %s", message, stmtName);
        }

        return stmtName;




    }

//    public Stmt prepare(final String query) {
//        final String stmtName = getStmtName();
//        Native._PQprepare(connPtr, stmtName, query, arena.ptr());
//        final PGResult pgResult = PGResult.of(arena);
//        return new Stmt(connPtr, arena, stmtName, pgResult);
//    }
//
//    public PGResult prepare2(final String sql) {
//        //Encoder.encodeExecParams(arena, 3, List.of(555, "hello", UUID.randomUUID()), new int[] {OID.INT4, OID.TEXT, OID.UUID});
//        Encoder.encodeExecParams(arena, 0, List.of(), new int[] {});
//        Native.Abc(connPtr, sql, arena.ptr());
//        return PGResult.of(arena);
//    }

    public void reset() {
        Native.PQreset(connPtr);
    }

    public CONNECTION status() {
        final int result = Native.PQstatus(connPtr);
        return CONNECTION.of(result);
    }

    public PQTRANS transactionStatus() {
        final int result = Native.PQtransactionStatus(connPtr);
        return PQTRANS.of(result);
    }

    @Override
    public void close() {
        Native2.closeConnection(connPtr);
    }

    public static void main(String... args) {
        final String connInfo = "host=localhost port=5432 dbname=book user=book password=book";
//        final String query = "select x from generate_series(1, 199) as seq(x)";
//        try (final PQClient client = PQClient.of(connInfo);
//             final Stmt stmt = client.prepare(query);
//             final PGResult  res = stmt.execute(List.of(555, "hello", UUID.randomUUID()))) {
//            for (Object[] row: res.iterTuples()) {
//                System.out.println(Arrays.toString(row));
//            }
//        }
        try (final PQClient client = PQClient.of(connInfo)) {
            var s = client.prepare("select 1 as foo");
            System.out.println(s);
            client.closeStatement(s);
//            PGResult result;
//            long ptr = 42;
//
//            result = client.prepare2("select x from generate_series(1, 30) as seq(x)");
//            for (Object[] row: result.iterTuples()) {
//                System.out.println(Arrays.toString(row));
//            }
//            System.out.println("-----------------");
//
//            while (ptr != client.arena.NULL()) {
//                ptr = Native.nextResult(client.connPtr, client.arena.ptr());
//                System.out.println(ptr);
//                result = PGResult.of(client.arena);
//                for (Object[] row: result.iterTuples()) {
//                    System.out.println(Arrays.toString(row));
//                }
//                System.out.println(Native.PQresultStatus(ptr));
//                System.out.println("-----------------");
//            }
        }
    }
}
