package com.eischet.janitor.api.types.dispatch;

import com.eischet.janitor.api.errors.glue.JanitorGlueException;
import com.eischet.janitor.api.errors.runtime.JanitorArgumentException;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.types.TemporaryAssignable;
import com.eischet.janitor.api.types.builtin.*;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static com.eischet.janitor.api.util.ObjectUtilities.simpleClassNameOf;

public class Conversions {

    /**
     * Create a new JInt.
     *
     * @param value the value
     * @return the integer
     * @throws JanitorGlueException [JanitorArgumentException] if the value is not an integer
     */
    public static JanitorObject requireNullableInt(final JanitorObject value) throws JanitorGlueException {
        if (value == Janitor.NULL) {
            return Janitor.NULL;
        }
        if (value instanceof JInt ok) {
            return ok;
        }
        if (value instanceof TemporaryAssignable ta) {
            return requireNullableInt(ta.getValue());
        }
        throw new JanitorGlueException(JanitorArgumentException::fromGlue, "Expected an integer value or null but got " + value.janitorClassName() + ".");
    }



    public static JFloat requireFloat(final Object value) throws JanitorGlueException {
        if (value instanceof JFloat alreadyMatches) {
            return alreadyMatches;
        }
        if (value instanceof Number num) {
            return Janitor.getBuiltins().floatingPoint(num.doubleValue());
        }
        if (value instanceof TemporaryAssignable ta) {
            return requireFloat(ta.getValue());
        }
        throw new JanitorGlueException(JanitorArgumentException::fromGlue, "Expected a floating point value but got " + simpleClassNameOf(value) + ".");
    }

    public static <E> List<E> toList(JList list, ConverterFromJanitor<E> converter) throws JanitorGlueException {
        final List<E> result = new ArrayList<>(list.size());
        for (final JanitorObject element : list) {
            final E converted = converter.convertFromJanitor(element);
            result.add(converted);
        }
        return result;
    }

    public static <E> JanitorObject toJanitorList(List<E> list, ConverterToJanitor<E> converter) throws JanitorGlueException {
        if (list == null) {
            return JNull.NULL;
        }
        if (list.isEmpty()) {
            return Janitor.getBuiltins().list();
        }
        final JList result = Janitor.getBuiltins().list();
        for (final E element : list) {
            result.add(converter.convertToJanitor(element));
        }
        return result;
    }

    public static Integer toNullableJavaInteger(final @NotNull JanitorObject value) throws JanitorGlueException {
        if (value == Janitor.NULL) {
            return null;
        }
        if (value instanceof TemporaryAssignable ta) {
            return toNullableJavaInteger(ta.getValue());
        }
        if (value instanceof JNumber number) {
            return (int) number.toLong();
        }
        throw new JanitorGlueException(JanitorArgumentException::fromGlue, "Expected an integer value or null but got " + value.janitorClassName() + " [" + simpleClassNameOf(value)+ "].");
    }

    public static Long toNullableJavaLong(final @NotNull JanitorObject value) throws JanitorGlueException {
        if (value == Janitor.NULL) {
            return null;
        }
        if (value instanceof TemporaryAssignable ta) {
            return toNullableJavaLong(ta.getValue());
        }
        if (value instanceof JNumber number) {
            return number.toLong();
        }
        throw new JanitorGlueException(JanitorArgumentException::fromGlue, "Expected a long value or null but got " + value.janitorClassName() + " [" + simpleClassNameOf(value)+ "].");
    }




}
