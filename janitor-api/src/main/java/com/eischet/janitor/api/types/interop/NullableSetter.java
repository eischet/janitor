package com.eischet.janitor.api.types.interop;

import com.eischet.janitor.api.errors.glue.JanitorGlueException;
import com.eischet.janitor.api.errors.runtime.JanitorArgumentException;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.toolbox.strings.StringHelpers;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.eischet.janitor.api.util.ObjectUtilities.simpleClassNameOf;

@FunctionalInterface
public interface NullableSetter<INSTANCE, PROPERTY> {
    void set(@NotNull INSTANCE instance, @Nullable PROPERTY value) throws Exception;

    static <T> NullableSetter<T, Boolean> guard(PrimitiveBooleanSetter<T> setter) {
        return (instance, value) -> {
            if (value == null) {
                throw new JanitorGlueException(JanitorArgumentException::fromGlue, "Expected a boolean value but got null.");
            } else {
                setter.set(instance, value);
            }
        };
    }

    static <T> NullableSetter<T, Long> guard(PrimitiveLongSetter<T> setter) {
        return (instance, value) -> {
            if (value == null) {
                throw new JanitorGlueException(JanitorArgumentException::fromGlue, "Expected a numeric value but got null.");
            } else {
                setter.set(instance, value);
            }
        };
    }

    static <T> NullableSetter<T, Integer> guard(PrimitiveIntSetter<T> setter) {
        return (instance, value) -> {
            if (value == null) {
                throw new JanitorGlueException(JanitorArgumentException::fromGlue, "Expected a numeric value but got null.");
            } else {
                setter.set(instance, value);
            }
        };
    }

    static <T> NullableSetter<T, Double> guard(PrimitiveDoubleSetter<T> setter) {
        return (instance, value) -> {
            if (value == null) {
                throw new JanitorGlueException(JanitorArgumentException::fromGlue, "Expected a numeric value but got null.");
            } else {
                setter.set(instance, value);
            }
        };
    }

    static <T extends JanitorObject, U> NullableSetter<T, U> readOnly(final @NotNull String name) {
        return (T instance, U value) -> instance.janitorWarn("Ignoring attempt to write value '" + StringHelpers.cut(String.valueOf(value), 50) + " [" + simpleClassNameOf(value) + " ]' to read-only field '" + name + "'.");
    }


}
