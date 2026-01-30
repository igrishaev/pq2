package org.pq;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.util.Arrays;

public class Lib {

    final static MethodHandle
            _PQconnectdb,
            _PQlibVersion,
            _PQexec,
            _PQntuples,
            _PQfname,
            _PQgetvalue,
            _PQexecParams,
            _PQgetisnull,
            _PQcmdStatus,
            _PQerrorMessage,
            _PQclear;

    final static int CONNECTION_OK;

    final static ValueLayout PTR = ValueLayout.ADDRESS;
    final static ValueLayout INT = ValueLayout.JAVA_INT;

    final static MemorySegment NULL = MemorySegment.NULL;

    final static Linker linker;
    final static SymbolLookup lookup;

    static MethodHandle getMethod(final String methodName, final ValueLayout result, final ValueLayout... args) {
        final MemorySegment PQconnectdb_ms = lookup.find(methodName).orElseThrow();
        final FunctionDescriptor PQconnectdb_fd = FunctionDescriptor.of(result, args);
        return linker.downcallHandle(PQconnectdb_ms, PQconnectdb_fd);
    }

    static int getIntConstant(final String constName) {
        final var seg = lookup.find("CONNECTION_OK").orElseThrow();
        return seg.get(ValueLayout.JAVA_INT, 0);
    }

    static {
        System.load("/opt/homebrew/Cellar/libpq/18.1/lib/libpq.dylib");

        linker = Linker.nativeLinker();
        lookup = SymbolLookup.loaderLookup();

        _PQconnectdb = getMethod("PQconnectdb", PTR, PTR);
        _PQlibVersion = getMethod("PQlibVersion", INT, PTR);
        _PQexec = getMethod("PQexec", PTR, PTR, PTR);
        _PQntuples = getMethod("PQntuples", INT, PTR);
        _PQfname = getMethod("PQfname", PTR, PTR, INT);
        _PQclear = getMethod("PQclear", PTR, PTR);
        _PQgetvalue = getMethod("PQgetvalue", PTR, PTR, INT, INT);
        _PQexecParams = getMethod("PQexecParams", PTR, PTR, PTR, INT, PTR, PTR, PTR, PTR, INT);
        _PQgetisnull = getMethod("PQgetisnull", INT, PTR, INT, INT);
        _PQcmdStatus = getMethod("PQcmdStatus", PTR, PTR);
        _PQerrorMessage = getMethod("PQerrorMessage", PTR, PTR);

        CONNECTION_OK = getIntConstant("CONNECTION_OK");

    }

    public static Object _call(final MethodHandle method, final Object... args) {
        try {
            return method.invokeWithArguments(args);
        } catch (Throwable e) {
            throw new RuntimeException(
                    String.format("cannot run foreign method: %s, args: %s", method, Arrays.toString(args)),
                    e
            );
        }
    }

    public static int readIntBin(final MemorySegment memSeg) {
        return memSeg.reinterpret(4).asByteBuffer().getInt();
    }

    public static String readCharPtr(final MemorySegment memSeg) {
        System.out.println(memSeg.toString());
        if (memSeg.address() == 0) {
            return null;
        } else {
            return memSeg.reinterpret(Integer.MAX_VALUE).getString(0);
        }
    }

    public static MemorySegment PQconnectdb(final String connString) {
        final Arena arena = Arena.ofAuto();
        var memSeg = arena.allocate(300);
        memSeg.setString(0, connString);
        return (MemorySegment) _call(_PQconnectdb, memSeg);
    }

    public static int PQlibVersion(final Object conn) {
        return (int) _call(_PQlibVersion, conn);
    }

    public static MemorySegment PQexec(final Object conn, final MemorySegment segSql) {
        return (MemorySegment) _call(_PQexec, conn, segSql);
    }

    public static int PQntuples(final Object conn) {
        return (int) _call(_PQntuples, conn);
    }

    public static String PQfname(final Object conn, final int col) {
        final var seg = (MemorySegment) _call(_PQfname, conn, col);
        return readCharPtr(seg);
    }

    public static void PQclear(final Object result) {
        _call(_PQclear, result);
    }

    public static Object PQgetvalue(final Object result, final int row, final int col) {
        final var val = (MemorySegment) _call(_PQgetvalue, result, row, col);
        return readCharPtr(val);
    }

    public static int PQgetisnull(final Object result, final int row, final int col) {
        return (int) _call(_PQgetisnull, result, row, col);
    }

    public static MemorySegment PQexecParams(final Object conn, final String sqlSting) {
        final var arena = Arena.ofAuto();
        final var seg = arena.allocate(300);
        seg.setString(0, sqlSting);
        return (MemorySegment) _call(_PQexecParams, conn, seg, 0, NULL, NULL, NULL, NULL, 1);
    }

    public static boolean isNULL(final MemorySegment segment) {
        return segment.equals(MemorySegment.NULL);
    }

    public static String PQerrorMessage(final MemorySegment conn) {
        final var seg = (MemorySegment) _call(_PQerrorMessage, conn);
        return readCharPtr(seg);
    }

    public static String PQcmdStatus(final MemorySegment result) {
        final var val = (MemorySegment) _call(_PQcmdStatus, result);
        return readCharPtr(val);
    }

    public static void main(String... args) {
        var conn = PQconnectdb("host=localhost port=15432 dbname=test user=test password=test");
        var version = PQlibVersion(conn);

        System.out.println(conn);
        System.out.println(version);

        final Arena arena = Arena.ofAuto();
        var segSql = arena.allocate(300);
        segSql.setString(0, "select 1 fs f as number");

        var res = PQexec(conn, segSql);
        System.out.println(PQcmdStatus(res));

        System.out.println(PQerrorMessage(conn));

        var result = PQexecParams(conn, "select null::text as num");
        var tuples = PQntuples(result);
        var column = PQfname(result, 0);
        var value = PQgetvalue(result, 0, 0);
        var isNull = PQgetisnull(result, 0, 0);

        PQclear(result);

        System.out.println(tuples);
        System.out.println(column);
        System.out.println(value);
        System.out.println(isNull);


    }


}
