package com.eischet.janitor.api.types.builtin;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.runtime.JanitorArgumentException;
import com.eischet.janitor.api.types.JConstant;
import com.eischet.janitor.api.types.JanitorObject;
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
     * Require a boolean value.
     * @param scriptProcess the running script
     * @param value the value to check
     * @return the value, if it's a boolean
     * @throws JanitorArgumentException if the value is not a boolean
     */
    public static JBool require(final JanitorScriptProcess scriptProcess, final JanitorObject value) throws JanitorArgumentException {
        if (value instanceof JBool ok) {
            return ok;
        }
        throw new JanitorArgumentException(scriptProcess, "Expected a boolean value, but got " + value.janitorClassName() + " instead.");
    }

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
    private JBool(final boolean value) {
        this.value = value;
    }

    /**
     * Map a java boolean to a JBool.
     * @param value the value
     * @return the JBool
     */
    public static JBool of(final boolean value) {
        return value ? TRUE : FALSE;
    }

    /**
     * Map a java boolean to a JBool.
     * @param value the value
     * @return the JBool
     * TODO: this is the same as "of" and should be removed
     */
    public static JBool map(final boolean value) {
        return value ? TRUE : FALSE;
    }

    /**
     * Map a nullable java Boolean to a JBool. Null yields FALSE.
     * @param value the value
     * @return the JBool
     */
    public static JBool of(final Boolean value) {
        return value == Boolean.TRUE ? TRUE : FALSE;
    }

    @Override
    public @NotNull
    Boolean janitorGetHostValue() {
        return value;
    }

    @Override
    public String janitorToString() {
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
