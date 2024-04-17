package com.eischet.janitor.api.util;

public class ShortStringInterner {
    private static final int MAX_INTERNED_LENGTH = 10;

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
