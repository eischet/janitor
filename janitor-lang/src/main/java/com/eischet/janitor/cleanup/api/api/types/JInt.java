package com.eischet.janitor.cleanup.api.api.types;

import com.eischet.janitor.cleanup.api.api.errors.runtime.JanitorNameException;
import com.eischet.janitor.cleanup.runtime.types.JFieldAdapter;
import com.eischet.janitor.cleanup.runtime.types.JavaGetter;
import com.eischet.janitor.cleanup.runtime.types.JavaSetter;
import com.eischet.janitor.cleanup.tools.DateTimeUtilities;
import com.eischet.janitor.cleanup.json.JsonExportablePrimitive;
import com.eischet.janitor.api.json.JsonException;
import com.eischet.janitor.cleanup.json.JsonOutputStream;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class JInt implements JConstant, JsonExportablePrimitive {

    private final long integer;

    @Override
    public boolean janitorIsTrue() {
        return integer != 0;
    }

    public JInt(final long integer) {
        this.integer = integer;
    }

    public static JanitorObject ofNullable(final Long value) {
        return value == null ? JNull.NULL : JInt.of(value);
    }

    public static JanitorObject ofNullable(final Integer value) {
        return value == null ? JNull.NULL : JInt.of(value);
    }

    @Override
    public Long janitorGetHostValue() {
        return integer;
    }

    public double getAsDouble() {
        return integer;
    }

    public int getAsInt() {
        return (int) integer;
    }

    @Override
    public String toString() {
        return String.valueOf(integer);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final JInt that = (JInt) o;
        return integer == that.integer;
    }

    @Override
    public int hashCode() {
        return Objects.hash(integer);
    }

    public static JInt of(final long value) {
        return new JInt(value);
    }

    public static JInt ofNullableOrZero(final Long value) {
        return value == null ? JInt.of(0) : JInt.of(value);
    }

    public static JInt of(final int value) {
        return new JInt(value);
    }

    public long getValue() {
        return integer;
    }

    @Override
    public @Nullable JanitorObject janitorGetAttribute(final JanitorScriptProcess runningScript, final String name, final boolean required) throws JanitorNameException {
        if (Objects.equals(name, "int")) {
            return this;
        }
        if (Objects.equals(name, "epoch")) {
            return JDateTime.ofNullable(DateTimeUtilities.localFromEpochSeconds(integer));
        }
        return JConstant.super.janitorGetAttribute(runningScript, name, required);
    }

    public static <T> JFieldAdapter<T> adaptInt(final JavaGetter<T, Integer> getter, final JavaSetter<T, Integer> setter) {
        return new JFieldAdapter<T>() {
            @Override
            public JanitorObject getPropertyValue(final T instance) {
                return ofNullable(getter.getJavaValue(instance));
            }

            @Override
            public void setPropertyValue(final T instance, final JanitorObject value) {
                final JanitorObject unpacked = value.janitorUnpack();
                if (unpacked instanceof JInt csInt) {
                    setter.setJavaValue(instance, (int) csInt.getValue());
                } else {
                    System.err.println("what should we do now?"); // LATER: figure this out
                }
            }
        };
    }

    public static <T> JFieldAdapter<T> adaptLong(final JavaGetter<T, Long> getter, final JavaSetter<T, Long> setter) {
        // LATER: direkt hier Nullable / Not-Nullable berücksichtigen, für long vs. Long?
        return new JFieldAdapter<T>() {
            @Override
            public JanitorObject getPropertyValue(final T instance) {
                return ofNullable(getter.getJavaValue(instance));
            }

            @Override
            public void setPropertyValue(final T instance, final JanitorObject value) {
                final JanitorObject unpacked = value.janitorUnpack();
                if (unpacked instanceof JInt csInt) {
                    setter.setJavaValue(instance, csInt.getValue());
                } else {
                    System.err.println("what should we do now?"); // LATER: figure this out
                }
            }
        };
    }

    @Override
    public @NotNull String janitorClassName() {
        return "int";
    }


    @Override
    public boolean isDefaultOrEmpty() {
        return integer == 0;
    }

    @Override
    public void writeJson(final JsonOutputStream producer) throws JsonException {
        producer.value(integer);
    }


}
