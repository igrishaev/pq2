package org.pq.api;

import org.pq.Native2;
import org.pq.codec.Decoder;

import java.util.Iterator;

import static org.pq.api.PQError.error;

public class Result implements AutoCloseable {

    private final long ptr;
    private final Arena arena;
    private final int nColumns;
    private final int nTuples;
    private int row;

    private Result(long ptr, Arena arena, int nTuples, int nColumns) {
        this.ptr = ptr;
        this.arena = arena;
        this.nTuples = nTuples;
        this.nColumns = nColumns;
        this.row = -1;
    }

    public static Result of(long ptr, Arena arena) {
        final int nTuples = Native2.nTuples(ptr);
        final int nColumns = Native2.nColumns(ptr);
        return new Result(ptr, arena, nTuples, nColumns);
    }

    public Object getColumn(final int col) {
        if (!(0 <= col && col < nColumns)) {
            throw error("column is out of bounds: %s", col);
        }
        if (!(0 <= row && row < nTuples)) {
            throw error("row is out of bounds: %s", row);
        }

        final boolean isNull = Native2.fieldIsNull(ptr, row, col);

        if (isNull) {
            return null;
        }

        final int oid = Native2.fieldOid(ptr, col);
        final int format = Native2.fieldFormat(ptr, col);
        final int len = Native2.fieldLength(ptr, row, col);

        Native2.fieldValue(ptr, row, col, arena.ptr());

        if (format == 0) {
            final String string = arena.getString(0, len);
            return Decoder.decodeTxt(oid, string);
        } else {
            arena.rewind();
            arena.orderJVM();
            return Decoder.decodeBin(oid, len, arena.bb());
        }
    }

    public boolean next() {
        if (row < nTuples - 1) {
            row++;
            return true;
        } else {
            return false;
        }
    }

    public Iterable<Integer> iterCols() {
        return () -> new Iterator<>() {
            private int i = -1;
            @Override
            public boolean hasNext() {
                return i <  nColumns - 1;
            }
            @Override
            public Integer next() {
                return ++i;
            }
        };
    }

    @Override
    public void close() {
        Native2.closeResult(ptr);
    }
}
