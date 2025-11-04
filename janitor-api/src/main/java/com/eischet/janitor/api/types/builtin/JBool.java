package com.eischet.janitor.api.types.builtin;

import com.eischet.janitor.api.types.JConstant;
import com.eischet.janitor.toolbox.json.api.JsonException;
import com.eischet.janitor.toolbox.json.api.JsonExportablePrimitive;
import com.eischet.janitor.toolbox.json.api.JsonOutputStream;
import org.jetbrains.annotations.NotNull;

/**
 * A boolean object, which is probably the closest to Matthew 5:37 you can get using a computer.
 * This is one of the built-in types that Janitor provides automatically.
 * Booleans are designed to have no attributes, so there is no dispatcher and no class here.
 * Personally, I do not see any value in having stuff like "true.thing", but if someone ever presents a valid use case, that might change.
 */
public enum JBool implements JConstant, JsonExportablePrimitive {
    /**
     * Yes.
     */
    TRUE(true),

    /**
     * No.
     */
    FALSE(false);


    private final boolean value;

    /**
     * Get the boolean value.
     * @return the boolean value
     */
    @Override
    public boolean janitorIsTrue() {
        return value;
    }

    /**
     * Create a new JBool.
     * @param value the value
     */
    JBool(final boolean value) {
        this.value = value;
    }

    @Override
    public @NotNull
    Boolean janitorGetHostValue() {
        return value;
    }

    @Override
    public @NotNull String janitorToString() {
        return value ? "true" : "false";
    }

    /**
     * Get the opposite of this boolean.
     * @return the opposite
     */
    public JBool opposite() {
        return this == TRUE ? FALSE : TRUE;
    }

    @Override
    public String toString() {
        return value ? "true" : "false";
    }

    @Override
    public @NotNull String janitorClassName() {
        return "bool";
    }

    @Override
    public boolean isDefaultOrEmpty() {
        return !value;
    }

    @Override
    public void writeJson(final JsonOutputStream producer) throws JsonException {
        producer.value(value);
    }
}
