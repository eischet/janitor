package com.eischet.janitor.api;

import com.eischet.janitor.api.types.JString;
import com.eischet.janitor.api.types.JanitorObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Interface / factory for working with built-in types.
 */
public interface JanitorBuiltins {

    /**
     * Return an empty String.
     * @return an empty string
     */
    JString emptyString();

    /**
     * Return a new String object.
     * @param value a Java String
     * @return a JString from the Java String
     */
    JString string(final @NotNull String value);

    /**
     * Return a new String object, or NULL if the value is null.
     * @param value a Java String or null
     * @return a JString from the Java String, or NULL
     */
    JanitorObject nullableString(final @Nullable String value);

}
