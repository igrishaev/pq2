public static void encodeBB(final ByteBuffer bb, long bbPtr, Object[] params, int[] oids, int[] formats, int resultFormat) {

    bb.rewind();
    bb.order(ByteOrder.LITTLE_ENDIAN);

    int nParams = params.length;

    // nParams
    bb.putInt(nParams);

    // paramTypes
    for (int i = 0; i < nParams; i++) {
        bb.putInt(oids[i]);
    }

    // paramValues
    int posPtr = bb.position();
    skip(bb, 8 * nParams);

    // paramLengths
    int posLen = bb.position();
    skip(bb, 4 * nParams);

    // paramFormats
    for (int i = 0; i < nParams; i++) {
        bb.putInt(formats[i]);
    }

    // resultFormat
    bb.putInt(resultFormat);

    // update lengths and pointers
    int len;
    int pos;
    FORMAT format;
    for (int i = 0; i < nParams; i++) {
        format = FORMAT.of(formats[i]);
        pos = bb.position();
        bb.order(ByteOrder.BIG_ENDIAN);
        switch (format) {
        case TXT -> encodeText(oids[i], params[i], bb);
        case BIN -> encodeBin(oids[i], params[i], bb);
        }
        len = bb.position() - pos;
        bb.order(ByteOrder.LITTLE_ENDIAN);
        bb.putInt(posLen + i * 4, len);
        bb.putLong(posPtr + i * 8, bbPtr + pos);
    }

}


public PGResult execWithParams(final String sql, final List<Object> params) {
    final int len = params.size();
    final Object[] prms = params.toArray(new Object[0]);
    final int[] oids = new int[] {23};
    final int[] formats = new int[] {1};
    Encoder.encodeBB(arena.bb(), arena.bbPtr(), prms, oids, formats, 1);

    final long resPtr = Native.execWithParams(connPtr, sql, arena.bbPtr());
    if (resPtr == arena.NULL()) {
        throw PQError.error("PQExec returned null (most likely no enough memory)");
    }
    int opStatus = Native.PQresultStatus(resPtr);
    PGRES pgres = PGRES.of(opStatus);
    switch (pgres) {
    case FATAL_ERROR, NONFATAL_ERROR, BAD_RESPONSE -> {
        Native.PQclear(resPtr);
        String message = Native.PQerrorMessage(connPtr);
        throw PQError.error(message);
    }
    }
    opStatus = Native.PGresultInfo(resPtr, arena.bbPtr());
    if (opStatus != 0) {
        Native.PQclear(resPtr);
        throw PQError.error("PGresultInfo returned non-zero status: %s", opStatus);
    }
    return PGResult.of(arena);

}
