package org.pq.api;

enum PGRES {
    EMPTY_QUERY,
    COMMAND_OK,
    TUPLES_OK,
    COPY_OUT,
    COPY_IN,
    BAD_RESPONSE,
    NONFATAL_ERROR,
    FATAL_ERROR,
    COPY_BOTH,
    SINGLE_TUPLE,
    PIPELINE_SYNC,
    PIPELINE_ABORTED,
    TUPLES_CHUNK;
    private static final PGRES[] vals = values();
    public static PGRES of(final int code) {
        return vals[code];
    }
}