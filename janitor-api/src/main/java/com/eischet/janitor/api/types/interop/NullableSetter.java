// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.api.types.interop;

import com.eischet.janitor.api.errors.glue.JanitorGlueException;
import com.eischet.janitor.api.errors.runtime.JanitorArgumentException;
import com.eischet.janitor.api.errors.runtime.JanitorAssignmentException;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.toolbox.strings.StringHelpers;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.eischet.janitor.api.util.ObjectUtilities.simpleClassNameOf;

/**
 * Writes a property that may be null to an instance.
 * @param <INSTANCE> the type of the object that owns the property
 * @param <PROPERTY> the type of the property
 */
@FunctionalInterface
public interface NullableSetter<INSTANCE, PROPERTY> {

    /**
     * Returns the setter, or a setter that rejects every assignment if there is none.
     * @param name the name of the property, for the error message
     * @param setter the setter, may be null
     * @param <T> the type of the object
     * @param <U> the type of the property
     * @return the setter, or one that throws an assignment error
     */
    static @NotNull <T extends JanitorObject, U> NullableSetter<T, U> ofNullable(@NotNull final String name, @Nullable NullableSetter<T, U> setter) {
        if (setter != null) {
            return setter;
        } else {
            return (instance, value) -> {
                throw new JanitorGlueException(JanitorAssignmentException::fromGlue, "The field " + name + " cannot be assigned to!");
            };
        }
    }

    /**
     * Sets the property.
     * @param instance the object
     * @param value the new value, may be null
     * @throws Exception on errors
     */
    void set(@NotNull INSTANCE instance, @Nullable PROPERTY value) throws Exception;

    /**
     * Adapts a setter for a primitive boolean, which rejects null.
     * @param setter the primitive setter
     * @param <T> the type of the object
     * @return the adapted setter
     */
    static <T> NullableSetter<T, Boolean> guard(PrimitiveBooleanSetter<T> setter) {
        return (instance, value) -> {
            if (value == null) {
                throw new JanitorGlueException(JanitorArgumentException::fromGlue, "Expected a boolean value but got null.");
            } else {
                setter.set(instance, value);
            }
        };
    }

    /**
     * Adapts a setter for a primitive long, which rejects null.
     * @param setter the primitive setter
     * @param <T> the type of the object
     * @return the adapted setter
     */
    static <T> NullableSetter<T, Long> guard(PrimitiveLongSetter<T> setter) {
        return (instance, value) -> {
            if (value == null) {
                throw new JanitorGlueException(JanitorArgumentException::fromGlue, "Expected a numeric value but got null.");
            } else {
                setter.set(instance, value);
            }
        };
    }

    /**
     * Adapts a setter for a primitive int, which rejects null.
     * @param setter the primitive setter
     * @param <T> the type of the object
     * @return the adapted setter
     */
    static <T> NullableSetter<T, Integer> guard(PrimitiveIntSetter<T> setter) {
        return (instance, value) -> {
            if (value == null) {
                throw new JanitorGlueException(JanitorArgumentException::fromGlue, "Expected a numeric value but got null.");
            } else {
                setter.set(instance, value);
            }
        };
    }

    /**
     * Adapts a setter for a primitive double, which rejects null.
     * @param setter the primitive setter
     * @param <T> the type of the object
     * @return the adapted setter
     */
    static <T> NullableSetter<T, Double> guard(PrimitiveDoubleSetter<T> setter) {
        return (instance, value) -> {
            if (value == null) {
                throw new JanitorGlueException(JanitorArgumentException::fromGlue, "Expected a numeric value but got null.");
            } else {
                setter.set(instance, value);
            }
        };
    }

    /**
     * Creates a setter for a read-only property, which rejects every assignment.
     * @param name the name of the property, for the error message
     * @param <T> the type of the object
     * @param <U> the type of the property
     * @return the setter
     */
    static <T extends JanitorObject, U> NullableSetter<T, U> readOnly(final @NotNull String name) {
        return (T instance, U value) -> instance.janitorWarn("Ignoring attempt to write value '" + StringHelpers.cut(String.valueOf(value), 50) + " [" + simpleClassNameOf(value) + " ]' to read-only field '" + name + "'.");
    }


}
