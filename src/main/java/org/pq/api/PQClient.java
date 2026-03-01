package org.pq.api;

import org.pq.Native;
import org.pq.Wrapper;

import java.util.List;

import static org.pq.api.PQError.error;

public class PQClient implements AutoCloseable {

    private final long ptr;
    private final String connInfo;
    private final Arena arena;
    private int counter;
    private boolean isClosed;
    private final TryLock lock;

    private PQClient(final long ptr, final String connInfo, final Arena arena) {
        this.ptr = ptr;
        this.connInfo = connInfo;
        this.arena = arena;
        this.counter = 0;
        this.isClosed = false;
        this.lock = new TryLock();
    }

    public static PQClient of(final String connInfo) {

        final Arena arena = Arena.of(Const.BB_SIZE);

        final long ptr = Native.connect(connInfo);
        if (ptr == arena.NULL()) {
            throw PQError.error("PQ connection returned null (most likely it's lack of memory)");
        }
        final CONNECTION connStatus = Wrapper.connStatus(ptr);

        return switch (connStatus) {
            case OK -> new PQClient(ptr, connInfo, arena);
            case BAD -> {
                final String message = Native.connError(ptr);
                throw PQError.error(message);
            }
            default -> throw PQError.error("wrong connection status: %s", connStatus);
        };
    }

    public String connInfo() {
        return connInfo;
    }

    public TryLock lock() {
        return lock.lock();
    }

    private String getStmtName() {
        try (var ignored = lock()) {
            ensureOpen();
            return "s" + ++counter;
        }
    }

    public PQResult query(final String query) {
        try (var ignored = lock()) {
            ensureOpen();
            final long resPtr = Native.query(this.ptr, query);
            final PGRES pgres = Wrapper.resultStatus(resPtr);
            final int nColumns = Native.nColumns(resPtr);
            switch (pgres) {
                case TUPLES_OK, COMMAND_OK -> {}
                default -> {
                    final String message = Native.connError(ptr);
                    throw error("query has failed: code: %s, error: %s, SQL: %s",
                            pgres, message, query
                    );
                }
            }
            return new PQResult(this.ptr, resPtr, nColumns, arena, lock, false);
        }
    }

    public PQResult queryChunked(final String query, int chunkSize) {
        try (var ignored = lock()) {
            final int status = Native.sendQuery(ptr, query);
            if (status == 0) {
                final String message = Native.connError(ptr);
                throw error("failed to send query, error: %s, chunk size: %s, query: %s",
                        message, chunkSize, query
                );
            }
            Native.setChunkedRowsMode(ptr, chunkSize);
            final long resPtr = Native.getResult(ptr);
            final PGRES result = Wrapper.resultStatus(resPtr);
            final int nColumns = Native.nColumns(resPtr);
            switch (result) {
                case TUPLES_CHUNK -> {}
                default -> {
                    Native.closeResult(resPtr);
                    throw error("wrong result status: %s", result);
                }
            }
            return new PQResult(ptr, resPtr, nColumns, arena, lock, true);
        }
    }

    public void begin() {
        try (var ignored = lock()) {
            final PQTRANS txStatus = txStatus();
            switch (txStatus) {
                case IDLE -> query("begin").close();
                case INERROR -> throw error("Cannot 'begin' as the connection is an error state. Please rollback first.");
                case INTRANS -> {} // do nothing
                case ACTIVE -> throw error("Cannot 'begin' as the connection is processing a command. Perhaps you should wait.");
                case UNKNOWN -> throw error("Cannot 'begin' as the connection is an unknown mode. Perhaps it was closed.");
            }
        }
    }

    public void commit() {
        try (var ignored = lock()) {
            final PQTRANS txStatus = txStatus();
            switch (txStatus) {
                case IDLE -> {}
                case INERROR -> throw error("Cannot 'commit' as the connection is an error state. Please rollback first.");
                case INTRANS -> query("commit").close();
                case ACTIVE -> throw error("Cannot 'commit' as the connection is processing a command. Perhaps you should wait.");
                case UNKNOWN -> throw error("Cannot 'commit' as the connection is an unknown mode. Perhaps it was closed.");
            }
        }
    }

    public void rollback() {
        try (var ignored = lock()) {
            final PQTRANS txStatus = txStatus();
            switch (txStatus) {
                case IDLE -> {}
                case INERROR, INTRANS -> query("rollback").close();
                case ACTIVE -> throw error("Cannot 'rollback' as the connection is processing a command. Perhaps you should wait.");
                case UNKNOWN -> throw error("Cannot 'rollback' as the connection is an unknown mode. Perhaps it was closed.");
            }
        }
    }

    public PQStatement prepare(final String query) {
        try (var ignored = lock()) {
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
                    throw error("failed to prepare, status: %s, error: %s, query: %s",
                            status, message, query
                    );
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
                    return new PQStatement(ptr, arena, stmtName, query, nParams, paramOids, lock);
                }
                default -> {
                    Native.closeResult(resPtr);
                    message = Native.connError(ptr);
                    throw error("describe error: %s, statement: %s", message, stmtName);
                }
            }
        }
    }

//    public void reset() {
//        Native.PQreset(ptr);
//    }


    public CONNECTION status() {
        try (var ignored = lock()) {
            ensureOpen();
            return Wrapper.connStatus(ptr);
        }
    }

    public PQTRANS txStatus() {
        try (var ignored = lock()) {
            ensureOpen();
            return Wrapper.txStatus(ptr);
        }
    }

    private void ensureOpen() {
        if (isClosed) {
            throw error("connection is closed");
        }
    }

    @Override
    public void close() {
        if (isClosed) {
            return;
        }
        try (var ignored = lock()) {
            isClosed = true;
            Native.closeConnection(ptr);
        }
    }

    public static void main(String... args) {
        final String connInfo = "host=localhost port=15432 dbname=test user=test password=test";
        try (final PQClient client = PQClient.of(connInfo);
             final PQStatement stmt = client.prepare("select x, x + $1::int4 from generate_series(1, 22) as seq(x)");
             final PQResult res = stmt.executeMulti(List.of(10), 10)) {
            System.out.println(client.status());
            System.out.println(client.txStatus());
            while (res.next()) {
                System.out.println(res.rowAsList());
//                for (int col: res.iterCols()) {
//                    System.out.println(res.getColumn(col));
//                }
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
