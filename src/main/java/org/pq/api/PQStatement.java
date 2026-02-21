package org.pq.api;

import org.pq.Native;
import org.pq.codec.Encoder;

import static org.pq.api.PQError.error;
import java.util.List;

public record PQStatement(
        long connPtr,
        Arena arena,
        String stmtName,
        String query,
        int nParams,
        int[] paramOids
) implements AutoCloseable {

    public PQResult execute() {
        return execute(List.of());
    }

    public PQResult execute(final List<Object> params) {
        final int size = params.size();
        if (size != this.nParams) {
            throw PQError.error("parameters mismatch: %s required, %s passed", nParams, size);
        }
        Encoder.encodeExecParams(arena, nParams, params, paramOids);
        final long resPtr = Native.execPrepared(connPtr, stmtName, arena.ptr());
        final PGRES status = Native.resultStatus(resPtr);
        return switch (status) {
            case TUPLES_OK -> new PQResult(connPtr, resPtr, arena, false);
            default -> {
                Native.closeResult(resPtr);
                final String message = Native.connError(connPtr);
                throw error(message);
            }
        };
    }

    public PQResult executeMulti(final List<Object> params, final int chunkSize) {
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
        final PGRES pgres = Native.resultStatus(resPtr);
        return switch (pgres) {
            case TUPLES_CHUNK -> new PQResult(connPtr, resPtr, arena, true);
            default -> {
                Native.closeResult(resPtr);
                final String message = Native.connError(connPtr);
                throw error(message);
            }
        };
    }

    @Override
    public void close() {
        final long resPtr = Native.closeStatement(connPtr, stmtName);
        final PGRES status = Native.resultStatus(resPtr);
        Native.closeResult(resPtr);
        switch (status) {
            case COMMAND_OK -> {}
            default -> throw error("failed to close statement: %s, code: %s", stmtName, status);

        }
    }
}
