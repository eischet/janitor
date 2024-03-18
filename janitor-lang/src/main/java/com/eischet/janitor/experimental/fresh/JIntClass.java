package com.eischet.janitor.experimental.fresh;

import org.eclipse.collections.api.factory.Maps;
import org.eclipse.collections.api.map.MutableMap;
import org.jetbrains.annotations.NotNull;

public class JIntClass extends JNativeClass<Long> {

    public static final JIntClass CLASS = new JIntClass();

    private static final FixedPropertyMap properties;

    static {
        final MutableMap<String, JProp> p = Maps.mutable.empty();

        properties = new FixedPropertyMap(p.toImmutable());
    }

    public static JInt of(final long value) {
        return new JInt(value);
    }

    @Override
    protected @NotNull JPropertyMap getProperties() {
        return properties;
    }

    public static class JInt implements JObject {

        private final long value;

        public JInt(final long value) {
            this.value = value;
        }

        public long getValue() {
            return value;
        }

        @Override
        public JClass jGetClass() {
            return CLASS;
        }

    }

}