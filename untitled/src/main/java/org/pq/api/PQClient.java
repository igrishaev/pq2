package org.pq.api;

import org.pq.Native;
import org.pq.codec.Encoder;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.List;

public class PQClient implements AutoCloseable {
    protected final long connPtr;
    private final String conninfo;
    protected final ByteBuffer bb;
    protected final long bbPtr;
    private final ByteOrder BO_JVM;
    private final ByteOrder BO_CPP;
    private final long NULL;
    private int counter = 0;

    private PQClient(final long ptr,
                     final String conninfo,
                     final ByteBuffer bb,
                     final long bbPtr,
                     final ByteOrder BO_JVM,
                     final ByteOrder BO_CPP,
                     final long NULL
    ) {
        this.connPtr = ptr;
        this.conninfo = conninfo;
        this.bb = bb;
        this.bbPtr = bbPtr;
        this.BO_JVM = BO_JVM;
        this.BO_CPP = BO_CPP;
        this.NULL = NULL;
    }

    public static PQClient of(final String conninfo) {
        final ByteBuffer bb = ByteBuffer.allocateDirect(CONST.BB_SIZE);
        final int initStatus = Native.initBB(bb);

        if (initStatus != 0) {
            throw PQError.of("byte buffer init error, code: %s", initStatus);
        }

        final ByteOrder BO_JVM = ByteOrder.BIG_ENDIAN;

        final byte lead = bb.get(0);
        final ByteOrder BO_CPP = (lead == 1) ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN;
        bb.order(BO_CPP);

        bb.getLong();
        final long bbPtr = bb.getLong();
        final long NULL = bb.getLong();

        final long connPtr = Native.PQconnectdb(conninfo);
        if (connPtr == NULL) {
            throw PQError.of("PQ connection returned null");
        }

        final int statusCode = Native.PQstatus(connPtr);
        final CONNECTION status = CONNECTION.of(statusCode);

        return switch (status) {
            case OK -> new PQClient(
                    connPtr,
                    conninfo,
                    bb,
                    bbPtr,
                    BO_JVM,
                    BO_CPP,
                    NULL
            );
            case BAD -> {
                final String message = Native.PQerrorMessage(connPtr);
                throw PQError.of(message);
            }
            default -> throw PQError.of("wrong status: %s, code: %s", status, statusCode);
        };
    }

    protected void rewind() {
        bb.rewind();
    }

    protected void bbJVM() {
        bb.order(BO_JVM);
    }

    protected void bbCPP() {
        bb.order(BO_CPP);
    }

    protected void bbDebug(final int len) {
        final byte[] ba = new byte[len];
        bb.get(0, ba);
        System.out.println(Arrays.toString(ba));
    }

    public PGResult execWithParams(final String sql, final List<Object> params) {
        final int len = params.size();
        final Object[] prms = params.toArray(new Object[0]);
        final int[] oids = new int[] {23};
        final int[] formats = new int[] {1};
        Encoder.encodeBB(bb, bbPtr, prms, oids, formats, 1);

        final long resPtr = Native.execWithParams(connPtr, sql, bbPtr);
        if (resPtr == NULL) {
            throw PQError.of("PQExec returned null (most likely no enough memory)");
        }
        int opStatus = Native.PQresultStatus(resPtr);
        PGRES pgres = PGRES.of(opStatus);
        switch (pgres) {
            case FATAL_ERROR, NONFATAL_ERROR, BAD_RESPONSE -> {
                Native.PQclear(resPtr);
                String message = Native.PQerrorMessage(connPtr);
                throw PQError.of(message);
            }
        }
        opStatus = Native.PGresultInfo(resPtr, bbPtr);
        if (opStatus != 0) {
            Native.PQclear(resPtr);
            throw PQError.of("PGresultInfo returned non-zero status: %s", opStatus);
        }
        // bbDebug(64);
        return PGResult.of(this, bb);

    }

    public PGResult exec(final String sql) {
        final long resPtr = Native.PQexec(connPtr, sql);
        if (resPtr == NULL) {
            throw PQError.of("PQExec returned null (most likely no enough memory)");
        }
        int opStatus = Native.PQresultStatus(resPtr);
        PGRES pgres = PGRES.of(opStatus);
        switch (pgres) {
            case FATAL_ERROR, NONFATAL_ERROR, BAD_RESPONSE -> {
                Native.PQclear(resPtr);
                String message = Native.PQerrorMessage(connPtr);
                throw PQError.of(message);
            }
        }
        opStatus = Native.PGresultInfo(resPtr, bbPtr);
        if (opStatus != 0) {
            Native.PQclear(resPtr);
            throw PQError.of("PGresultInfo returned non-zero status: %s", opStatus);
        }
        return PGResult.of(this, bb);
    }

    private String getStmtName() {
        return "s" + ++counter;
    }

    public Stmt prepare(final String query) {
        final String stmtName = getStmtName();
        Native._PQprepare(connPtr, stmtName, query, bbPtr);
        final PGResult pgResult = PGResult.of(this, bb);
        return new Stmt(this, stmtName, pgResult);
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
        PQClient client = PQClient.of("host=localhost port=15432 dbname=test user=test password=test");

        Stmt s = client.prepare("select $1::int4 as foo");
        PGResult res = s.execute(List.of(555));
        for (int row: res) {
            System.out.println(res.getObject(row, 0));
        }
//        try (PGResult res = client.execWithParams("select $1::int4 as x", List.of(1))) {
//            for (int row: res) {
//                System.out.println(res.getObject(row, 0));
//            }
//        }
//        client.close();
    }
}
