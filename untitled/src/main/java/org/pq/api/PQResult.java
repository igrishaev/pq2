package org.pq.api;

import org.pq.Native;
import org.pq.codec.Decoder;

import java.util.Iterator;

import static org.pq.api.PQError.error;

public class PQResult implements AutoCloseable {

    private final long connPtr;
    private long resPtr;
    private final Arena arena;
    private int row;
    private final boolean isMulti;

    public PQResult(long connPtr, long resPtr, Arena arena, boolean isMulti) {
        this.connPtr = connPtr;
        this.resPtr = resPtr;
        this.arena = arena;
        this.isMulti = isMulti;
    }

    private int nColumns() {
        return Native.nColumns(resPtr);
    }

    private int nTuples() {
        return Native.nTuples(resPtr);
    }

    public Object getColumn(final int col) {
        if (!(0 <= col && col < nColumns())) {
            throw error("column is out of bounds: %s", col);
        }
        if (!(0 <= row && row < nTuples())) {
            throw error("row is out of bounds: %s", row);
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

        final boolean isEnd = (row == nTuples() - 1);
        if (isEnd && isMulti) {
            Native.closeResult(resPtr);
            final long newPtr = Native.getResult(connPtr);
            final PGRES status = PQClient.resStatus(newPtr);
            if (status != PGRES.COMMAND_OK) {
                Native.closeResult(newPtr);
                throw error("wrong result status: %s", status);
            } else {
                resPtr = newPtr;
                row = -1;
            }
        }

        if (row < nTuples() - 1) {
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
                return i <  nColumns() - 1;
            }
            @Override
            public Integer next() {
                return ++i;
            }
        };
    }

    @Override
    public void close() {
        Native.closeResult(resPtr);
    }
}
