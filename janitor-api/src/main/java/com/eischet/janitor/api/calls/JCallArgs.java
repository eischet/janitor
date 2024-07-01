package com.eischet.janitor.api.calls;


import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.runtime.JanitorArgumentException;
import com.eischet.janitor.api.errors.runtime.JanitorNativeException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.types.JInt;
import com.eischet.janitor.api.types.JNull;
import com.eischet.janitor.api.types.JString;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.api.util.ObjectUtilities;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class JCallArgs {


    private final @Nullable List<JanitorObject> args;
    private final String functionName;
    private final @NotNull JanitorScriptProcess runningScript;

    public JCallArgs(final String functionName, final @NotNull JanitorScriptProcess runningScript, final @Nullable List<JanitorObject> args) {
        this.functionName = functionName;
        this.runningScript = runningScript;
        this.args = args;
    }

    public String getFunctionName() {
        return functionName;
    }

    public static JCallArgs empty(final String functionName, final JanitorScriptProcess runningScript) {
        return new JCallArgs(functionName, runningScript, null);
    }

    public int size() {
        return args == null ? 0 : args.size();
    }

    public JCallArgs require(final int minSize, final int maxSize) throws JanitorRuntimeException {
        if (size() < minSize || size() > maxSize) {
            throw new JanitorArgumentException(runningScript, "%s: requires between %s and %s arguments, but got: %s".formatted(functionName, minSize, maxSize, args));
        }
        return this;
    }

    public JCallArgs require(final int size) throws JanitorRuntimeException {
        if (size() != size) {
            throw new JanitorArgumentException(runningScript, "%s: requires exactly %s arguments, but got: %s".formatted(functionName, size, args));
        }
        return this;
    }

    public JanitorObject get(final int position) {
        if (args == null) {
            throw new IndexOutOfBoundsException("No arguments provided");
        }
        return args.get(position).janitorUnpack(); // unpacking is super-important, so that we store the actual value instead of a property reference, for example (!)
    }

    public List<String> getStringList(int position) throws JanitorRuntimeException {
        final List<String> strings = new ArrayList<>();
        String next = getOptionalStringValue(position, null);
        while (next != null) {
            strings.add(next);
            next = getOptionalStringValue(++position, null);
        }
        return strings;
    }


    public String getOptionalStringValue(final int i, final String defaultValue) throws JanitorRuntimeException {
        if (i < size()) {
            return getString(i).janitorGetHostValue();
        }
        return defaultValue;
    }

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
                throw new JanitorNativeException(runningScript, "%s: error casting required argument #%s to type %s".formatted(functionName, position, cls.getSimpleName()), e);
            }
        } else {
            throw new JanitorArgumentException(runningScript, "%s: argument #%s is required to be of type %s, but it is of type %s".formatted(functionName, position, cls.getSimpleName(), ObjectUtilities.simpleClassNameOf(obj)));
        }
    }


    public <T extends JanitorObject> T getRequired(final int position, final Class<T> cls) throws JanitorRuntimeException {
        final JanitorObject obj = get(position);
        if (obj != null && cls.isAssignableFrom(obj.getClass())) {
            try {
                return cls.cast(obj);
            } catch (ClassCastException e) {
                throw new JanitorNativeException(runningScript, "%s: error casting required argument #%s to type %s".formatted(functionName, position, cls.getSimpleName()), e);
            }
        } else {
            throw new JanitorArgumentException(runningScript, "%s: argument #%s is required to be of type %s, but it is of type %s".formatted(functionName, position, cls.getSimpleName(), ObjectUtilities.simpleClassNameOf(obj)));
        }
    }

    public JInt getInt(final int i) throws JanitorRuntimeException {
        final JInt intValue = get(i).janitorCoerce(JInt.class);
        if (intValue == null) {
            throw new JanitorArgumentException(runningScript, "%s: argument %s must be an integer value, but the caller provided: %s".formatted(functionName, i, args));
        }
        return intValue;
    }

    public JString getString(final int i) throws JanitorRuntimeException {
        final JString stringValue = get(i).janitorCoerce(JString.class);
        if (stringValue == null) {
            throw new JanitorArgumentException(runningScript, "%s: argument %s must be a string value, but the caller provided: %s".formatted(functionName, i, args));
        }
        return stringValue;
    }

    @Override
    public String toString() {
        return "CSCallArgs{" +
            "args=" + args +
            ", functionName='" + functionName + '\'' +
            '}';
    }


    public List<JanitorObject> getList() {
        return args == null ? Collections.emptyList() : args;
    }
}