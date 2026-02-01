package org.pq;

public class Native {

    static {
        System.load("/opt/homebrew/Cellar/libpq/18.1/lib/libpq.dylib");
        System.load("/Users/ivan.grishaev-external/work/pq2/untitled/libfoo.dylib");
    }

    public static native String foobar(final int param);

    public static native long connect(final String connString);

    public static native PGconn PQconnectdb(final String conninfo);

    public static native PQconninfoOption PQconninfoOption();

    public static native PQconninfoOption PQconninfo(final PGconn conn);

    public static native void PQfinish (final PGconn conn);

    public static native void PQreset (final PGconn conn);

    public static native int PQping(final String conninfo);

    public static void main(final String... args) {
        System.out.println(PQconnectdb("host=localhost port=5432 dbname=book user=book password=book"));
        // System.out.println(foobar(42));
    }

}
