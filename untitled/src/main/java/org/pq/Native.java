package org.pq;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class Native {

    static {
        System.load("/opt/homebrew/Cellar/libpq/18.1/lib/libpq.dylib");
        System.load("/Users/ivan.grishaev-external/work/pq2/untitled/libfoo.dylib");
    }

    public static native String foobar(final int param);
    public static native long connect(final String connString);

    public static native long PQconnectdb(final String conninfo);

    public static native PQconninfoOption[] PQconndefaults();

    public static native void PQfinish (final long conn);

    public static native void PQreset (final long conn);

    public static native long PQping(final String conninfo);

    public static native String PQdb(final long conn);
    public static native String PQuser(final long conn);
    public static native String PQpass(final long conn);
    public static native String PQhost(final long conn);
    public static native String PQhostaddr(final long conn);
    public static native String PQport(final long conn);
    public static native String PQoptions(final long conn);


    public static void main(final String... args) {
        System.out.println(PQconnectdb("host=localhost port=5432 dbname=book user=book password=book"));
        final PQconninfoOption[] opts = PQconndefaults();
        System.out.println(opts.length);
        System.out.println(Arrays.toString(opts));
        // inal ByteBuffer bb = PQconndefaults(42);
        // System.out.println(bb.);
        // System.out.println(foobar(42));
    }

}
