package org.pq.api;

import org.pq.Native;

public record PGResult(
        long ptr,
        int nColumns,
        int nTuples,
        String[] columns,
        int[] formats,
        int[] oids
) {

    public static PGResult ofResult(long result) {
        // TODO: get in bulk with byte buffer
        int width = Native.PQnfields(result);
        int height = Native.PQntuples(result);
        String[] columns = new String[width];
        int[] formats = new int[width];
        int[] oids = new int[width];
        for (int i = 0; i < width; i++) {
            columns[i] = Native.PQfname(result, i);
            formats[i] = Native.PQfformat(result, i);
            oids[i] = Native.PQftype(result, i);
        }
        return new PGResult(result, width, height, columns, formats, oids);
    }

    public int getIndex(final String column) {
        for (int s = 0; s < nColumns; s++) {
            if (columns[s].equals(column)) {
                return s;
            }
        }
        throw PQError.of("missing column: %s", column);
    }
}
