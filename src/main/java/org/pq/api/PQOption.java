package org.pq.api;

public record PQOption(
        String keyword,
        String envvar,
        String compiled,
        String val,
        String label,
        String dispchar,
        int dispsize
) {}
