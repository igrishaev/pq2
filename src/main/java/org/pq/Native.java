package org.pq;

import org.pq.tool.OS;

import java.io.*;
import java.nio.ByteBuffer;

public class Native {

    public static File dumpLib(final String filename) throws IOException {
        final File temp = File.createTempFile(filename, ".lib");
        temp.deleteOnExit();
        try (InputStream in = Native.class.getResourceAsStream(filename);
             OutputStream out = new FileOutputStream(temp)) {
            if (in == null) {
                throw error("resource %s not found", filename);
            }
            in.transferTo(out);
        }
        return temp;
    }

    public static RuntimeException error(final String template, final Object... args) {
        return new RuntimeException(String.format(template, args));
    }

    public static RuntimeException error(final Throwable e, final String template, final Object... args) {
        return new RuntimeException(String.format(template, args), e);
    }

    public static void loadLib(final String name) {
        final String libPath = "/bin/" + OS.libName(name);
        try {
            System.load(dumpLib(libPath).getAbsolutePath());
        } catch (IOException e) {
            throw error(e, "failed to load library: %s", libPath);
        }
    }

    public static void loadLibs() {
        loadLib("libpq");
        loadLib("api");
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
    public static native int transactionStatus(long conn);
    public static native int libVersion();
}
