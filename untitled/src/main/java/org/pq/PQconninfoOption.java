package org.pq;

public record PQconninfoOption(
        String keyword,
        String envvar,
        String compiled,
        String val,
        String label,
        String dispchar,
        int dispsize
) {}
