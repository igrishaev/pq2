package org.pq.api;

import org.pq.Native;
import org.pq.codec.Encoder;

import java.util.List;

public record Stmt (
        PQClient client,
        String stmtName,
        PGResult result
) implements AutoCloseable {

    @Override
    public void close() {
        // Native.PQclosePrepared(client.connPtr, stmtName);
        // check PGRES_COMMAND_OK
    }

    public PGResult execute(List<Object> params) {

        final int nParams = params.size();
        if (nParams != result.nParams) {
            throw PQError.of("parameters mismatch: %s required, %s passed", result.nParams, nParams);
        }
        final int[] oids = result.paramOids;
        Encoder.encodePrepared(client.bb, client.bbPtr, nParams, params, oids);
        final long resPtr = Native._PQexecPrepared(client.connPtr, stmtName, client.bbPtr);
        return PGResult.of(client, client.bb);
    }
}
