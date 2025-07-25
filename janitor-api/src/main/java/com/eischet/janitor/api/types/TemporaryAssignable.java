package com.eischet.janitor.api.types;

import com.eischet.janitor.api.errors.glue.JanitorGlueException;
import org.jetbrains.annotations.NotNull;

/**
 * An object (e.g. return value) that can be assigned to.
 * This is what the interpreter uses to run statements like this: "foo.jay = bar;", where "foo.jay" will be wrapped
 * in a TemporaryAssignable object. There are probably more elegant ways to implement such a thing, but this is what
 * I came up with.
 */
public class TemporaryAssignable implements JAssignable, JanitorObject {

    private final JanitorObject value;
    private final RuntimeConsumer<JanitorObject> setter;

    /**
     * Create a new temporary assignable.
     * @param value our value
     * @param setter code to call when someone assigns to us
     */
    public TemporaryAssignable(final JanitorObject value, final RuntimeConsumer<JanitorObject> setter) {
        this.value = value;
        this.setter = setter;
    }

    /**
     * Create a new temporary assignable.
     * @param value our value
     * @param setter code to call when someone assigns to us
     * @return the new temporary assignable
     */
    public static TemporaryAssignable of(final JanitorObject value, final RuntimeConsumer<JanitorObject> setter) {
        return new TemporaryAssignable(value, setter);
    }

    /**
     * Assign a value to this object.
     * @param value the value to assign
     * @return true if the assignment was successful
     * @throws JanitorGlueException on runtime errors
     */
    @Override
    public boolean assign(final JanitorObject value) throws JanitorGlueException {
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

    public JanitorObject getValue() {
        return value;
    }

    public RuntimeConsumer<JanitorObject> getSetter() {
        return setter;
    }
}
