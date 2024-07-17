package com.eischet.janitor.api.util;

/**
 * Helper class to intern short strings.
 */
public class ShortStringInterner {
    private static final int MAX_INTERNED_LENGTH = 10;

    /**
     * Interns a string if it is short enough.
     * @param s the string to intern
     * @return the interned string, and/or the original string if it is too long
     */
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

}
