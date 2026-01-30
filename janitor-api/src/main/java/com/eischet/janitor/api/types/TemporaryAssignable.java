package com.eischet.janitor.api.types;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.JanitorException;
import com.eischet.janitor.api.errors.glue.JanitorGlueException;
import com.eischet.janitor.api.errors.runtime.JanitorAssignmentException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.eischet.janitor.api.util.ObjectUtilities.simpleClassNameOf;

/**
 * An object (e.g. return value) that can be assigned to.
 * This is what the interpreter uses to run statements like this: "foo.jay = bar;", where "foo.jay" will be wrapped
 * in a TemporaryAssignable object. There are probably more elegant ways to implement such a thing, but this is what
 * I came up with.
 */
public class TemporaryAssignable implements JAssignable, JanitorObject {

    private final @NotNull  String name;
    private final @NotNull JanitorObject value;
    private final @NotNull RuntimeConsumer<JanitorObject> setter;

    @Override
    public String toString() {
        return "TemporaryAssignable{name='"+name+"', value="+simpleClassNameOf(value)+"}";
    }

    /**
     * Create a new temporary assignable.
     * @param name a readable name, e.g. for a field
     * @param value our value
     * @param setter code to call when someone assigns to us
     */
    protected TemporaryAssignable(final @NotNull String name, final @NotNull JanitorObject value, final @NotNull RuntimeConsumer<JanitorObject> setter) {
        this.name = name;
        this.value = value;
        this.setter = setter;
    }

    /**
     * Create a new temporary assignable.
     * @param name a readable name, e.g. for a field
     * @param value our value
     * @param setter code to call when someone assigns to us
     * @return the new temporary assignable
     */
    public static TemporaryAssignable of(final @NotNull String name, final @NotNull JanitorObject value, final @NotNull RuntimeConsumer<JanitorObject> setter) {
        return new TemporaryAssignable(name, value, setter);
    }

    /**
     * Assign a value to this object.
     * @param value the value to assign
     * @return true if the assignment was successful
     * @throws JanitorGlueException on runtime errors
     */
    @Override
    public boolean assign(final JanitorObject value) throws JanitorGlueException {
        try {
            this.setter.accept(value);
            return true;
        } catch (JanitorGlueException e) {
            throw e;
        }  catch (Exception e) {
            throw new JanitorGlueException(JanitorAssignmentException::fromGlue, e);
        }
    }

    @Override
    public String describeAssignable() {
        return "temporary assignable";
    }

    @Override
    public @Nullable Object janitorGetHostValue() {
        return value.janitorGetHostValue();
    }

    @Override
    public @NotNull String janitorToString() {
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
    public @NotNull JanitorObject janitorUnpack() {
        return value;
    }

    public @NotNull JanitorObject getValue() {
        return value;
    }

    public @NotNull RuntimeConsumer<JanitorObject> getSetter() {
        return setter;
    }
}
