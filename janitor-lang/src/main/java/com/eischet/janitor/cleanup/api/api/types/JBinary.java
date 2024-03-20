package com.eischet.janitor.cleanup.api.api.types;

import com.eischet.janitor.cleanup.api.api.errors.runtime.JanitorNameException;
import com.eischet.janitor.cleanup.runtime.types.JUnboundMethod;
import com.eischet.janitor.cleanup.runtime.types.JanitorClass;
import org.eclipse.collections.api.factory.Maps;
import org.eclipse.collections.api.map.ImmutableMap;
import org.eclipse.collections.api.map.MutableMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public class JBinary implements JConstant {

    private static final JBinaryClass cls = new JBinaryClass();

    public static class JBinaryClass extends JanitorClass<JBinary> {

        private static final ImmutableMap<String, JUnboundMethod<JBinary>> methods;

        static {
            final MutableMap<String, JUnboundMethod<JBinary>> m = Maps.mutable.empty();
            m.put("encodeBase64", (self, runningScript, arguments) -> {
                if (self.arr == null) {
                    return JString.ofNullable(null);
                }
                return JString.of(new String(java.util.Base64.getEncoder().encode(self.arr)));
            });
            m.put("toString", (self, runningScript, arguments) -> JString.ofNullable(self.janitorIsTrue() ? new String(self.arr) : null));
            m.put("size", (self, runningScript, arguments) -> JInt.of(self.size()));
            methods = m.toImmutable();
        }

        public JBinaryClass() {
            super(null, methods);
        }
    }



    private final byte[] arr;

    @Override
    public boolean janitorIsTrue() {
        return arr != null && arr.length > 0;
    }

    public int size() {
        return arr == null ? 0 : arr.length;
    }

    public JBinary(final byte[] arr) {
        this.arr = arr;
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

}
