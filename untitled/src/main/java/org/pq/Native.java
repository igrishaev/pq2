package org.pq;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class Native {

    public static class PQPING {
        static int OK = 0;
        static int REJECT = 1;
        static int NO_RESPONSE = 2;
        static int NO_ATTEMPT = 3;
    }

    static int PQPING_OK = 0;
    static int PQPING_REJECT = 1;
    static int PQPING_NO_RESPONSE = 2;
    static int PQPING_NO_ATTEMPT = 3;

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
    // - PQstatus int
    // - PQtransactionStatus
    // PQparameterStatus
    // PQfullProtocolVersion
    // PQprotocolVersion
    // PQserverVersion
    // - PQerrorMessage
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

    // - PQexec
    // PQexecParams
    // PQprepare
    // PQexecPrepared
    // PQdescribePrepared
    // PQdescribePortal
    // PQclosePrepared
    // PQclosePortal
    // PQresultStatus
    // - PQresStatus
    // - PQresultErrorMessage
    // - PQresultVerboseErrorMessage
    // PQresultErrorField
    // - PQclear

    /* FIELDS */

    // PQntuples
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
    // - PQgetvalue
    // - PQgetisnull
    // - PQgetlength
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

    public static void main(final String... args) {
        final var conninfo = "host=localhost port=5432 dbname=book user=book password=book";
        final var conn = PQconnectdb(conninfo);
        System.out.println(conn);

        final var pingres = PQping(conninfo);
        System.out.println(pingres);

        final var opts1 = PQconndefaults();
        System.out.println(opts1.length);
        System.out.println(Arrays.toString(opts1));

        final var opts2 = PQconninfo(conn);
        System.out.println(opts2.length);
        System.out.println(Arrays.toString(opts2));

        // final ByteBuffer bb = PQconndefaults(42);
        // System.out.println(bb.);
        // System.out.println(foobar(42));
    }

}
