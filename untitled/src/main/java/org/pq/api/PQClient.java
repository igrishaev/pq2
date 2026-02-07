package org.pq.api;

import org.pq.Native;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

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
        final long ptr = Native.PQconnectdb(conninfo);
        // TODO check if null
        final int statusCode = Native.PQstatus(ptr);
        final CONNECTION status = CONNECTION.of(statusCode);
        return switch (status) {
            case OK -> {
                ByteOrder BO_CPP;
                final ByteOrder BO_JVM = ByteOrder.BIG_ENDIAN;
                final ByteBuffer bb = ByteBuffer.allocateDirect(CONST.BB_SIZE);
                Native.initBB(bb);
                final byte lead = bb.get(0);
                if (lead == 1) {
                    BO_CPP = ByteOrder.LITTLE_ENDIAN;
                } else {
                    BO_CPP = ByteOrder.BIG_ENDIAN;
                }
                bb.order(BO_CPP);
                bb.getLong();
                final long bbPtr = bb.getLong();
                final long NULL = bb.getLong();
                yield new PQClient(
                        ptr,
                        conninfo,
                        bb,
                        bbPtr,
                        BO_JVM,
                        BO_CPP,
                        NULL
                );
            }
            case BAD -> {
                final String message = Native.PQerrorMessage(ptr);
                throw PQError.of(message);
            }
            default -> throw PQError.of("wrong status: %s, code: %s", status, statusCode);
        };
    }

    public PGResult exec(final String sql) {
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
        System.out.println(client);
        System.out.println(client.status());
        System.out.println(client.transactionStatus());
        client.close();
    }
}
