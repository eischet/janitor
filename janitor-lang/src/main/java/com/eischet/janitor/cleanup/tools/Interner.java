package com.eischet.janitor.cleanup.tools;

public class Interner {
    public static final int INTERNED_INTS = 150000;
    public static final Integer[] internedIntegers;
    public static final Long[] internedLongs;
    private static final int MAX_INTERNED_LENGTH = 100;

    static {
        internedIntegers = new Integer[INTERNED_INTS];
        for (int i = 0; i < INTERNED_INTS; i++) {
            internedIntegers[i] = i;
        }
        internedLongs = new Long[INTERNED_INTS];
        for (int i = 0; i < INTERNED_INTS; i++) {
            internedLongs[i] = (long) i;
        }
    }

    public static String maybeIntern(final String s) {
        if (s != null) {
            if (s.length() <= MAX_INTERNED_LENGTH) {
                return s.intern();
            } else {
                return s;
            }
        } else {
            return null;
        }
    }

    public static Integer maybeIntern(final Integer i) {
        if (i != null && i >= 0 && i < INTERNED_INTS) {
            return internedIntegers[i];
        } else {
            return i;
        }
    }

    public static Long maybeIntern(final Long i) {
        if (i != null && i >= 0 && i < INTERNED_INTS) {
            return internedLongs[i.intValue()];
        } else {
            return i;
        }
    }
}
