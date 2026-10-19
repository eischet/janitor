// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.api.types;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.JanitorException;
import com.eischet.janitor.api.errors.glue.JanitorGlueException;
import com.eischet.janitor.api.errors.runtime.JanitorAssignmentException;
import com.eischet.janitor.api.errors.runtime.JanitorError;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

import static com.eischet.janitor.api.util.ObjectUtilities.simpleClassNameOf;

/**
 * An object (e.g. return value) that can be assigned to.
 * This is what the interpreter uses to run statements like this: "foo.jay = bar;", where "foo.jay" will be wrapped
 * in a TemporaryAssignable object. There are probably more elegant ways to implement such a thing, but this is what
 * I came up with.
 * <p>
 * A temporary assignable created with {@link #lazy} reads its value only when someone asks for it, so that plain
 * assignments like "foo.jay = bar" never call the getter. This matters when the getter is expensive or cannot work
 * yet, e.g. when it lazily loads data from a database for an object that has not been stored.
 * </p>
 */
public class TemporaryAssignable implements JAssignable, JanitorObject {

    private final @NotNull  String name;
    private @Nullable JanitorObject value;
    private @Nullable ValueSupplier supplier;
    private final @NotNull RuntimeConsumer<JanitorObject> setter;

    /**
     * Supplies the current value of a lazy temporary assignable.
     */
    @FunctionalInterface
    public interface ValueSupplier {
        /**
         * @return the current value
         * @throws Exception on errors, e.g. when a getter fails
         */
        @NotNull JanitorObject get() throws Exception;
    }

    @Override
    public String toString() {
        return "TemporaryAssignable{name='"+name+"', value="+(value == null ? "(not read yet)" : simpleClassNameOf(value))+"}";
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
     * Create a new lazy temporary assignable, which reads its value only when needed.
     * @param name a readable name, e.g. for a field
     * @param supplier supplies the current value
     * @param setter code to call when someone assigns to us
     */
    protected TemporaryAssignable(final @NotNull String name, final @NotNull ValueSupplier supplier, final @NotNull RuntimeConsumer<JanitorObject> setter) {
        this.name = name;
        this.supplier = supplier;
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
     * Create a new lazy temporary assignable, which calls the supplier only when its value is needed, and at most once.
     * Assigning to it does not read the value.
     * @param name a readable name, e.g. for a field
     * @param supplier supplies the current value
     * @param setter code to call when someone assigns to us
     * @return the new temporary assignable
     */
    public static TemporaryAssignable lazy(final @NotNull String name, final @NotNull ValueSupplier supplier, final @NotNull RuntimeConsumer<JanitorObject> setter) {
        return new TemporaryAssignable(name, supplier, setter);
    }

    /**
     * Reads the value now, if that has not happened yet.
     * Callers that can handle errors should call this before using the value, because {@link #getValue()} can only
     * report them as unchecked exceptions.
     * @return the current value
     * @throws Exception if reading the value fails
     */
    public @NotNull JanitorObject resolve() throws Exception {
        if (value == null) {
            value = Objects.requireNonNull(Objects.requireNonNull(supplier).get(), name);
            supplier = null;
        }
        return value;
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
        return getValue().janitorGetHostValue();
    }

    @Override
    public @NotNull String janitorToString() {
        return getValue().janitorToString();
    }

    @Override
    public boolean janitorIsTrue() {
        return getValue().janitorIsTrue();
    }

    @Override
    public @NotNull String janitorClassName() {
        return getValue().janitorClassName();
    }

    @Override
    public @NotNull JanitorObject janitorUnpack() {
        return getValue();
    }

    /**
     * @return the current value, which is read now if that has not happened yet
     * @throws JanitorError if reading the value fails
     */
    public @NotNull JanitorObject getValue() {
        try {
            return resolve();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new JanitorError("error reading the value of " + name, e);
        }
    }

    /**
     * @return the function that assigns a new value
     */
    public @NotNull RuntimeConsumer<JanitorObject> getSetter() {
        return setter;
    }
}
