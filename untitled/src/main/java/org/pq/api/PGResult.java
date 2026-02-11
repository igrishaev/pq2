package org.pq.api;

import org.pq.Native;
import org.pq.codec.Decoder;
import static org.pq.api.PQError.error;
import static org.pq.tool.BB.*;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

public record PGResult(
        Arena arena,
        long resPtr, 
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

    public static PGResult of(final Arena arena) {
        arena.rewind();
        arena.orderCPP();

        final ByteBuffer bb = arena.bb();

        final long resPtr = bb.getLong();

        final int nTuples = bb.getInt();
        final int nColumns = bb.getInt();

        final String[] columns = new String[nColumns];
        final int[] columnOids = new int[nColumns];
        final int[] columnFormats = new int[nColumns];
        final int[] tableOids = new int[nColumns];
        final int[] typeMods = new int[nColumns];

        for (int i = 0; i < nColumns; i++) {
            columnOids[i] = bb.getInt();
            columnFormats[i] = bb.getInt();
            tableOids[i] = bb.getInt();
            typeMods[i] = bb.getInt();
            columns[i] = getLenString(bb);
        }

        final int nParams = bb.getInt();
        final int[] paramOids = new int[nParams];
        for (int i = 0; i < nParams; i++) {
            paramOids[i] = bb.getInt();
        }

        return new PGResult(arena, resPtr, nTuples, nColumns, columns,
                columnFormats, columnOids, tableOids, typeMods,
                nParams, paramOids
        );
    }
    
    public int getColIndex(final String column) {
        for (int s = 0; s < nColumns; s++) {
            if (columns[s].equals(column)) {
                return s;
            }
        }
        throw error("missing column: %s", column);
    }

    public Object getObject(final int row, final int col) {

        // TODO check row
        // TODO check col

        final int opStatus = Native.fetchField(resPtr, arena.bbPtr(), row, col);
        if (opStatus != 0) {
            throw error("fetchField returned non-zero status: %s, row: %s, column: %s",
                    opStatus, row, col);
        }

        arena.rewind();
        arena.orderCPP();

        final ByteBuffer bb = arena.bb();

        // is null?
        if (bb.getInt() == 1) {
            return null;
        }

        final int oid = bb.getInt();
        final int format = bb.getInt();
        final int len = bb.getInt();

        arena.orderJVM();

        return switch (FORMAT.of(format)) {
            case TXT -> {
                final byte[] ba = new byte[len];
                bb.get(ba);
                yield Decoder.decodeTxt(oid, new String(ba, StandardCharsets.UTF_8));
            }
            case BIN -> Decoder.decodeBin(oid, len, bb);
        };
    }

    @Override
    public void close() {
        Native.PQclear(resPtr);
    }

    public Iterable<Integer> iterRows() {
        return () -> new Iterator<>() {
            private int i = -1;

            @Override
            public boolean hasNext() {
                return i < nTuples - 1;
            }

            @Override
            public Integer next() {
                return ++i;
            }
        };
    }

    public Iterable<Integer> iterCols() {
        return () -> new Iterator<>() {
            private int i = -1;

            @Override
            public boolean hasNext() {
                return i < nColumns - 1;
            }

            @Override
            public Integer next() {
                return ++i;
            }
        };
    }

}
