package org.pq.api;

import org.pq.Native;

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

    public void execute(List<Object> params) {

    }
}
