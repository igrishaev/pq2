package org.pq.api;

public class PQError extends RuntimeException {

    public static PQError of(final String message) {
        return new PQError(message);
    }

    public static PQError of(final String template, final Object... args) {
        return new PQError(String.format(template, args));
    }

    private PQError(String message) {
        super(message);
    }
}
