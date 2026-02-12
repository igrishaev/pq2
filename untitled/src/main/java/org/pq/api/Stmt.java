package org.pq.api;

import org.pq.Native;
import org.pq.codec.Encoder;

import java.util.List;

public record Stmt (
        long connPtr,
        Arena arena,
        String stmtName,
        PGResult result
) implements AutoCloseable {

    @Override
    public void close() {
        // TODO: close
        // Native.PQclosePrepared(client.connPtr, stmtName);
        // check PGRES_COMMAND_OK
    }

    public PGResult execute(List<Object> params) {

        final int nParams = params.size();
        if (nParams != result.nParams()) {
            throw PQError.error("parameters mismatch: %s required, %s passed", result.nParams(), nParams);
        }
        final int[] oids = result.paramOids();
        Encoder.encodeExecParams(arena, nParams, params, oids);

        final long resPtr = Native._PQexecPrepared(connPtr, stmtName, arena.bbPtr());
        return PGResult.of(arena);
    }
}
