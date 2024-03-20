package com.eischet.janitor.cleanup.api.api.types;

import com.eischet.janitor.cleanup.api.api.errors.runtime.JanitorNameException;
import com.eischet.janitor.cleanup.tools.Interner;
import com.eischet.janitor.cleanup.tools.JStringUtilities;
import com.eischet.janitor.cleanup.json.JsonExportablePrimitive;
import com.eischet.janitor.api.json.JsonException;
import com.eischet.janitor.cleanup.json.JsonOutputStream;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class JString implements JConstant, JsonExportablePrimitive {

    private static final JStringClass myClass = new JStringClass();

    public static final JString EMPTY = new JString("");

    protected final String string;

    @Override
    public boolean janitorIsTrue() {
        return !string.isEmpty();
    }

    private JStringUtilities.WildCardMatcher wildCardMatcher;

    public JStringUtilities.WildCardMatcher getWildCardMatcher() {
        return wildCardMatcher;
    }

    public void setWildCardMatcher(final JStringUtilities.WildCardMatcher wildCardMatcher) {
        this.wildCardMatcher = wildCardMatcher;
    }

    protected JString(final String string) {
        this.string = string != null && !string.isEmpty() ? Interner.maybeIntern(string) : "";
    }

    public static JString parseLiteral(final @NotNull String literal) {
        return JString.of(JStringUtilities.unescapeJava(literal));
    }

    @Override
    public String toString() {
        return string;
    }

    public static JanitorObject ofNullable(final String string) {
        return string == null ? JNull.NULL : JString.of(string);
    }

    public static JString of(final String string) {
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
    public @Nullable JanitorObject janitorGetAttribute(final JanitorScriptProcess runningScript, final String name, final boolean required) throws JanitorNameException {
        final JanitorObject boundMethod = myClass.getBoundMethod(name, this);
        if (boundMethod != null) {
            return boundMethod;
        }
        return JConstant.super.janitorGetAttribute(runningScript, name, required);
    }


    @Override
    public @NotNull String janitorClassName() {
        return "string";
    }

    @Override
    public boolean isDefaultOrEmpty() {
        return string == null || string.isEmpty();
    }

    @Override
    public void writeJson(final JsonOutputStream producer) throws JsonException {
        producer.value(string);
    }

}
