package org.pq.api;

import org.pq.Native;
import org.pq.codec.Decoder;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

public class PGResult implements AutoCloseable, Iterable<Integer> {

    // TODO: remove ref to the client
    private final PQClient client;
    private final long resPtr;
    private final int nTuples;
    private final int nColumns;
    private final String[] columns;
    private final int[] columnFormats;
    private final int[] columnOids;
    private final int[] tableOids;
    private final int[] typeMods;
    private final int nParams;
    protected final int[] paramOids;
    private int currentRow;

    private PGResult(
            final PQClient client,
            final long resPtr,
            final int nTuples,
            final int nColumns,
            final String[] columns,
            final int[] columnFormats,
            final int[] columnOids,
            final int[] tableOids,
            final int[] typeMods,
            final int nParams,
            final int[] paramOids
    ) {
        this.client    = client;
        this.resPtr    = resPtr;
        this.nTuples   = nTuples;
        this.nColumns  = nColumns;
        this.columns   = columns;
        this.columnFormats = columnFormats;
        this.columnOids = columnOids;
        this.tableOids = tableOids;
        this.typeMods  = typeMods;
        this.nParams = nParams;
        this.paramOids = paramOids;
        this.currentRow = -1;
    }

    public static PGResult of(final PQClient client, final ByteBuffer bb) {
        bb.rewind();
        client.bbCPP();

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
            columns[i] = BB.getString(bb);
        }

        final int nParams = bb.getInt();
        final int[] paramOids = new int[nParams];
        for (int i = 0; i < nParams; i++) {
            paramOids[i] = bb.getInt();
        }

        return new PGResult(
                client, resPtr,
                nTuples, nColumns, columns, columnFormats, columnOids, tableOids, typeMods,
                nParams, paramOids
        );
    }

//    @Override
//    public String toString() {
//        return String.format("PGResult[client=%s, resPtr=%d, nTuples=%d, nColumns=%d, current=%d, columns=%s, formats=%s, oids=%s, tableOids=%s, typeMods=%s",
//                client,
//                resPtr,
//                nTuples,
//                nColumns,
//                current,
//                Arrays.toString(columns),
//                Arrays.toString(formats),
//                Arrays.toString(oids),
//                Arrays.toString(tableOids),
//                Arrays.toString(typeMods)
//        );
//    }

    public int getIndex(final String column) {
        for (int s = 0; s < nColumns; s++) {
            if (columns[s].equals(column)) {
                return s;
            }
        }
        throw PQError.of("missing column: %s", column);
    }

    public Object getObject(final int col) {
        return getObject(currentRow, col);
    }

    public Object getObject(final int row, final int col) {

        // TODO check row
        // TODO check col

        final int opStatus = Native.fetchField(resPtr, client.bbPtr, row, col);
        if (opStatus != 0) {
            throw PQError.of("fetchField returned non-zero status: %s, row: %s, column: %s",
                             opStatus, row, col);
        }

        client.rewind();
        client.bbCPP();

        // is null?
        if (client.bb.getInt() == 1) {
            return null;
        }

        final int oid = client.bb.getInt();
        final int format = client.bb.getInt();
        final int len = client.bb.getInt();

        client.bbJVM();
        // client.bbCPP();
        // client.bbDebug(64);

        return switch (FORMAT.of(format)) {
            case TXT -> {
                final byte[] ba = new byte[len];
                client.bb.get(ba);
                yield Decoder.decodeTxt(oid, new String(ba, StandardCharsets.UTF_8));
            }
            case BIN -> Decoder.decodeBin(oid, len, client.bb);
        };
    }

    @Override
    public void close() {
        Native.PQclear(resPtr);
    }

    @Override
    public Iterator<Integer> iterator() {
        return new Iterator<>() {
            @Override
            public boolean hasNext() {
                return currentRow < nTuples - 1;
            }

            @Override
            public Integer next() {
                return ++currentRow;
            }
        };
    }
}
