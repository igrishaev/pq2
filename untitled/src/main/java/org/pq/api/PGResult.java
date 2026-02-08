package org.pq.api;

import org.pq.Native;
import org.pq.codec.Decoder;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Iterator;

public class PGResult implements AutoCloseable, Iterator<PGResult> {

    private final PQClient client;
    private final long resPtr;
    private final int nTuples;
    private final int nColumns;
    private final String[] columns;
    private final int[] formats;
    private final int[] oids;
    private final int[] tableOids;
    private final int[] typeMods;
    private int current;

    private PGResult(
            final PQClient client,
            final long resPtr,
            final int nTuples,
            final int nColumns,
            final String[] columns,
            final int[] formats,
            final int[] oids,
            final int[] tableOids,
            final int[] typeMods
    ) {
        this.client    = client;
        this.resPtr    = resPtr;
        this.nTuples   = nTuples;
        this.nColumns  = nColumns;
        this.columns   = columns;
        this.formats   = formats;
        this.oids      = oids;
        this.tableOids = tableOids;
        this.typeMods  = typeMods;
        this.current   = -1;
    }

    public static PGResult of(final PQClient client, final long resPtr, final ByteBuffer bb) {
        bb.rewind();
        client.bbJVM();

        final int nTuples = bb.getInt();
        final int nColumns = bb.getInt();

        final String[] columns = new String[nColumns];
        final int[] oids = new int[nColumns];
        final int[] formats = new int[nColumns];
        final int[] tableOids = new int[nColumns];
        final int[] typeMods = new int[nColumns];

        for (int i = 0; i < nColumns; i++) {
            oids[i] = bb.getInt();
            formats[i] = bb.getInt();
            tableOids[i] = bb.getInt();
            typeMods[i] = bb.getInt();
            columns[i] = BB.getString(bb);
        }

        return new PGResult(
                client, resPtr,
                nTuples, nColumns, columns,
                formats, oids, tableOids, typeMods
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
        return getObject(current, col);
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
    public boolean hasNext() {
        return current < nTuples - 1;
    }

    @Override
    public PGResult next() {
        current += 1;
        return this;
    }
}
