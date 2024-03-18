package com.eischet.janitor.tools;

public class ObjectUtilities {

    public static boolean different(final boolean b1, final Boolean b2) {
        if (b2 == null) {
            return true;
        } else {
            return b1 != b2;
        }
    }

    public static String simpleClassNameOf(final Object o) {
        return o == null ? "<null>" : o.getClass().getSimpleName();
    }

}
