package org.pq.api;

import org.pq.Native2;
import org.pq.codec.Decoder;

import java.nio.charset.StandardCharsets;
import java.util.Iterator;

import static org.pq.api.PQError.error;

public class Result implements AutoCloseable {

    private final long ptr;
    private final Stmt2 statement;
    private final int nTuples;
    private final int oid;
    private final String cmdStatus;
    private final String cmdTuples;
    private int row;

    private Result(long ptr, Stmt2 statement, int nTuples, int oid, String cmdStatus, String cmdTuples) {
        this.ptr = ptr;
        this.nTuples = nTuples;
        this.oid = oid;
        this.statement = statement;
        this.cmdStatus = cmdStatus;
        this.cmdTuples = cmdTuples;
        this.row = -1;
    }

    public static Result of(long ptr, Stmt2 statement) {
        Arena arena = statement.client().arena();
        Native2.resultInfo(ptr, arena.ptr());
        arena.rewind();
        arena.orderCPP();
        final int nTuples = arena.getInt();
        final int oid = arena.getInt();
        final String cmdStatus = arena.getLenString();
        final String cmdTuples = arena.getLenString();
        return new Result(ptr, statement, nTuples, oid, cmdStatus, cmdTuples);
    }

    public Object getColumn(final int col) {
        if (!(0 <= col && col < statement.nColumns())) {
            throw error("column is out of bounds: %s", col);
        }
        if (!(0 <= row && row < nTuples)) {
            throw error("row is out of bounds: %s", row);
        }
        final Arena arena = statement.client().arena();
        Native2.fetchField(ptr, row, col, arena.ptr());

        arena.rewind();
        arena.orderCPP();

        final int isNull = arena.getInt();
        if (isNull == 1) {
            return null;
        }

        final int oid = arena.getInt();
        final int format = arena.getInt();
        final int len = arena.getInt();
        arena.orderJVM();
        if (format == 0) {
            final byte[] ba = new byte[len];
            arena.get(ba);
            return Decoder.decodeTxt(oid, new String(ba, StandardCharsets.UTF_8));
        } else {
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
            private final int nColumns = statement.nColumns();

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
