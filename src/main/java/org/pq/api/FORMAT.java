package org.pq.api;

public enum FORMAT {
    TXT,
    BIN;
    private static final FORMAT[] vals = values();
    public static FORMAT of(final int code) {
        return vals[code];
    }
}
