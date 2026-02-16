package org.pq;

import java.nio.ByteBuffer;

public class Native2 {

    static {
        System.load("/opt/homebrew/opt/libpq/lib/libpq.dylib");
        System.load(String.format("/Users/%s/work/pq2/untitled/libfoo2.dylib", System.getenv("USER")));
    }
    public static native long connect(final String connInfo);
    public static native void closeConnection (final long connPtr);
    public static native int connStatus(final long connPtr);
    public static native int resStatus(final long resPtr);
    public static native String connError(final long connPtr);
    public static native int initByteBuffer(final ByteBuffer bb);
    public static native void closeResult(final long resPtr);
    public static native long prepare(final long connPtr, final String name, final String query);
    public static native long describe(final long connPtr, final String name);
    public static native long closeStatement(final long connPtr, final String stmt);
    public static native int serializePrepared(final long resPtr, final long bbPtr);
    public static native long execPrepared(final long connPtr, final String stmtName, final long bbPtr);
    public static native void resultInfo(final long resPtr, final long bbPtr);
    public static native void fetchField(final long resPtr, final int row, final int col, final long bbPtr);
}
