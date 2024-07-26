package com.eischet.janitor.api.scripting;

import org.jetbrains.annotations.Nullable;

public interface DispatchCaster<T extends JanitorWrapper<?>> {

    /**
     * Check whether o is an instance of T.
     *
     * @param o any object
     * @return true if o is an instance of T, else false
     */
    boolean isInstance(final Object o);

    /**
     * Cast o to T. If that is not possible, return null.
     * @param o any object
     * @return o cast to T, or null if that is not possible
     */
    @Nullable
    T cast(final Object o);

}
