package org.pq.api;

import org.pq.Native;
import org.pq.codec.Encoder;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public record PQClient (
    long connPtr,
    String connInfo,
    Arena arena,
    byte[] counter
) implements AutoCloseable {

    public static PQClient of(final String connInfo) {
        final ByteBuffer bb = ByteBuffer.allocateDirect(CONST.BB_SIZE);
        final int initStatus = Native.initBB(bb);

        if (initStatus != 0) {
            throw PQError.error("byte buffer init error, code: %s", initStatus);
        }

        final ByteOrder BO_JVM = ByteOrder.BIG_ENDIAN;

        final byte lead = bb.get(0);
        final ByteOrder BO_CPP = (lead == 1) ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN;
        bb.order(BO_CPP);

        bb.getLong();
        final long bbPtr = bb.getLong();
        final long NULL = bb.getLong();

        final Arena arena = new Arena(bb, bbPtr, BO_JVM, BO_CPP, NULL);

        final long connPtr = Native.PQconnectdb(connInfo);
        if (connPtr == NULL) {
            // TODO: error message
            throw PQError.error("PQ connection returned null");
        }

        final int statusCode = Native.PQstatus(connPtr);
        final CONNECTION status = CONNECTION.of(statusCode);

        return switch (status) {
            case OK -> new PQClient(
                    connPtr,
                    connInfo,
                    arena,
                    new byte[] {0}
            );
            case BAD -> {
                final String message = Native.PQerrorMessage(connPtr);
                throw PQError.error(message);
            }
            default -> throw PQError.error("wrong status: %s, code: %s", status, statusCode);
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
        opStatus = Native.PGresultInfo(resPtr, arena.bbPtr());
        if (opStatus != 0) {
            Native.PQclear(resPtr);
            throw PQError.error("PGresultInfo returned non-zero status: %s", opStatus);
        }
        return PGResult.of(arena);
    }

    private String getStmtName() {
        return "s" + ++counter[0];
    }

    public Stmt prepare(final String query) {
        final String stmtName = getStmtName();
        Native._PQprepare(connPtr, stmtName, query, arena.bbPtr());
        final PGResult pgResult = PGResult.of(arena);
        return new Stmt(connPtr, arena, stmtName, pgResult);
    }

    public PGResult prepare2(final String sql) {
        //Encoder.encodeExecParams(arena, 3, List.of(555, "hello", UUID.randomUUID()), new int[] {OID.INT4, OID.TEXT, OID.UUID});
        Encoder.encodeExecParams(arena, 0, List.of(), new int[] {});
        Native.Abc(connPtr, sql, arena.bbPtr());
        return PGResult.of(arena);
    }

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
        Native.PQfinish(connPtr);
    }

    public static void main(String... args) {
        final String connInfo = "host=localhost port=15432 dbname=test user=test password=test";
//        final String query = "select x from generate_series(1, 199) as seq(x)";
//        try (final PQClient client = PQClient.of(connInfo);
//             final Stmt stmt = client.prepare(query);
//             final PGResult  res = stmt.execute(List.of(555, "hello", UUID.randomUUID()))) {
//            for (Object[] row: res.iterTuples()) {
//                System.out.println(Arrays.toString(row));
//            }
//        }
        try (final PQClient client = PQClient.of(connInfo)) {
            PGResult result;
            long ptr = 42;

            result = client.prepare2("select x from generate_series(1, 30) as seq(x)");
            for (Object[] row: result.iterTuples()) {
                System.out.println(Arrays.toString(row));
            }
            System.out.println("-----------------");

            while (ptr != client.arena.NULL()) {
                ptr = Native.nextResult(client.connPtr, client.arena.bbPtr());
                System.out.println(ptr);
                result = PGResult.of(client.arena);
                for (Object[] row: result.iterTuples()) {
                    System.out.println(Arrays.toString(row));
                }
                System.out.println(Native.PQresultStatus(ptr));
                System.out.println("-----------------");
            }




        }
    }
}
