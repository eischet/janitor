package com.eischet.janitor.cleanup.runtime.types;

import com.eischet.janitor.cleanup.api.api.types.JanitorObject;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class TemporaryAssignable implements JAssignable, JanitorObject {


    public static TemporaryAssignable of(final JanitorObject value, final Consumer<JanitorObject> setter) {
        return new TemporaryAssignable(value, setter);
    }

    private final JanitorObject value;
    private final Consumer<JanitorObject> setter;

    public TemporaryAssignable(JanitorObject value, final Consumer<JanitorObject> setter) {
        this.value = value;
        this.setter = setter;
    }

    @Override
    public boolean assign(JanitorObject value) {
        this.setter.accept(value);
        return true;
    }

    @Override
    public String describeAssignable() {
        return "temporary assignable";
    }

    @Override
    public Object janitorGetHostValue() {
        return value.janitorGetHostValue();
    }

    @Override
    public String janitorToString() {
        return value.janitorToString();
    }

    @Override
    public boolean janitorIsTrue() {
        return value.janitorIsTrue();
    }

    @Override
    public @NotNull String janitorClassName() {
        return value.janitorClassName();
    }

    @Override
    public JanitorObject janitorUnpack() {
        return value;
    }
}
