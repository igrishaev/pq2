package org.pq;

import org.pq.api.CONNECTION;
import org.pq.api.PGRES;

public class Wrapper {
    public static PGRES resultStatus(final long result) {
        return PGRES.of(Native.resultStatus(result));
    }

    public static CONNECTION connStatus(final long connPtr) {
        return CONNECTION.of(Native.connStatus(connPtr));
    }
}
