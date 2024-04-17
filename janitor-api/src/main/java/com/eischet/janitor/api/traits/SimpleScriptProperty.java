package com.eischet.janitor.api.traits;

import com.eischet.janitor.api.types.*;
import org.jetbrains.annotations.NotNull;

public class SimpleScriptProperty<V> implements JanitorObject, JAssignable {

    private final String name;
    private final Getter getter;
    private final Setter setter;
    private final Class<?> hostType;
    private final HostGetter hostGetter;

    public SimpleScriptProperty(final String name, final HostGetter<V> hostGetter, final Getter getter, final Setter setter, final Class<?> hostType) {
        this.name = name;
        this.hostGetter = hostGetter;
        this.getter = getter;
        this.setter = setter;
        this.hostType = hostType;
    }

    @Override
    public boolean assign(final JanitorObject value) {
        if (setter == null) {
            return false;
        }
        return setter.set(value);
    }

    @Override
    public JanitorObject janitorUnpack() {
        return getter.get();
    }

    @Override
    public String toString() {
        return "SimpleScriptProperty<" + name + " = " + String.valueOf(janitorGetHostValue()) + ">";
    }

    @Override
    public String describeAssignable() {
        return "SimpleScriptProperty<" + name + ">";
    }

    @Override
    public Object janitorGetHostValue() {
        return hostGetter.get();
    }

    @Override
    public String janitorToString() {
        return String.valueOf(hostGetter.get());
    }

    @Override
    public boolean janitorIsTrue() {
        final JanitorObject v = getter.get();
        return v != null && v.janitorIsTrue();
    }

    @FunctionalInterface
    public interface HostGetter<V> {
        V get();
    }

    @FunctionalInterface
    public interface HostSetter<V> {
        void set(V v);
    }

    @FunctionalInterface
    public interface Getter {
        JanitorObject get();
    }

    @FunctionalInterface
    public interface Setter {
        boolean set(JanitorObject value);
    }

    public static SimpleScriptProperty<String> ofString(final String name, final HostGetter<String> hostGetter) {
        return new SimpleScriptProperty<>(name, hostGetter, () -> JString.ofNullable(hostGetter.get()), null, String.class);
    }

    public static SimpleScriptProperty<String> ofString(final String name, final HostGetter<String> hostGetter, final HostSetter<String> hostSetter) {
        return new SimpleScriptProperty<>(name, hostGetter, () -> JString.ofNullable(hostGetter.get()), str -> {
            hostSetter.set(str == null ? null : str.janitorToString());
            return true;
        }, String.class);
    }

    public static SimpleScriptProperty<Boolean> ofBoolean(final String name, final HostGetter<Boolean> hostGetter) {
        return new SimpleScriptProperty<>(name, hostGetter, () -> JBool.of(hostGetter.get()), null, Boolean.class);
    }

    public static SimpleScriptProperty<JList> ofList(final String name, final HostGetter<JList> hostGetter) {
        return new SimpleScriptProperty<>(name, hostGetter, hostGetter::get, null, JList.class);
    }

    public static SimpleScriptProperty<JBinary> ofBinary(final String name, final HostGetter<JBinary> hostGetter) {
        return new SimpleScriptProperty<>(name, hostGetter, hostGetter::get, null, JBinary.class);
    }

    @Override
    public @NotNull String janitorClassName() {
        return "property";
    }

}
