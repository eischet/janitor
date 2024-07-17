package com.eischet.janitor.api.types;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.calls.JUnboundMethod;
import com.eischet.janitor.api.errors.runtime.JanitorNameException;
import com.eischet.janitor.api.traits.JConstant;
import com.eischet.janitor.api.util.JanitorClass;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * A binary object, like a byte array.
 * This is one of the built-in types that Janitor provides automatically.
 */
public class JBinary implements JConstant {

    private static final JBinaryClass cls = new JBinaryClass();
    private final byte[] arr;

    /**
     * Create a new JBinary from a byte array.
     *
     * @param arr the byte array
     */
    public JBinary(final byte[] arr) {
        this.arr = arr;
    }

    /**
     * Define truthiness: the backing byte array contains a least one byte.
     *
     * @return true if the array is not null and has at least one element
     */
    @Override
    public boolean janitorIsTrue() {
        return arr != null && arr.length > 0;
    }

    /**
     * Get the size of the binary.
     *
     * @return the size of the binary
     */
    public int size() {
        return arr == null ? 0 : arr.length;
    }

    @Override
    public byte[] janitorGetHostValue() {
        return arr;
    }

    @Override
    public String janitorToString() {
        return Arrays.toString(arr);
    }

    @Override
    public String toString() {
        return janitorToString();
    }

    @Override
    public @Nullable JanitorObject janitorGetAttribute(final JanitorScriptProcess runningScript, final String name, final boolean required) throws JanitorNameException {
        final JanitorObject boundMethod = cls.getBoundMethod(name, this);
        if (boundMethod != null) {
            return boundMethod;
        }
        if ("string".equals(name)) {
            return JString.ofNullable(janitorIsTrue() ? new String(this.arr) : null);
        }
        if ("length".equals(name)) {
            return JInt.of(size());
        }
        return JConstant.super.janitorGetAttribute(runningScript, name, required);
    }

    @Override
    public @NotNull String janitorClassName() {
        return "binary";
    }

    /**
     * The class of a JBinary.
     * TODO: this old implementation should be converted to a dispatch table instead
     */
    public static class JBinaryClass extends JanitorClass<JBinary> {

        private static final Map<String, JUnboundMethod<JBinary>> methods;

        static {
            final Map<String, JUnboundMethod<JBinary>> m = new HashMap<>();
            m.put("encodeBase64", (self, runningScript, arguments) -> {
                if (self.arr == null) {
                    return JString.ofNullable(null);
                }
                return JString.of(new String(java.util.Base64.getEncoder().encode(self.arr)));
            });
            m.put("toString", (self, runningScript, arguments) -> JString.ofNullable(self.janitorIsTrue() ? new String(self.arr) : null));
            m.put("size", (self, runningScript, arguments) -> JInt.of(self.size()));
            methods = m;
        }

        public JBinaryClass() {
            super(null, methods);
        }
    }

}
