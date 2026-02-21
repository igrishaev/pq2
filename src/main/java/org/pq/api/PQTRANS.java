package org.pq.api;

public enum PQTRANS {
    IDLE,
    ACTIVE,
    INTRANS,
    INERROR,
    UNKNOWN;
    private static final PQTRANS[] vals = values();
    public static PQTRANS of(final int code) {
        return vals[code];
    }
}
