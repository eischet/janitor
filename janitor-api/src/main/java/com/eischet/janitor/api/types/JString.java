package com.eischet.janitor.api.types;

import com.eischet.janitor.api.json.api.JsonException;
import com.eischet.janitor.api.json.api.JsonExportablePrimitive;
import com.eischet.janitor.api.json.api.JsonOutputStream;
import com.eischet.janitor.api.strings.WildCardMatcher;
import com.eischet.janitor.api.traits.JConstant;
import com.eischet.janitor.api.util.ShortStringInterner;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class JString implements JConstant, JsonExportablePrimitive {

    public static final String CLASS_NAME = "string";
    public static final JString EMPTY = new JString("");

    protected final String string;

    @Override
    public boolean janitorIsTrue() {
        return !string.isEmpty();
    }

    private WildCardMatcher wildCardMatcher;

    public WildCardMatcher getWildCardMatcher() {
        return wildCardMatcher;
    }

    public void setWildCardMatcher(final WildCardMatcher wildCardMatcher) {
        this.wildCardMatcher = wildCardMatcher;
    }

    protected JString(final String string) {
        this.string = string != null && !string.isEmpty() ? ShortStringInterner.maybeIntern(string) : "";
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

}
