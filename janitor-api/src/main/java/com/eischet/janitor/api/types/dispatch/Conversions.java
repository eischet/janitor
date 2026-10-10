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

/** Static helpers for converting between Janitor values and Java values, for use in glue code. */
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



    /**
     * Converts a value to a Janitor float.
     * @param value a {@link JFloat}, a Java {@link Number}, or an assignable wrapper around one of these
     * @return the float value
     * @throws JanitorGlueException if the value is not numeric
     */
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

    /**
     * Converts a Janitor list to a Java list, converting each element.
     * @param list the Janitor list
     * @param converter the converter to apply to each element
     * @param <E> the Java element type
     * @return a new Java list
     * @throws JanitorGlueException if an element cannot be converted
     */
    public static <E> List<E> toList(JList list, ConverterFromJanitor<E> converter) throws JanitorGlueException {
        final List<E> result = new ArrayList<>(list.size());
        for (final JanitorObject element : list) {
            final E converted = converter.convertFromJanitor(element);
            result.add(converted);
        }
        return result;
    }

    /**
     * Converts a Java list to a Janitor list, converting each element.
     * @param list the Java list, may be null
     * @param converter the converter to apply to each element
     * @param <E> the Java element type
     * @return a new Janitor list, or null (the Janitor null value) if the Java list is null
     * @throws JanitorGlueException if an element cannot be converted
     */
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

    /**
     * Converts a Janitor value to a Java Integer.
     * @param value a Janitor number, or null
     * @return the integer value, or null if the value is the Janitor null value
     * @throws JanitorGlueException if the value is not a number
     */
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

    /**
     * Converts a Janitor value to a Java Long.
     * @param value a Janitor number, or null
     * @return the long value, or null if the value is the Janitor null value
     * @throws JanitorGlueException if the value is not a number
     */
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
