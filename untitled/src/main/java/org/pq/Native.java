package org.pq;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.UUID;

public class Native {

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

    public enum CONNECTION {
        CONNECTION_OK,
        CONNECTION_BAD,
        CONNECTION_STARTED,
        CONNECTION_MADE,
        CONNECTION_AWAITING_RESPONSE,
        CONNECTION_AUTH_OK,
        CONNECTION_SETENV,
        CONNECTION_SSL_STARTUP,
        CONNECTION_NEEDED,
        CONNECTION_CHECK_WRITABLE,
        CONNECTION_CONSUME,
        CONNECTION_GSS_STARTUP,
        CONNECTION_CHECK_TARGET,
        CONNECTION_CHECK_STANDBY,
        CONNECTION_ALLOCATED,
        CONNECTION_AUTHENTICATING;
        private static final CONNECTION[] vals = values();
        public static CONNECTION of(final int code) {
            return vals[code];
        }
    }

    enum PQTRANS {
        IDLE,
        ACTIVE,
        INTRANS,
        INERROR,
        UNKNOWN;
        private static final PQTRANS[] vals = values();
        public static PQTRANS of(final int code) {
            return vals[code];
        }
    }

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

    static {
        System.load("/opt/homebrew/Cellar/libpq/18.1/lib/libpq.dylib");
        System.load("/Users/ivan.grishaev-external/work/pq2/untitled/libfoo.dylib");
    }

    /* INIT */

    // PQconnectdbParams
    public static native long PQconnectdb(final String conninfo);
    // PQsetdbLogin
    // PQsetdb
    // PQconnectStartParams
    // PQconnectStart
    // PQconnectPoll
    // PQsocketPoll
    public static native PQconninfoOption[] PQconndefaults();
    public static native PQconninfoOption[] PQconninfo(final long conn);
    // PQconninfoParse
    public static native void PQfinish (final long conn);
    public static native void PQreset (final long conn);
    // PQresetStart
    // PQresetPoll
    // PQpingParams
    public static native int PQping(final String conninfo);
    // PQsetSSLKeyPassHook_OpenSSL
    // PQgetSSLKeyPassHook_OpenSSL

    /* STATE */

    // public static native String PQdb(final long conn);
    // public static native String PQuser(final long conn);
    // public static native String PQpass(final long conn);
    // public static native String PQhost(final long conn);
    // public static native String PQhostaddr(final long conn);
    // public static native String PQport(final long conn);
    // public static native String PQoptions(final long conn);
    public static native int PQstatus(final long conn);
    public static native int PQtransactionStatus(final long conn);
    // PQparameterStatus
    // PQfullProtocolVersion
    // PQprotocolVersion
    // PQserverVersion
    public static native String PQerrorMessage(final long conn);
    // PQsocket
    // PQbackendPID
    // PQconnectionNeedsPassword
    // PQconnectionUsedPassword
    // PQconnectionUsedGSSAPI
    // PQsslInUse
    // PQsslAttribute
    // PQsslAttributeNames
    // PQsslStruct
    // PQgetssl

    /* EXEC */

    public static native long PQexec(final long conn, final String command);
    // PQexecParams
    // PQprepare
    // PQexecPrepared
    // PQdescribePrepared
    // PQdescribePortal
    // PQclosePrepared
    // PQclosePortal
    public static native int PQresultStatus(final long result);
    public static native String PQresStatus(final int code);
    public static native String PQresultErrorMessage(final long result);
    // PQresultVerboseErrorMessage
    // PQresultErrorField
    public static native void PQclear(final long result);

    /* FIELDS */

    public static native int PQntuples(final long result);
    // PQnfields
    // PQfname
    // PQfnumber
    // PQftable
    // PQftablecol
    // PQfformat
    // PQftype
    // PQfmod
    // PQfsize
    // PQbinaryTuples
    public static native String PQgetvalue(final long result, final int row, final int col);
    public static native boolean PQgetisnull(final long result, final int row, final int col);
    public static native int PQgetlength(final long result, final int row, final int col);
    // PQnparams
    // PQparamtype
    // PQprint

    /* OTHER */

    // PQcmdStatus
    // PQcmdTuples
    // PQoidValue
    // PQoidStatus

    /* QUOTING */

    // PQescapeLiteral
    // PQescapeIdentifier
    // PQescapeStringConn
    // PQescapeString
    // PQescapeByteaConn
    // PQescapeBytea
    // PQunescapeBytea

    /* CUSTOM */

    public static native Object getValue(final long result, final int row, final int col);

    public static native int getInt(final long result, final int row, final int col);

    public static native long asLong(final long request, final int row, final int col);

    public static native long asLong(final long request, final int row, final int col, int offset);

    public static native String getString(final long result, final int row, final int col);

    public static native java.nio.ByteBuffer getBB(final long result, final int row, final int col);

    public static native byte[] getBytes(final long result, final int row, final int col);

    public static native Object[] getTuple(final long result, final int row);

    public static void main(final String... args) {
        final var conninfo = "host=localhost port=15432 dbname=test user=test password=test";
        final var conn = PQconnectdb(conninfo);
        // System.out.println(conn);

        final var st = PQstatus(conn);
        // System.out.println(st);

        final var m = PQerrorMessage(conn);
        // System.out.println(m);

        // System.out.println(result);
        final var result = PQexec(conn, "select '122b85b3-8385-48a6-9973-036cf8b04eac'::uuid from generate_series(1, 99) as seq(x)");

        final var status = PQresultStatus(result);
        // System.out.println(status);

        final var msg = PQresultErrorMessage(result);
        // System.out.println(msg);

        // System.out.println(PQgetvalue(result, 7, 0));

        long bits1 = asLong(result, 0, 0);
        long bits2 = asLong(result, 0, 0, 8);

        System.out.println(new UUID(bits1, bits2));


        // System.out.println(getValue(result, 1, 0));

//        final var pingres = PQping(conninfo);
//        System.out.println(pingres);
//
        final var opts1 = PQconndefaults();
        System.out.println(opts1.length);
        System.out.println(Arrays.toString(opts1));
//
//        final var opts2 = PQconninfo(conn);
//        System.out.println(opts2.length);
//        System.out.println(Arrays.toString(opts2));

        // final ByteBuffer bb = PQconndefaults(42);
        // System.out.println(bb.);
        // System.out.println(foobar(42));
    }

}
