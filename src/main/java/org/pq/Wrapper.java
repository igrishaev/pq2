package org.pq;

import org.pq.api.CONNECTION;
import org.pq.api.PGRES;
import org.pq.api.PQTRANS;

public class Wrapper {

    public static PGRES resultStatus(final long result) {
        return PGRES.of(Native.resultStatus(result));
    }

    public static CONNECTION connStatus(final long connPtr) {
        return CONNECTION.of(Native.connStatus(connPtr));
    }

    public static PQTRANS txStatus(final long connPtr) {
        return PQTRANS.of(Native.transactionStatus(connPtr));
    }

    public static int affectedRows(final long resPtr) {
        final String result = Native.affectedRows(resPtr);
        if (result.isEmpty()) {
            return -1;
        } else {
            return Integer.parseInt(result);
        }
    }
}
