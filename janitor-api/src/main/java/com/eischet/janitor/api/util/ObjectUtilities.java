package com.eischet.janitor.api.util;

/**
 * Utility methods for objects.
 */
public class ObjectUtilities {

    /**
     * Returns true if the two parameters are different.
     * @param b1 a boolean
     * @param b2 a Boolean
     * @return true if the two parameters are different
     * TODO: this is used in some obscure client code, and should be removed from the API
     */
    public static boolean different(final boolean b1, final Boolean b2) {
        if (b2 == null) {
            return true;
        } else {
            return b1 != b2;
        }
    }

    /**
     * Returns the simple class name of the object or "&lt;null&gt;" if the object is null.
     * @param o an object
     * @return the simple class name of the object or "&lt;null&gt;" if the object is null
     * TODO: should be moved to the implementation, as it's not really useful for API consumers I think
     */
    public static String simpleClassNameOf(final Object o) {
        return o == null ? "<null>" : o.getClass().getSimpleName();
    }

}
