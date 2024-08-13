package com.eischet.janitor.api.types.builtin;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.runtime.JanitorArgumentException;
import com.eischet.janitor.api.types.composed.JanitorComposed;
import com.eischet.janitor.api.types.dispatch.Dispatcher;
import com.eischet.janitor.api.util.WildCardMatcher;
import com.eischet.janitor.api.types.JConstant;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.toolbox.json.api.JsonException;
import com.eischet.janitor.toolbox.json.api.JsonExportablePrimitive;
import com.eischet.janitor.toolbox.json.api.JsonOutputStream;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.Function;

/**
 * A string object, representing a string of characters.
 * This is one of the built-in types that Janitor provides automatically.
 */
public class JString extends JanitorComposed<JString> implements JConstant, JsonExportablePrimitive, JStringBase {

    /**
     * String class name.
     */
    public static final String CLASS_NAME = "string";

    private final @NotNull String wrapped;


    @Override
    public boolean janitorIsTrue() {
        return !wrapped.isEmpty();
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
    public String janitorGetHostValue() {
        return wrapped;
    }

    @Override
    public String janitorToString() {
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
}
