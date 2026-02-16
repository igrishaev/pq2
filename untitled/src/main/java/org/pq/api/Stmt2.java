package org.pq.api;

import org.pq.Native2;
import org.pq.codec.Encoder;
import static org.pq.api.PQError.error;
import java.util.List;

import static org.pq.api.PGRES.TUPLES_OK;

public record Stmt2 (
        PQClient client,
        String stmtName,
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
) implements AutoCloseable {
    public static Stmt2 of(final PQClient client, final String name, final String query, final Arena arena) {
        arena.rewind();
        arena.orderCPP();

        // ptr
        arena.getLong();

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

    public Result execute(final List<Object> params) {
        final int size = params.size();
        final Arena arena = client.arena();
        if (size != this.nParams) {
            throw PQError.error("parameters mismatch: %s required, %s passed", nParams, size);
        }
        Encoder.encodeExecParams(arena, nParams, params, paramOids);
        final long ptr = Native2.execPrepared(client.ptr(), stmtName, arena.ptr());
        final PGRES status = PQClient.resStatus(ptr);
        if (status != TUPLES_OK) {
            Native2.closeResult(ptr);
            final String message = Native2.connError(client.ptr());
            throw error(message);
        } else {
            return Result.of(ptr, this);
        }



//        final long resPtr = Native._PQexecPrepared(ptr, stmtName, arena.ptr());
//        return PGResult.of(arena);
    }

    @Override
    public void close() {
        client.closeStatement(this);
    }
}
