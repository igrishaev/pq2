package org.pq;

import org.pq.tool.OS;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.Objects;

public class Native {

    public static File dumpLib(final String filename) throws IOException {
        final File temp = File.createTempFile("temp", ".lib");
        temp.deleteOnExit();
        try (InputStream in = Native.class.getResourceAsStream(filename);
             OutputStream out = new FileOutputStream(temp)) {
            Objects.requireNonNull(in).transferTo(out);
        }
        return temp;
    }

    public static void loadLibs() {
        final String prefix = OS.prefix();
        try {
            System.load(dumpLib(String.format("/%s_libpq.lib", prefix)).getAbsolutePath());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        try {
            System.load(dumpLib(String.format("/%s_api.lib", prefix)).getAbsolutePath());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    static {
        loadLibs();
    }

    public static native long connect(String connInfo);
    public static native void closeConnection (long conn);
    public static native int connStatus(long conn);
    public static native int resultStatus(long result);
    public static native String connError(long conn);
    public static native int initByteBuffer(ByteBuffer bb);
    public static native void closeResult(long result);
    public static native long prepare(long conn, String name, String query);
    public static native long describe(long conn, String name);
    public static native long closeStatement(long conn, String stmt);
    public static native long execPrepared(long conn, String stmtName, long bb);
    public static native int sendQueryPrepared(long conn, String stmtName, long bb);
    public static native void fieldValue(long result, int row, int col, long bb);
    public static native boolean fieldIsNull(long result, int row, int col);
    public static native int fieldOid(long result, int col);
    public static native int fieldFormat(long result, int col);
    public static native int fieldLength(long result, int row, int col);
    public static native int nTuples(long result);
    public static native long query(long conn, String query);
    public static native int sendQuery(long conn, String query);
    public static native int setChunkedRowsMode(long conn, int size);
    public static native int nColumns(long result);
    public static native int nParams(long result);
    public static native int paramOid(long result, int i);
    public static native long getResult(long conn);
}
