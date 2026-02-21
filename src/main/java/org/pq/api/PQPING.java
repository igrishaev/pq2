package org.pq.api;

public enum PQPING {
    OK,
    REJECT,
    NO_RESPONSE,
    NO_ATTEMPT;
    private static final PQPING[] vals = values();
    public static PQPING of(final int code) {
        return vals[code];
    }
}
