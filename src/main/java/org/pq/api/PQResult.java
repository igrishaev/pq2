package org.pq.api;

import org.pq.Native;
import org.pq.Wrapper;
import org.pq.codec.Decoder;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.pq.api.PQError.error;

public class PQResult implements AutoCloseable {

    // todo add toString

    private final long connPtr;
    private long resPtr;
    private final Arena arena;
    private int row;
    private final boolean isMulti;
    private final int nColumns;
    private int nTuples;
    private final TryLock lock;
    private boolean isClosed;

    private TryLock lock() {
        return lock.lock();
    }

    public PQResult(final long connPtr,
                    final long resPtr,
                    final Arena arena,
                    final TryLock lock,
                    // todo rename
                    final boolean isMulti) {
        this.connPtr = connPtr;
        this.resPtr = resPtr;
        this.arena = arena;
        this.isMulti = isMulti;
        this.row = -1;
        // TODO move to static method
        // TODO: calculate once
        this.nColumns = Native.nColumns(resPtr);
        this.nTuples = Native.nTuples(resPtr);
        this.lock = lock;
        this.isClosed = false;
    }

    private void ensureOpen() {
        if (isClosed) {
            throw error("result is closed");
        }
    }

    public void reset() {
        ensureOpen();
        try (var ignored = lock()) {
            row = -1;
        }

    }

    // TODO: need lock?
    public String commandName() {
        ensureOpen();
        return Native.commandName(resPtr);
    }

    // TODO: need lock?
    public int affectedRows() {
        ensureOpen();
        return Wrapper.affectedRows(resPtr);
    }

    public Object getColumn(final int col) {
        try (var ignored = lock()) {
            ensureOpen();

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
    }

    public boolean next() {
        try (var ignored = lock()) {
            ensureOpen();
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

    // TODO: asMap
    // TODO: as array

    public List<Object> asList() {
        final List<Object> result = new ArrayList<>(nColumns);
        for (int i = 0; i < nColumns; i++) {
            result.set(i, getColumn(i));
        }
        return result;
    }

    @Override
    public void close() {
        if (isClosed) {
            return;
        }
        try (var ignored = lock()) {
            isClosed = true;
            if (isMulti) {
                while (resPtr != arena.NULL()) {
                    Native.closeResult(resPtr);
                    resPtr = Native.getResult(connPtr);
                }
            } else {
                Native.closeResult(resPtr);
            }
        }
    }
}
