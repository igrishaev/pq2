package org.pq.api;

import org.pq.Native2;
import org.pq.codec.Encoder;

import static org.pq.api.PQError.error;
import java.util.List;

import static org.pq.api.PGRES.TUPLES_OK;

public record Stmt2 (
        long connPtr,
        Arena arena,
        String stmtName,
        String query,
        int nParams,
        int[] paramOids
) implements AutoCloseable {

    public static Stmt2 of(final PQClient client, final String stmtName, final String query) {

        final int nParams = Native2.nParams()
        final int[] paramOids = new int[3];

        return new Stmt2(client.ptr(), client.arena(), stmtName, query, nParams, paramOids);
    }

    public Result execute(final List<Object> params) {
        final int size = params.size();
        if (size != this.nParams) {
            throw PQError.error("parameters mismatch: %s required, %s passed", nParams, size);
        }
        Encoder.encodeExecParams(arena, nParams, params, paramOids);
        final long ptr = Native2.execPrepared(connPtr, stmtName, arena.ptr());
        final PGRES status = PQClient.resStatus(ptr);
        if (status != TUPLES_OK) {
            Native2.closeResult(ptr);
            final String message = Native2.connError(connPtr);
            throw error(message);
        } else {
            return Result.of(ptr, arena);
        }
    }

    @Override
    public void close() {
        final long resPtr = Native2.closeStatement(connPtr, stmtName);
        final PGRES status = PQClient.resStatus(resPtr);
        Native2.closeResult(resPtr);
        if (status != PGRES.COMMAND_OK) {
            throw error("failed to close statement: %s, code: %s", stmtName, status);
        }
    }
}
