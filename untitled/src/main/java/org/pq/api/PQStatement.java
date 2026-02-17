package org.pq.api;

import org.pq.Native;
import org.pq.codec.Encoder;

import static org.pq.api.PGRES.TUPLES_CHUNK;
import static org.pq.api.PQError.error;
import java.util.List;

import static org.pq.api.PGRES.TUPLES_OK;

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
        final PGRES status = PQClient.resStatus(resPtr);
        if (status != TUPLES_OK) {
            Native.closeResult(resPtr);
            final String message = Native.connError(connPtr);
            throw error(message);
        } else {
            return new PQResult(connPtr, resPtr, arena, false);
        }
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
        final PGRES pgres = PQClient.resStatus(resPtr);
        if (pgres != TUPLES_CHUNK) {
            Native.closeResult(resPtr);
            final String message = Native.connError(connPtr);
            throw error(message);
        } else {
            return new PQResult(connPtr, resPtr, arena, true);
        }
    }

    @Override
    public void close() {
        final long resPtr = Native.closeStatement(connPtr, stmtName);
        final PGRES status = PQClient.resStatus(resPtr);
        Native.closeResult(resPtr);
        if (status != PGRES.COMMAND_OK) {
            throw error("failed to close statement: %s, code: %s", stmtName, status);
        }
    }
}
