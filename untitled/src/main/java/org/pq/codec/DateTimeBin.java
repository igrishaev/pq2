package org.pq.codec;

import java.nio.ByteBuffer;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

public class DateTimeBin {

    private static final Duration PG_DIFF;
    private static final long PG_DIFF_DAYS;

    // 946684800 seconds
    static {
        PG_DIFF = Duration.between(
                Instant.EPOCH,
                LocalDate.of(2000, 1, 1).atStartOfDay(ZoneOffset.UTC)
        );
        PG_DIFF_DAYS = PG_DIFF.toDays();
    }

    public static LocalDate decodeDATE (final ByteBuffer buf) {
        final int days = buf.getInt();
        return LocalDate.ofEpochDay(days + PG_DIFF_DAYS);
    }


}
