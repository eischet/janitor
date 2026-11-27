// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.api.types.functions;

import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.runtime.JanitorArgumentException;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.api.types.builtin.JBool;
import com.eischet.janitor.api.types.builtin.JDuration;
import com.eischet.janitor.api.types.builtin.JList;
import com.eischet.janitor.api.types.builtin.JMap;
import com.eischet.janitor.api.types.builtin.JNull;
import com.eischet.janitor.api.types.builtin.JString;
import com.eischet.janitor.api.types.dispatch.Conversions;
import com.eischet.janitor.api.util.ObjectUtilities;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The arguments of a call, bound to the parameters of a {@link Signature} and read by parameter name. A parameter that the
 * caller left out has its default value. Asking for a name that the signature does not declare is a programming error
 * ({@link IllegalArgumentException}); a value of the wrong type is the caller's ({@link JanitorArgumentException}).
 */
public final class SignatureArguments {

    private final Signature signature;
    private final JanitorScriptProcess process;
    private final String functionName;
    private final JanitorObject[] values;

    SignatureArguments(final Signature signature, final JanitorScriptProcess process, final String functionName, final JanitorObject[] values) {
        this.signature = signature;
        this.process = process;
        this.functionName = functionName;
        this.values = values;
    }

    /**
     * @param name the name of a parameter
     * @return true if the caller gave a value for it, by position or by name
     */
    public boolean has(final @NotNull String name) {
        return values[signature.indexOf(name)] != null;
    }

    /**
     * @param name the name of a parameter
     * @return its value, or its default value; {@link JNull#NULL} for null
     */
    public @NotNull JanitorObject get(final @NotNull String name) {
        final int index = signature.indexOf(name);
        final JanitorObject value = values[index];
        if (value != null) {
            return value;
        }
        final Object defaultValue = signature.defaultValue(index);
        return switch (defaultValue) {
            case null -> JNull.NULL;
            case String text -> process.getBuiltins().string(text);
            case Long number -> process.getBuiltins().integer(number);
            case Double number -> process.getBuiltins().floatingPoint(number);
            case Boolean flag -> Janitor.toBool(flag);
            default -> throw new IllegalStateException("unsupported default value: " + defaultValue);
        };
    }

    /**
     * @param name the name of a parameter
     * @return its value as a string, or null; values that are not strings are converted where Janitor can, e.g. numbers
     * @throws JanitorArgumentException if the value cannot be a string
     */
    public @Nullable String string(final @NotNull String name) throws JanitorArgumentException {
        final JanitorObject value = get(name);
        if (value == JNull.NULL) {
            return null;
        }
        final JString text = value.janitorCoerce(JString.class);
        if (text == null) {
            throw wrongType(name, "a string", value);
        }
        return text.janitorGetHostValue();
    }

    /**
     * @param name the name of a parameter
     * @return its value as a string
     * @throws JanitorArgumentException if the value is null or cannot be a string
     */
    public @NotNull String requiredString(final @NotNull String name) throws JanitorArgumentException {
        final String text = string(name);
        if (text == null) {
            throw wrongType(name, "a string", JNull.NULL);
        }
        return text;
    }

    /**
     * @param name the name of a parameter
     * @return its value as an integer; a float is taken if it is a whole number, e.g. {@code 5.0}
     * @throws JanitorArgumentException if the value is not a whole number
     */
    public long integer(final @NotNull String name) throws JanitorArgumentException {
        final JanitorObject value = get(name);
        final Long wholeNumber = Conversions.toWholeNumberOrNull(value);
        if (wholeNumber != null) {
            return wholeNumber;
        }
        throw wrongType(name, "an integer", value);
    }

    /**
     * @param name the name of a parameter
     * @return its value as a boolean
     * @throws JanitorArgumentException if the value is not a boolean
     */
    public boolean bool(final @NotNull String name) throws JanitorArgumentException {
        final JanitorObject value = get(name);
        if (value instanceof JBool flag) {
            return flag.janitorIsTrue();
        }
        throw wrongType(name, "a boolean", value);
    }

    /**
     * @param name the name of a parameter
     * @return its value as a number of seconds, written either as a duration like {@code @90s} or as a whole number
     * @throws JanitorArgumentException if the value is neither
     */
    public long seconds(final @NotNull String name) throws JanitorArgumentException {
        final JanitorObject value = get(name);
        if (value instanceof JDuration duration) {
            return duration.toSeconds();
        }
        final Long wholeNumber = Conversions.toWholeNumberOrNull(value);
        if (wholeNumber != null) {
            return wholeNumber;
        }
        throw wrongType(name, "a duration or a number of seconds", value);
    }

    /**
     * @param name the name of a parameter
     * @param cls  the class its value must have
     * @param <T>  that class
     * @return the value, or null if it is null
     * @throws JanitorArgumentException if the value has another class
     */
    public <T extends JanitorObject> @Nullable T object(final @NotNull String name, final @NotNull Class<T> cls) throws JanitorArgumentException {
        final JanitorObject value = get(name);
        if (value == JNull.NULL) {
            return null;
        }
        if (cls.isInstance(value)) {
            return cls.cast(value);
        }
        throw wrongType(name, "of type " + cls.getSimpleName(), value);
    }

    /**
     * @param name the name of a parameter
     * @param cls  the class its value must have
     * @param <T>  that class
     * @return the value
     * @throws JanitorArgumentException if the value is null or has another class
     */
    public <T extends JanitorObject> @NotNull T required(final @NotNull String name, final @NotNull Class<T> cls) throws JanitorArgumentException {
        final T value = object(name, cls);
        if (value == null) {
            throw wrongType(name, "of type " + cls.getSimpleName(), JNull.NULL);
        }
        return value;
    }

    /**
     * @param name the name of the {@code *rest} parameter
     * @return the positional arguments it collected, possibly none
     */
    public @NotNull JList rest(final @NotNull String name) {
        return (JList) values[signature.indexOf(name)];
    }

    /**
     * @param name the name of the {@code **options} parameter
     * @return the named arguments it collected, possibly none
     */
    public @NotNull JMap options(final @NotNull String name) {
        return (JMap) values[signature.indexOf(name)];
    }

    private JanitorArgumentException wrongType(final String name, final String expected, final JanitorObject value) {
        return new JanitorArgumentException(process, "%s: argument '%s' must be %s, but is: %s (%s)".formatted(
                functionName, name, expected, value.janitorToString(), ObjectUtilities.simpleClassNameOf(value)));
    }
}
