package org.pq.api;

import org.pq.Native;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class PQClient implements AutoCloseable {
    private final long connPtr;
    private final String conninfo;
    private final ByteBuffer bb;
    private final long bbPtr;
    private final ByteOrder BO_JVM;
    private final ByteOrder BO_CPP;
    private final long NULL;

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

        final ByteOrder BO_JVM = ByteOrder.nativeOrder();

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

    private void bbJVM() {
        bb.order(BO_JVM);
    }

    private void bbCPP() {
        bb.order(BO_CPP);
    }

    private void bbDebug(final int len) {
        final byte[] ba = new byte[len];
        bb.get(0, ba);
        System.out.println(Arrays.toString(ba));
    }

    public PGResult exec(final String sql) {
        final long resPtr = Native.PQexec(connPtr, sql);
        if (resPtr == NULL) {
            throw PQError.of("PQExec returned null");
        }

        // TODO check result state

        final int opStatus = Native.PGresultInfo(resPtr, bbPtr);
        if (opStatus != 0) {
            // TODO: close PGResult;
            throw PQError.of("PGresultInfo returned non-zero status: %s", opStatus);
        }

        bbDebug(64);

        bb.rewind();
        bbCPP();
//        final int nTuples = bb.getInt();
//        final int nColumns = bb.getInt();
////        final int len = bb.getInt();
//        // System.out.println("len");
//        System.out.println(nTuples);
//        System.out.println(nColumns);
//        byte[] ba = new byte[4];
//        bb.get(ba);
//        String s = new String(ba, StandardCharsets.UTF_8);
//        System.out.println(nTuples);
//        System.out.println(nColumns);
//        System.out.println(s);
        return null;
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
        PQClient client = PQClient.of("host=localhost port=5432 dbname=book user=book password=book");
//        System.out.println(client);
//        System.out.println(client.status());
//        System.out.println(client.transactionStatus());
        client.exec("select x as foobar from generate_series(1, 3) as seq(x)");

        client.close();
    }
}
