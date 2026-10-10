package com.eischet.janitor.toolbox.memory;

import org.jetbrains.annotations.Nullable;

/** Pre-computed, shared instances of small numbers and short strings, to conserve memory. */
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

    /**
     * Interns a short string, to conserve memory.
     * @param s the string, may be null
     * @return the interned string, or the string itself if it is too long, or null
     */
    public static @Nullable String maybeIntern(final @Nullable String s) {
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

    /**
     * Returns a shared instance for small non-negative integers.
     * @param i the number, may be null
     * @return the shared instance, or the number itself if it is out of range, or null
     */
    public static @Nullable Integer maybeIntern(final @Nullable Integer i) {
        if (i != null && i >= 0 && i < INTERNED_INTS) {
            return internedIntegers[i];
        } else {
            return i;
        }
    }

    /**
     * Returns a shared instance for small non-negative numbers.
     * @param i the number, may be null
     * @return the shared instance, or the number itself if it is out of range, or null
     */
    public static @Nullable Long maybeIntern(final @Nullable Long i) {
        if (i != null && i >= 0 && i < INTERNED_INTS) {
            return internedLongs[i.intValue()];
        } else {
            return i;
        }
    }
}
