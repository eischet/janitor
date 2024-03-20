package com.eischet.janitor.cleanup.experimental.fresh;

import org.eclipse.collections.api.factory.Maps;
import org.eclipse.collections.api.map.MutableMap;
import org.jetbrains.annotations.NotNull;

public class JStringClass extends JNativeClass<String> {

    public static final JStringClass CLASS = new JStringClass();

    private static final FixedPropertyMap properties;

    private static final JMethod<JString> SIZE = new JMethod<>("size") {
        @Override
        public JObject jCallMethod(final @NotNull JString self, final @NotNull JCallArgs args) {
            return JIntClass.of(self.value.length());
        }
    };

    static {
        final MutableMap<String, JProp> p = Maps.mutable.empty();
        p.put("length", new JPropReadonly<JIntClass.JInt>(JIntClass.CLASS, instance -> JIntClass.of(((JString) instance).value.length())));
        p.put("size", new JPropReadonly<>(JFunction.CLASS, instance -> SIZE));
        // actually add properties
        properties = new FixedPropertyMap(p.toImmutable());
    }




    @Override
    protected @NotNull JPropertyMap getProperties() {
        return properties;
    }

    public static class JString implements JObject {

        private final String value;

        public JString(final String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        @Override
        public JClass jGetClass() {
            return CLASS;
        }

    }

}
