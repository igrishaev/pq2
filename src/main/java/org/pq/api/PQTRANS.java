package org.pq.api;

// https://github.com/postgres/postgres/blob/3322f01a11b79d4b3c84ae9322c0984891349e46/src/interfaces/libpq/libpq-fe.h#L151
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
