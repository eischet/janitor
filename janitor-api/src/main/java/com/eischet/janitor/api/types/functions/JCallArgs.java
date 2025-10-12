package com.eischet.janitor.api.types.functions;


import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.runtime.JanitorArgumentException;
import com.eischet.janitor.api.errors.runtime.JanitorNativeException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.api.types.builtin.*;
import com.eischet.janitor.api.util.ObjectUtilities;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * A class that represents the arguments passed to a function call.
 */
public class JCallArgs {

    private final String functionName;
    private final @Nullable List<EvaluatedArgument> args;
    private final @NotNull JanitorScriptProcess process;

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
    }

    public JCallArgs(final JanitorScriptProcess process, final String identifier, final @NotNull List<EvaluatedArgument> evaluatedArguments) {
        this.functionName = identifier;
        this.process = process;
        this.args = evaluatedArguments;
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
     * Get the name of the function being called.
     *
     * @return the name of the function being called.
     */
    public String getFunctionName() {
        return functionName;
    }

    /**
     * Get the number of arguments in the call.
     *
     * @return the number of arguments in the call.
     */
    public int size() {
        return args == null ? 0 : args.size();
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
     * Get the argument at the given position.
     *
     * @param position the position of the argument to get.
     * @return the argument at the given position.
     * @throws IndexOutOfBoundsException if the position is out of bounds.
     */
    public JanitorObject get(final int position) throws IndexOutOfBoundsException {
        if (args == null) {
            throw new IndexOutOfBoundsException("No arguments provided");
        }
        // TODO: I'm pretty sure this will fail when named args with defaults come in...
        return args.get(position).getValue().janitorUnpack(); // unpacking is super-important, so that we store the actual value instead of a property reference, for example (!)
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

    public int getRequiredIntValue(final int i) throws JanitorArgumentException {
        final JanitorObject arg = get(i);
        if (arg instanceof JInt num) {
            if (num.toLong() > Integer.MAX_VALUE) {
                throw new JanitorArgumentException(process, "%s: argument %s must be an integer value, but the value that was provided is too big for an integer: %s".formatted(functionName, i, args));
            }
            return (int) num.toLong();
        }
        throw new JanitorArgumentException(process, "%s: argument %s must be a numeric value, but the caller provided: %s".formatted(functionName, i, args));
    }

    public long getRequiredLongValue(final int i) throws JanitorArgumentException {
        final JanitorObject arg = get(i);
        if (arg instanceof JNumber num) {
            return num.toLong();
        }
        throw new JanitorArgumentException(process, "%s: argument %s must be a numeric value, but the caller provided: %s".formatted(functionName, i, args));
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
     * Get a boolean value at the given position.
     * If it's not a Float, try to coerce it to one.
     * @param i the position of the argument to get.
     * @return the boolean value at the given position.
     * @throws JanitorRuntimeException if the argument is not a boolean value.
     */
    public JFloat getFloat(final int i) throws JanitorRuntimeException {
        final JFloat floatValue = get(i).janitorCoerce(JFloat.class);
        if (floatValue == null) {
            throw new JanitorArgumentException(process, "%s: argument %s must be a float value, but the caller provided: %s".formatted(functionName, i, args));
        }
        return floatValue;
    }

    /**
     * Get an integer value at the given position.
     * If it's not an integer, try to coerce it to an integer.
     * @param i the position of the argument to get.
     * @return the integer value at the given position.
     * @throws JanitorRuntimeException if the argument is not an integer value.
     */
    public JInt getInt(final int i) throws JanitorRuntimeException {
        final JInt intValue = get(i).janitorCoerce(JInt.class);
        if (intValue == null) {
            throw new JanitorArgumentException(process, "%s: argument %s must be an integer value, but the caller provided: %s".formatted(functionName, i, args));
        }
        return intValue;
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
        return args == null ? Collections.emptyList() : args.stream().map(EvaluatedArgument::getValue).toList();
    }

    @Override
    public String toString() {
        return "JCallArgs{" +
               "args=" + args +
               ", functionName='" + functionName + '\'' +
               '}';
    }

    public JanitorObject getByName(final @NotNull String name) {
        return args.stream()
                .filter(element -> Objects.nonNull(element.getName()))
                .filter(element -> Objects.equals(element.getName(), name))
                .map(EvaluatedArgument::getValue)
                .findFirst()
                .orElse(null);
    }

    public JMap asKwargs(final Set<String> except) {
        @NotNull final JMap map = Janitor.map();
        for (final EvaluatedArgument arg : args) {
            if (arg.getName() != null) {
                if (except == null || !except.contains(arg.getName())) {
                    map.put(arg.getName(), arg.getValue());
                }
            }
        }
        return map;
    }

}
