// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.api.types.functions;


import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.runtime.JanitorArgumentException;
import com.eischet.janitor.api.errors.runtime.JanitorNameException;
import com.eischet.janitor.api.errors.runtime.JanitorNativeException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.api.types.builtin.*;
import com.eischet.janitor.api.types.dispatch.Conversions;
import com.eischet.janitor.api.util.ObjectUtilities;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * A class that represents the arguments passed to a function call.
 */
public class JCallArgs {

    private final String functionName;
    private final @NotNull List<EvaluatedArgument> args;
    private final @NotNull List<EvaluatedArgument> positional;
    private final @NotNull List<EvaluatedArgument> named;
    private final @NotNull JanitorScriptProcess process;
    /** Names of the named arguments that the called function has read; shared with the copies made by {@link #shift()}. */
    private final @Nullable Set<String> usedNames;

    /**
     * Create a new call arguments object.
     *
     * @param functionName  the name of the function being called.
     * @param process the script that is running.
     * @param args          the arguments to the function.
     */
    public JCallArgs(final String functionName, final @NotNull JanitorScriptProcess process, final @Nullable List<JanitorObject> args) {
        this.functionName = functionName;
        this.process = process;
        this.args = args == null ? Collections.emptyList() : args.stream().map(value -> new EvaluatedArgument(null, value)).toList();
        this.positional = this.args;
        this.named = Collections.emptyList();
        this.usedNames = null;
    }

    /**
     * Create a new call arguments object from evaluated arguments, which may be positional or named.
     *
     * @param process            the script that is running.
     * @param identifier         the name of the function being called.
     * @param evaluatedArguments the arguments: positional ones (without a name) and named ones.
     */
    public JCallArgs(final @NotNull JanitorScriptProcess process, final String identifier, final @NotNull List<EvaluatedArgument> evaluatedArguments) {
        this(process, identifier, evaluatedArguments, null);
    }

    private JCallArgs(final @NotNull JanitorScriptProcess process, final String identifier, final @NotNull List<EvaluatedArgument> evaluatedArguments, final @Nullable Set<String> sharedUsedNames) {
        this.functionName = identifier;
        this.process = process;
        this.args = evaluatedArguments;
        boolean anyNamed = false;
        for (final EvaluatedArgument argument : evaluatedArguments) {
            if (argument.getName() != null) {
                anyNamed = true;
                break;
            }
        }
        if (anyNamed) {
            this.positional = evaluatedArguments.stream().filter(argument -> argument.getName() == null).toList();
            this.named = evaluatedArguments.stream().filter(argument -> argument.getName() != null).toList();
            this.usedNames = sharedUsedNames != null ? sharedUsedNames : new HashSet<>();
        } else {
            this.positional = evaluatedArguments;
            this.named = Collections.emptyList();
            this.usedNames = sharedUsedNames;
        }
    }

    /**
     * Create an empty call arguments object.
     *
     * @param functionName  the name of the function being called.
     * @param process the script that is running.
     * @return the empty call arguments object.
     */
    public static JCallArgs empty(final String functionName, final JanitorScriptProcess process) {
        return new JCallArgs(functionName, process, null);
    }

    /**
     * Creates call arguments for a single argument.
     * @param process the running script process
     * @param arg the argument
     * @return the call arguments
     */
    public static @NotNull JCallArgs ofSingleArgument(final JanitorScriptProcess process, final JanitorObject arg) {
        return new JCallArgs(process, "anonymous", List.of(new EvaluatedArgument(null, arg)));
    }

    /**
     * Get the name of the function being called.
     *
     * @return the name of the function being called.
     */
    public String getFunctionName() {
        return functionName;
    }

    /**
     * Get the number of positional arguments in the call; named arguments do not count.
     *
     * @return the number of positional arguments in the call.
     */
    public int size() {
        return positional.size();
    }

    /**
     * @return true if the call has named arguments ({@code name = value})
     */
    public boolean hasNamed() {
        return !named.isEmpty();
    }

    /**
     * Returns the named arguments in the order in which they were written, and counts them all as used, see
     * {@link #requireNamesUsed()}. This is for functions that bind all names themselves.
     *
     * @return the named arguments
     */
    public @NotNull List<EvaluatedArgument> takeNamed() {
        if (usedNames != null) {
            for (final EvaluatedArgument argument : named) {
                usedNames.add(argument.getName());
            }
        }
        return named;
    }

    /**
     * Requires that the called function has read every named argument, by {@link #getByName}, {@link #asKwargs} or
     * {@link #takeNamed()}. Script calls check this after the function returns, so that a name the function does not
     * know fails instead of being ignored.
     *
     * @throws JanitorArgumentException if a named argument was not read
     */
    public void requireNamesUsed() throws JanitorArgumentException {
        for (final EvaluatedArgument argument : named) {
            if (usedNames == null || !usedNames.contains(argument.getName())) {
                throw new JanitorArgumentException(process, "%s: unexpected named argument '%s'".formatted(functionName, argument.getName()));
            }
        }
    }

    /**
     * Require that the number of arguments in the call is between minSize and maxSize.
     *
     * @param minSize the minimum number of arguments allowed.
     * @param maxSize the maximum number of arguments allowed.
     * @return this object.
     * @throws JanitorRuntimeException if the requirement is not met
     */
    public JCallArgs require(final int minSize, final int maxSize) throws JanitorRuntimeException {
        if (size() < minSize || size() > maxSize) {
            throw new JanitorArgumentException(process, "%s: requires between %s and %s arguments, but got: %s".formatted(functionName, minSize, maxSize, args));
        }
        return this;
    }

    /**
     * Requires a minimum number of arguments.
     * @param minSize the minimum number of arguments
     * @return this object, for chaining
     * @throws JanitorRuntimeException if fewer arguments were passed
     */
    public JCallArgs requireAtLeast(final int minSize) throws JanitorRuntimeException {
        if (size() < minSize) {
            throw new JanitorArgumentException(process, "%s: requires at least %s arguments, but got: %s".formatted(functionName, minSize, args));
        }
        return this;
    }

    /**
     * Require that the number of arguments in the call is exactly size.
     *
     * @param size the exact number of arguments required.
     * @return this object.
     * @throws JanitorRuntimeException if the requirement is not met
     */
    public JCallArgs require(final int size) throws JanitorRuntimeException {
        if (size() != size) {
            throw new JanitorArgumentException(process, "%s: requires exactly %s arguments, but got: %s".formatted(functionName, size, args));
        }
        return this;
    }

    /**
     * Get the positional argument at the given position; named arguments do not count.
     *
     * @param position the position of the argument to get.
     * @return the argument at the given position.
     * @throws IndexOutOfBoundsException if the position is out of bounds.
     */
    public JanitorObject get(final int position) throws IndexOutOfBoundsException {
        return positional.get(position).getValue().janitorUnpack(); // unpacking is super-important, so that we store the actual value instead of a property reference, for example (!)
    }

    /**
     * Get a list of strings starting at the given position.
     * Like "varargs". This is for calls like print(a,b,c), not print([a,b,c])!
     *
     * @param position starting position
     * @return a list of strings
     * @throws JanitorRuntimeException on runtime errors
     */
    public List<String> getStringList(int position) throws JanitorRuntimeException {
        final List<String> strings = new ArrayList<>();
        String next = getOptionalStringValue(position, null);
        while (next != null) {
            strings.add(next);
            next = getOptionalStringValue(++position, null);
        }
        return strings;
    }

    /**
     * Get a required string value at the given position.
     *
     * @param i the position of the argument to get.
     * @return the string value at the given position.
     * @throws JanitorRuntimeException if the argument is not a string value.
     */
    public String getRequiredStringValue(final int i) throws JanitorRuntimeException {
        final JanitorObject arg = get(i);
        if (arg instanceof JString jstr) {
            return jstr.janitorGetHostValue();
        }
        throw new JanitorArgumentException(process, "%s: argument %s must be a string value, but the caller provided: %s".formatted(functionName, i, args));
    }

    /**
     * Gets a required numeric argument.
     * @param i the index of the argument
     * @return the number
     * @throws JanitorArgumentException if the argument is missing or not a number
     */
    public JNumber getRequiredJNumber(final int i) throws JanitorArgumentException {
        final JanitorObject arg = get(i);
        if (arg instanceof JNumber num) {
            return num;
        }
        throw new JanitorArgumentException(process, "%s: argument %s must be a numeric value, but the caller provided: %s".formatted(functionName, i, args));
    }

    /**
     * Gets a required integer argument. A float is taken if it is a whole number, e.g. {@code 5.0}.
     * @param i the index of the argument
     * @return the value as a Java int
     * @throws JanitorArgumentException if the argument is missing, not a whole number, or too big for an int
     */
    public int getRequiredIntValue(final int i) throws JanitorArgumentException {
        final long value = getRequiredLongValue(i);
        if (value > Integer.MAX_VALUE || value < Integer.MIN_VALUE) {
            throw new JanitorArgumentException(process, "%s: argument %s must be an integer value, but the value that was provided is too big for an integer: %s".formatted(functionName, i, args));
        }
        return (int) value;
    }

    /**
     * Gets a required integer argument as a long. A float is taken if it is a whole number, e.g. {@code 5.0}; a float with a
     * fraction is refused instead of being cut off.
     * @param i the index of the argument
     * @return the value as a Java long
     * @throws JanitorArgumentException if the argument is missing or not a whole number
     */
    public long getRequiredLongValue(final int i) throws JanitorArgumentException {
        final Long value = Conversions.toWholeNumberOrNull(get(i));
        if (value == null) {
            throw new JanitorArgumentException(process, "%s: argument %s must be a whole number, but the caller provided: %s".formatted(functionName, i, args));
        }
        return value;
    }

    /**
     * Get a required boolean value at the given position.
     *
     * @param i the position of the argument to get.
     * @return the boolean value at the given position.
     * @throws JanitorRuntimeException if the argument is not a boolean value.
     */
    public boolean getRequiredBooleanValue(final int i) throws JanitorRuntimeException {
        final JanitorObject arg = get(i);
        if (arg instanceof JBool jbool) {
            return jbool.janitorIsTrue();
        }
        throw new JanitorArgumentException(process, "%s: argument %s must be a boolean value, but the caller provided: %s".formatted(functionName, i, args));
    }

    /**
     * Get an optional string value at the given position.
     *
     * @param i            the position of the argument to get.
     * @param defaultValue the default value to return if the argument is not present.
     * @return the string value at the given position, or the default value if the argument is not present.
     * @throws JanitorRuntimeException on inner runtime errors
     */
    public String getOptionalStringValue(final int i, final String defaultValue) throws JanitorRuntimeException {
        if (i < size()) {
            return getString(i).janitorGetHostValue();
        }
        return defaultValue;
    }

    /**
     * Get an instance of the given class at the given position, or null if the argument is not present.
     *
     * @param position the position of the argument to get.
     * @param cls      the class of the argument to get.
     * @param <T>      the type of the argument to get.
     * @return the argument at the given position, or null if the argument is not present.
     * @throws JanitorRuntimeException on inner runtime errors, or when the arg cannot be cast to T
     */
    public <T extends JanitorObject> T getNullable(final int position, final Class<T> cls) throws JanitorRuntimeException {
        if (position >= size()) {
            return null;
        }
        final JanitorObject obj = get(position);
        if (obj == null || obj == JNull.NULL) {
            return null;
        } else if (cls.isAssignableFrom(obj.getClass())) {
            try {
                return cls.cast(obj);
            } catch (ClassCastException e) {
                throw new JanitorNativeException(process, "%s: error casting required argument #%s to type %s".formatted(functionName, position, cls.getSimpleName()), e);
            }
        } else {
            throw new JanitorArgumentException(process, "%s: argument #%s is required to be of type %s, but it is of type %s".formatted(functionName, position, cls.getSimpleName(), ObjectUtilities.simpleClassNameOf(obj)));
        }
    }

    /**
     * Get an instance of the given class at the given position.
     *
     * @param position the position of the argument to get.
     * @param cls      the class of the argument to get.
     * @param <T>      the type of the argument to get.
     * @return the argument at the given position.
     * @throws JanitorRuntimeException on inner runtime errors, or when the arg cannot be cast to T
     */
    public <T extends JanitorObject> T getRequired(final int position, final Class<T> cls) throws JanitorRuntimeException {
        final JanitorObject obj = get(position);
        if (obj != null && cls.isAssignableFrom(obj.getClass())) {
            try {
                return cls.cast(obj);
            } catch (ClassCastException e) {
                throw new JanitorNativeException(process, "%s: error casting required argument #%s to type %s".formatted(functionName, position, cls.getSimpleName()), e);
            }
        } else {
            throw new JanitorArgumentException(process, "%s: argument #%s is required to be of type %s, but it is of type %s".formatted(functionName, position, cls.getSimpleName(), ObjectUtilities.simpleClassNameOf(obj)));
        }
    }

    /**
     * Get a float value at the given position.
     * Any other number, such as an integer, is converted to a float.
     * @param i the position of the argument to get.
     * @return the float value at the given position.
     * @throws JanitorRuntimeException if the argument is not a number.
     */
    public JFloat getFloat(final int i) throws JanitorRuntimeException {
        final JanitorObject value = get(i);
        final JFloat floatValue = value.janitorCoerce(JFloat.class);
        if (floatValue != null) {
            return floatValue;
        }
        final JNumber number = value.janitorCoerce(JNumber.class);
        if (number == null) {
            throw new JanitorArgumentException(process, "%s: argument %s must be a number, but the caller provided: %s".formatted(functionName, i, args));
        }
        return Janitor.floatingPoint(number.toDouble());
    }

    /**
     * Get an integer value at the given position.
     * A float is taken if it is a whole number, e.g. {@code 5.0}.
     * @param i the position of the argument to get.
     * @return the integer value at the given position.
     * @throws JanitorRuntimeException if the argument is not a whole number.
     */
    public JInt getInt(final int i) throws JanitorRuntimeException {
        final JanitorObject value = get(i);
        final JInt intValue = value.janitorCoerce(JInt.class);
        if (intValue != null) {
            return intValue;
        }
        final Long wholeNumber = Conversions.toWholeNumberOrNull(value);
        if (wholeNumber == null) {
            throw new JanitorArgumentException(process, "%s: argument %s must be an integer value, but the caller provided: %s".formatted(functionName, i, args));
        }
        return Janitor.integer(wholeNumber);
    }

    /**
     * Get a number of seconds at the given position, written either as an integer or as a duration like {@code @60s}.
     * @param i the position of the argument to get.
     * @return the number of seconds.
     * @throws JanitorRuntimeException if the argument is neither an integer nor a duration.
     */
    public long getSeconds(final int i) throws JanitorRuntimeException {
        final JanitorObject value = get(i);
        final JDuration duration = value.janitorCoerce(JDuration.class);
        if (duration != null) {
            return duration.toSeconds();
        }
        final Long seconds = Conversions.toWholeNumberOrNull(value);
        if (seconds == null) {
            throw new JanitorArgumentException(process, "%s: argument %s must be a number of seconds or a duration, but the caller provided: %s".formatted(functionName, i, args));
        }
        return seconds;
    }

    /**
     * Get a string value at the given position.
     * If it's not a String, try to coerce it to a string.
     * @param i the position of the argument to get.
     * @return the string value at the given position.
     * @throws JanitorRuntimeException if the argument is not a string value.
     */
    public JString getString(final int i) throws JanitorRuntimeException {
        final JString stringValue = get(i).janitorCoerce(JString.class);
        if (stringValue == null) {
            throw new JanitorArgumentException(process, "%s: argument %s must be a string value, but the caller provided: %s".formatted(functionName, i, args));
        }
        return stringValue;
    }

    /**
     * Get the list of arguments.
     * @return the list of arguments.
     */
    @Deprecated(since = "0.9.34", forRemoval = true) // "missing out on named arguments"
    public List<JanitorObject> getList() {
        return positional.stream().map(EvaluatedArgument::getValue).toList();
    }


    @Override
    public String toString() {
        return "JCallArgs{" +
               "args=" + args +
               ", functionName='" + functionName + '\'' +
               '}';
    }

    /**
     * Gets a keyword argument by its name.
     * @param name the name of the argument
     * @return the value, or null if there is no argument of this name
     */
    public JanitorObject getByName(final @NotNull String name) {
        for (final EvaluatedArgument argument : named) {
            if (name.equals(argument.getName())) {
                usedNames.add(name);
                return argument.getValue();
            }
        }
        return null;
    }

    /**
     * Collects all keyword arguments into a map; they count as used, see {@link #requireNamesUsed()}.
     * @param except names of arguments to leave out, may be null
     * @return a map of all named arguments
     */
    public JMap asKwargs(final Set<String> except) {
        @NotNull final JMap map = Janitor.map();
        for (final EvaluatedArgument arg : named) {
            if (except == null || !except.contains(arg.getName())) {
                usedNames.add(arg.getName());
                map.put(arg.getName(), arg.getValue());
            }
        }
        return map;
    }

    /**
     * Shorthand for creating Constructors that take a single optional map argument, e.g. Foo({name: "foo", friend: "bar"}).
     * All fields of the instance are populated from the map automatically.
     * @param instance any object instance
     * @return the same instance, optionally populated with values from the map.
     */
    public JanitorObject applyOptionalMap(final JanitorScriptProcess process, final JanitorObject instance) throws JanitorNameException {
        if (positional.size() == 1 && positional.get(0).getValue() instanceof JMap map) {
            map.applyTo(process, instance);
        }
        return instance;
    }

    /**
     * Return JCallArgs with the first positional argument removed. The named arguments stay, and reading them in the
     * copy counts for this object, too.
     * @return the arguments, with the first positional argument removed
     */
    public JCallArgs shift() {
        final List<EvaluatedArgument> rest = new ArrayList<>(positional.subList(1, positional.size()));
        rest.addAll(named);
        return new JCallArgs(process, functionName, rest, usedNames);
    }

    /**
     * Requires that no arguments were passed at all.
     * @throws JanitorRuntimeException if there are any arguments
     */
    public void notAllowed() throws JanitorRuntimeException {
        require(0);
    }

    /**
     * Return all list arguments, throwing an exception if any keyword arguments are present.
     * @return the list of arguments
     * @throws JanitorRuntimeException if any keyword arguments are present
     */
    public List<JanitorObject> requireArgListOnly() throws JanitorRuntimeException {
        JMap existingKwArgs = asKwargs(Set.of());
        if (!existingKwArgs.isEmpty()) {
            throw new JanitorArgumentException(process, "%s: does not accept keyword arguments, but got: %s".formatted(functionName, existingKwArgs.keySet()));
        } else {
            return args.stream().map(EvaluatedArgument::getValue).toList();
        }
    }

}
