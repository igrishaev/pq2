package org.pq.api;

import org.pq.Native;
import org.pq.Wrapper;
import org.pq.codec.Encoder;

import static org.pq.api.PQError.error;

import java.util.List;

public class PQStatement implements AutoCloseable {

    // TODO toString

    private final long connPtr;
    private final Arena arena;
    private final String stmtName;
    private final String query;
    private final int nParams;
    private final int[] paramOids;
    private final TryLock lock;
    private boolean isClosed;

    protected PQStatement(final long connPtr,
                          final Arena arena,
                          final String stmtName,
                          final String query,
                          final int nParams,
                          final int[] paramOids,
                          final TryLock lock) {
        this.connPtr = connPtr;
        this.arena = arena;
        this.stmtName = stmtName;
        this.query = query;
        this.nParams = nParams;
        this.paramOids = paramOids;
        this.lock = lock;
        this.isClosed = false;
    }

    private TryLock lock() {
        return lock.lock();
    }

    private void ensureOpen() {
        if (isClosed) {
            throw error("prepared statement is closed");
        }
    }

    public String getName() {
        return stmtName;
    }

    @SuppressWarnings("unused")
    public String getQuery() {
        return query;
    }

    @SuppressWarnings("unused")
    public PQResult execute() {
        return execute(List.of());
    }

    public PQResult execute(final List<Object> params) {
        try (var ignored = lock()) {
            ensureOpen();
            final int size = params.size();
            if (size != this.nParams) {
                throw PQError.error("parameters mismatch: %s required, %s passed", nParams, size);
            }
            Encoder.encodeExecParams(arena, nParams, params, paramOids);
            final long resPtr = Native.execPrepared(connPtr, stmtName, arena.ptr());
            final PGRES status = Wrapper.resultStatus(resPtr);
            return switch (status) {
                case TUPLES_OK -> new PQResult(connPtr, resPtr, arena, lock, false);
                default -> {
                    Native.closeResult(resPtr);
                    final String message = Native.connError(connPtr);
                    throw error(message);
                }
            };
        }
    }

    public PQResult executeMulti(final List<Object> params, final int chunkSize) {
        try (var ignored = lock()) {
            ensureOpen();

            final int size = params.size();
            if (size != this.nParams) {
                throw PQError.error("parameters mismatch: %s required, %s passed", nParams, size);
            }
            Encoder.encodeExecParams(arena, nParams, params, paramOids);
            final int status = Native.sendQueryPrepared(connPtr, stmtName, arena.ptr());
            if (status != 1) {
                final String message = Native.connError(connPtr);
                throw error(message);
            }
            Native.setChunkedRowsMode(connPtr, chunkSize);
            final long resPtr = Native.getResult(connPtr);
            final PGRES pgres = Wrapper.resultStatus(resPtr);
            return switch (pgres) {
                case TUPLES_CHUNK -> new PQResult(connPtr, resPtr, arena, lock, true);
                default -> {
                    Native.closeResult(resPtr);
                    final String message = Native.connError(connPtr);
                    throw error(message);
                }
            };
        }
    }

    @Override
    public void close() {
        if (isClosed) {
            return;
        }
        try (var ignored = lock()) {
            isClosed = true;
            final long resPtr = Native.closeStatement(connPtr, stmtName);
            final PGRES status = Wrapper.resultStatus(resPtr);
            Native.closeResult(resPtr);
            switch (status) {
                case COMMAND_OK -> {}
                default -> throw error("failed to close statement: %s, code: %s", stmtName, status);

            }
        }
    }
}
