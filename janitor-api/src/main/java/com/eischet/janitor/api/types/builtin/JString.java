// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.api.types.builtin;

import com.eischet.janitor.api.JanitorEnvironment;
import com.eischet.janitor.api.RunnableScript;
import com.eischet.janitor.api.types.composed.JanitorComposed;
import com.eischet.janitor.api.types.dispatch.Dispatcher;
import com.eischet.janitor.api.util.WildCardMatcher;
import com.eischet.janitor.api.types.JConstant;
import com.eischet.janitor.toolbox.json.api.JsonException;
import com.eischet.janitor.toolbox.json.api.JsonExportablePrimitive;
import com.eischet.janitor.toolbox.json.api.JsonOutputStream;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Function;

/**
 * A string object, representing a string of characters.
 * This is one of the built-in types that Janitor provides automatically.
 */
public class JString extends JanitorComposed<JString> implements JConstant, JsonExportablePrimitive, JStringBase, Comparable<JString> {

    /**
     * String class name.
     */
    public static final String CLASS_NAME = "string";

    private final @NotNull String wrapped;


    @Override
    public boolean janitorIsTrue() {
        return !wrapped.isEmpty();
    }

    /**
     * A template compiled from a string, for the environment it was compiled in.
     *
     * @param environment the environment
     * @param script      the compiled template
     */
    public record CompiledTemplate(@NotNull JanitorEnvironment environment, @NotNull RunnableScript script) {
    }

    // What the string compiles to when it is used as a wild card pattern or a template, kept so that a string literal is
    // compiled once and not on every use. One field for both keeps every string small; a string rarely serves as both.
    private volatile Object compiled;

    /**
     * Get the wild card matcher.
     * @return the wild card matcher, or null if none is set
     */
    public WildCardMatcher getWildCardMatcher() {
        return compiled instanceof WildCardMatcher matcher ? matcher : null;
    }

    /**
     * Set the wild card matcher.
     * @param wildCardMatcher the wild card matcher
     */
    public void setWildCardMatcher(final WildCardMatcher wildCardMatcher) {
        this.compiled = wildCardMatcher;
    }

    /**
     * @param environment the environment the template runs in
     * @return the template compiled from this string for that environment, or null if there is none yet
     */
    public @Nullable RunnableScript getCompiledTemplate(final @NotNull JanitorEnvironment environment) {
        return compiled instanceof CompiledTemplate template && template.environment() == environment ? template.script() : null;
    }

    /**
     * Keeps the template compiled from this string.
     * @param template the compiled template
     */
    public void setCompiledTemplate(final @NotNull CompiledTemplate template) {
        this.compiled = template;
    }

    /**
     * Create a new JString.
     *
     * @param dispatcher method/attribute dispatch table
     * @param string     the string
     */
    protected JString(final Dispatcher<JString> dispatcher, final String string, final Function<String, String> interner) {
        super(dispatcher);
        this.wrapped =  string != null && !string.isEmpty() ? interner.apply(string) : "";
    }

    /**
     * Get the string.
     * @return the string itself
     */
    @Override
    public String toString() {
        return wrapped;
    }


    @Override
    public @NotNull String janitorGetHostValue() {
        return wrapped;
    }

    @Override
    public @NotNull String janitorToString() {
        return wrapped;
    }

    /**
     * Check if the string is empty.
     * @return true if the string is empty
     * @see String#isEmpty()
     */
    public boolean isEmpty() {
        return wrapped.isEmpty();
    }

    /**
     * Check if the string is blank.
     * @return true if the string is blank
     * @see String#isBlank()
     */
    public boolean isBlank() {
        return wrapped.isBlank();
    }

    @Override
    public @NotNull String janitorClassName() {
        return CLASS_NAME;
    }

    @Override
    public boolean isDefaultOrEmpty() {
        return wrapped.isEmpty();
    }

    @Override
    public void writeJson(final JsonOutputStream producer) throws JsonException {
        producer.value(wrapped);
    }

    /**
     * Creates a new string value.
     * @param dispatcher the dispatcher for strings
     * @param value the string to wrap
     * @param interner a function that interns strings, to conserve memory
     * @return the new value
     */
    public static JString newInstance(final Dispatcher<JString> dispatcher, final String value, final Function<String, String> interner) {
        return new JString(dispatcher, value, interner);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final JString jString = (JString) o;
        return Objects.equals(wrapped, jString.wrapped);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(wrapped);
    }

    @Override
    public int compareTo(@NotNull final JString o) {
        return CharSequence.compare(wrapped, o.wrapped);
    }

}
