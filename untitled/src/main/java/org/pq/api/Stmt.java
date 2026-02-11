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
    public void close() throws Exception {
        // Native.PQclosePrepared(client.connPtr, stmtName);
        // check PGRES_COMMAND_OK
    }

    public PGResult execute(List<Object> params) {

        final int len = params.size();
        final Object[] prms = params.toArray(new Object[0]);
        final int[] oids = result.paramOids;
        final int[] formats = new int[] {1};
        Encoder.encodePrepared(client.bb, client.bbPtr, prms, oids, formats, 1);
        final long resPtr = Native._PQexecPrepared(client.connPtr, stmtName, client.bbPtr);
        return PGResult.of(client, client.bb);
    }
}
