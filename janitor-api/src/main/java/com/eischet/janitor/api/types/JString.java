package com.eischet.janitor.api.types;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.runtime.JanitorArgumentException;
import com.eischet.janitor.api.strings.WildCardMatcher;
import com.eischet.janitor.api.traits.JConstant;
import com.eischet.janitor.api.util.ShortStringInterner;
import com.eischet.janitor.toolbox.json.api.JsonException;
import com.eischet.janitor.toolbox.json.api.JsonExportablePrimitive;
import com.eischet.janitor.toolbox.json.api.JsonOutputStream;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * A string object, representing a string of characters.
 * This is one of the built-in types that Janitor provides automatically.
 */
public class JString  implements JConstant, JsonExportablePrimitive {


    /**
     * String class name.
     */
    public static final String CLASS_NAME = "string";

    /**
     * Empty string constant.
     */
    public static final JString EMPTY = new JString("");

    protected final String string;

    @Override
    public boolean janitorIsTrue() {
        return !string.isEmpty();
    }

    private WildCardMatcher wildCardMatcher;

    /**
     * Get the wild card matcher.
     * @return the wild card matcher, or null if none is set
     */
    public WildCardMatcher getWildCardMatcher() {
        return wildCardMatcher;
    }

    /**
     * Set the wild card matcher.
     * @param wildCardMatcher the wild card matcher
     */
    public void setWildCardMatcher(final WildCardMatcher wildCardMatcher) {
        this.wildCardMatcher = wildCardMatcher;
    }

    /**
     * Create a new JString.
     * @param string the string
     */
    protected JString(final String string) {
        this.string = string != null && !string.isEmpty() ? ShortStringInterner.maybeIntern(string) : "";
    }

    /**
     * Get the string.
     * @return the string itself
     */
    @Override
    public String toString() {
        return string;
    }

    /**
     * Create a new JString.
     * @param string the string
     * @return the string, or NULL if the input is null
     */
    public static JanitorObject ofNullable(/* TODO: final JanitorEnvironment env, */final @Nullable String string) {
        return string == null ? JNull.NULL : JString.of(string);
    }

    /**
     * Create a new JString.
     * @param string the string
     * @return the string
     */
    public static JString of(final @Nullable String string) {
        if (string == null || string.isEmpty()) {
            return EMPTY;
        }
        return new JString(string);
    }

    @Override
    public String janitorGetHostValue() {
        return string;
    }

    @Override
    public String janitorToString() {
        return string;
    }

    /**
     * Check if the string is empty.
     * @return true if the string is empty
     * @see String#isEmpty()
     */
    public boolean isEmpty() {
        return string.isEmpty();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final JString that = (JString) o;
        return Objects.equals(string, that.string);
    }

    @Override
    public int hashCode() {
        return Objects.hash(string);
    }

    @Override
    public @NotNull String janitorClassName() {
        return CLASS_NAME;
    }

    @Override
    public boolean isDefaultOrEmpty() {
        return string == null || string.isEmpty();
    }

    @Override
    public void writeJson(final JsonOutputStream producer) throws JsonException {
        producer.value(string);
    }

    /**
     * Require a value to be string.
     * @param scriptProcess the script process
     * @param value the value
     * @return the value as string
     * @throws JanitorArgumentException if the value is not a string
     */
    public static JString require(final JanitorScriptProcess scriptProcess, final JanitorObject value) throws JanitorArgumentException {
        if (value instanceof JString ok) {
            return ok;
        }
        throw new JanitorArgumentException(scriptProcess, "Expected a string value, but got " + value.janitorClassName() + " instead.");
    }

}
