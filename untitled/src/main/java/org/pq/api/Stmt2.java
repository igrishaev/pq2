package org.pq.api;

import org.pq.codec.Encoder;

import java.util.List;

public record Stmt2 (
        PQClient client,
        String name,
        String query,
        int nTuples,
        int nColumns,
        String[] columns,
        int[] columnFormats,
        int[] columnOids,
        int[] tableOids,
        int[] typeMods,
        int nParams,
        int[] paramOids
) {
    public static Stmt2 of(final PQClient client, final String name, final String query, final Arena arena) {
        arena.rewind();
        arena.orderCPP();

        final long resPtr = arena.getLong();

        final int nTuples = arena.getInt();
        final int nColumns = arena.getInt();

        final String[] columns = new String[nColumns];
        final int[] columnOids = new int[nColumns];
        final int[] columnFormats = new int[nColumns];
        final int[] tableOids = new int[nColumns];
        final int[] typeMods = new int[nColumns];

        for (int i = 0; i < nColumns; i++) {
            columnOids[i] = arena.getInt();
            columnFormats[i] = arena.getInt();
            tableOids[i] = arena.getInt();
            typeMods[i] = arena.getInt();
            columns[i] = arena.getLenString();
        }

        final int nParams = arena.getInt();
        final int[] paramOids = new int[nParams];
        for (int i = 0; i < nParams; i++) {
            paramOids[i] = arena.getInt();
        }

        return new Stmt2(client, name, query, nTuples, nColumns, columns,
                columnFormats, columnOids, tableOids, typeMods,
                nParams, paramOids
        );
    }

    public void execute(final List<Object> params) {
        final int size = params.size();
        if (size != this.nParams) {
            throw PQError.error("parameters mismatch: %s required, %s passed", nParams, size);
        }
        Encoder.encodeExecParams(client.arena(), nParams, params, paramOids);

        final long resPtr = Native._PQexecPrepared(connPtr, stmtName, arena.ptr());
        return PGResult.of(arena);
    }
}
