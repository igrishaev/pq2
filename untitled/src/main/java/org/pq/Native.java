package org.pq;

public class Native {

    static {
        // System.load("/opt/homebrew/Cellar/libpq/18.1/lib/libpq.dylib");
        System.load("/Users/xxx/work/pq2/untitled/libfoo.dylib");
    }

    public static native String foobar(final int param);

    public static void main(final String... args) {
        System.out.println(foobar(42));
    }

}
