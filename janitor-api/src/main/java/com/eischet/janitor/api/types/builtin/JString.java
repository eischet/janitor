package com.eischet.janitor.api.types.builtin;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.runtime.JanitorArgumentException;
import com.eischet.janitor.api.types.dispatch.Dispatcher;
import com.eischet.janitor.api.types.wrapped.JanitorWrapper;
import com.eischet.janitor.api.util.strings.WildCardMatcher;
import com.eischet.janitor.api.types.JConstant;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.api.util.ShortStringInterner;
import com.eischet.janitor.toolbox.json.api.JsonException;
import com.eischet.janitor.toolbox.json.api.JsonExportablePrimitive;
import com.eischet.janitor.toolbox.json.api.JsonOutputStream;
import org.jetbrains.annotations.NotNull;

/**
 * A string object, representing a string of characters.
 * This is one of the built-in types that Janitor provides automatically.
 */
public class JString extends JanitorWrapper<String> implements JConstant, JsonExportablePrimitive {


    /**
     * String class name.
     */
    public static final String CLASS_NAME = "string";


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
    protected JString(final Dispatcher<JanitorWrapper<String>> dispatcher, final String string) {
        super(dispatcher, string != null && !string.isEmpty() ? ShortStringInterner.maybeIntern(string) : "");
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

    public static JString newInstance(final Dispatcher<JanitorWrapper<String>> dispatcher, final String value) {
        return new JString(dispatcher, value);
    }

}
