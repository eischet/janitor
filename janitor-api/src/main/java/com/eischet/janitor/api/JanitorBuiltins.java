package com.eischet.janitor.api;

import com.eischet.janitor.api.types.JMap;
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
    @NotNull JString emptyString();

    /**
     * Return a new String object.
     * @param value a Java String, which may be null
     * @return a JString from the Java String, or the empty string when null
     */
    @NotNull JString string(final @Nullable String value);

    /**
     * Return a new String object, or NULL if the value is null.
     * "nullable" refers to our scripting NULL, not to Java null.
     * @param value a Java String or null
     * @return a JString from the Java String, or NULL
     */
    @NotNull JanitorObject nullableString(final @Nullable String value);

    @NotNull JMap map();


}
