package org.pq.api;

import org.pq.Native;
import org.pq.Wrapper;
import org.pq.codec.Decoder;

import java.util.Iterator;

import static org.pq.api.PQError.error;

public class PQResult implements AutoCloseable {

    private final long connPtr;
    private long resPtr;
    private final Arena arena;
    private int row;
    private final boolean isMulti;
    private final int nColumns;
    private int nTuples;

    public PQResult(long connPtr, long resPtr, Arena arena, boolean isMulti) {
        this.connPtr = connPtr;
        this.resPtr = resPtr;
        this.arena = arena;
        this.isMulti = isMulti;
        this.row = -1;
        this.nColumns = Native.nColumns(resPtr);
        this.nTuples = Native.nTuples(resPtr);
    }

    public void reset() {
        row = -1;
    }

    public Object getColumn(final int col) {
        if (!(0 <= col && col < nColumns)) {
            throw error("column is out of bounds: %s", col);
        }
        if (!(0 <= row && row < nTuples)) {
            throw error("The current row is out of bounds: %s. Perhaps you need to call .next() or .reset()", row);
        }

        final boolean isNull = Native.fieldIsNull(resPtr, row, col);

        if (isNull) {
            return null;
        }

        final int oid = Native.fieldOid(resPtr, col);
        final int format = Native.fieldFormat(resPtr, col);
        final int len = Native.fieldLength(resPtr, row, col);

        Native.fieldValue(resPtr, row, col, arena.ptr());

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

        final boolean isEnd = (row == nTuples - 1);
        if (isEnd && isMulti) {
            Native.closeResult(resPtr);
            final long newPtr = Native.getResult(connPtr);
            final PGRES status = Wrapper.resultStatus(newPtr);
            switch (status) {
                case TUPLES_CHUNK, TUPLES_OK -> {
                    resPtr = newPtr;
                    row = -1;
                    nTuples = Native.nTuples(newPtr);
                }
                default -> throw error("wrong result status: %s", status);
            }
        }

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
        if (isMulti) {
            while (resPtr != 0) {
                Native.closeResult(resPtr);
                resPtr = Native.getResult(connPtr);
            }
        } else {
            Native.closeResult(resPtr);
        }
    }
}
