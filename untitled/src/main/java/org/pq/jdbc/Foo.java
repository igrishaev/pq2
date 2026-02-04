package org.pq.jdbc;

import org.pq.Native;

public record Foo(
        long result, int width, int height, String[] columns, int[] formats, int[] oids) {

    public static Foo ofResult(long result) {
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
        return new Foo(result, width, height, columns, formats, oids);
    }

    public int getIndex(final String column) {
        for (int s = 0; s < width; s++) {
            if (columns[s].equals(column)) {
                return s;
            }
        }
        return -1;
    }
}
