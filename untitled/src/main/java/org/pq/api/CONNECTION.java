package org.pq.api;

public enum CONNECTION {
    OK,
    BAD,
    STARTED,
    MADE,
    AWAITING_RESPONSE,
    AUTH_OK,
    SETENV,
    SSL_STARTUP,
    NEEDED,
    CHECK_WRITABLE,
    CONSUME,
    GSS_STARTUP,
    CHECK_TARGET,
    CHECK_STANDBY,
    ALLOCATED,
    AUTHENTICATING;
    private static final CONNECTION[] vals = values();
    public static CONNECTION of(final int code) {
        return vals[code];
    }
}
