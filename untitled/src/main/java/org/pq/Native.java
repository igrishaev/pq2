package org.pq;

public class Native {

    static {
        // System.load("/opt/homebrew/Cellar/libpq/18.1/lib/libpq.dylib");
        System.load("/Users/ivan/work/pq2/untitled/libfoo.dylib");
    }

    public static native String foobar(final int param);

    public static native long connect(final String connString);

    public static void main(final String... args) {
        System.out.println(connect("host=localhost port=5432 dbname=book user=book password=book"));
        System.out.println(foobar(42));
    }

}
