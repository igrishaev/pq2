package org.pq.tool;

import java.util.UUID;

public class Cast {

    private static RuntimeException error(final String template, Object... args) {
        return new RuntimeException(String.format(template, args));
    }

    public static short castShort(final Integer i) {
        if ((Short.MIN_VALUE <= i) && (i <= Short.MAX_VALUE)) {
            return i.shortValue();
        } else {
            throw error("cannot cast integer %s to short", i);
        }
    }

    public static short castShort(final Long l) {
        if ((Short.MIN_VALUE <= l) && (l <= Short.MAX_VALUE)) {
            return l.shortValue();
        } else {
            throw error("cannot cast long %s to short", l);
        }
    }

    public static short castShort(final Object x) {
        if (x instanceof Short s) {
            return s;
        } else if (x instanceof Integer i) {
            return castShort(i);
        } else if (x instanceof Long l) {
            return castShort(l);
        } else {
            throw error("cannot cast object %s to short", x);
        }
    }

    public static int castInteger(final Long l) {
        if ((Integer.MIN_VALUE <= l) && (l <= Integer.MAX_VALUE)) {
            return l.intValue();
        } else {
            throw error("cannot cast long %s to short", l);
        }
    }

    public static int castInteger(final Object x) {
        if (x instanceof Short s) {
            return s.intValue();
        } else if (x instanceof Integer i) {
            return i;
        } else if (x instanceof Long l) {
            return castInteger(l);
        } else {
            throw error("cannot cast object %s to integer", x);
        }
    }

    public static long castLong(final Float f) {
        if ((f % 1 == 0) && (Long.MIN_VALUE <= f) && (f <= Long.MAX_VALUE)) {
            return f.longValue();
        } else {
            throw error("cannot cast float %s to long", f);
        }
    }

    public static long castLong(final Double d) {
        if ((d % 1 == 0) && (Long.MIN_VALUE <= d) && (d <= Long.MAX_VALUE)) {
            return d.longValue();
        } else {
            throw error("cannot cast double %s to long", d);
        }
    }

    public static long castLong(final Object x) {
        if (x instanceof Short s) {
            return s.longValue();
        } else if (x instanceof Integer i) {
            return i.longValue();
        } else if (x instanceof Long l) {
            return l;
        } else if (x instanceof Float f) {
            return castLong(f);
        } else if (x instanceof Double d) {
            return castLong(d);
        } else {
            throw error("cannot cast object %s to long", x);
        }
    }

    public static float castFloat(final Double d) {
        if ((Float.MIN_VALUE <= d) && (d <= Float.MAX_VALUE)) {
            return d.floatValue();
        } else {
            throw error("cannot cast double %s to float", d);
        }
    }

    public static float castFloat(final Long l) {
        if (Float.MIN_VALUE <= l) {
            return l.floatValue();
        } else {
            throw error("cannot cast long %s to float", l);
        }
    }

    public static float castFloat(final Object x) {
        if (x instanceof Float f) {
            return f;
        } else if (x instanceof Short s) {
            return s.floatValue();
        } else if (x instanceof Integer i) {
            return i.floatValue();
        } else if (x instanceof Double d) {
            return castFloat(d);
        } else if (x instanceof Long l) {
            return castFloat(l);
        } else {
            throw error("cannot cast object %s to float", x);
        }
    }

    public static double castDouble(final Object x) {
        if (x instanceof Float f) {
            return f.doubleValue();
        } else if (x instanceof Double d) {
            return d;
        } else if (x instanceof Short s) {
            return s.doubleValue();
        } else if (x instanceof Integer i) {
            return i.doubleValue();
        } else if (x instanceof Long l) {
            return l.doubleValue();
        } else {
            throw error("cannot cast object %s to double", x);
        }
    }

    public static UUID castUUID(final Object x) {
        if (x instanceof UUID uuid) {
            return uuid;
        } else if (x instanceof String s) {
            return UUID.fromString(s);
        } else {
            throw error("cannot cast object %s to UUID", x);
        }
    }
}
