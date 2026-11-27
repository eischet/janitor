// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

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
     * @param value a {@link JFloat}, another {@link JNumber} such as an integer, a Java {@link Number}, or an assignable wrapper around one of these
     * @return the float value
     * @throws JanitorGlueException if the value is not numeric
     */
    public static JFloat requireFloat(final Object value) throws JanitorGlueException {
        if (value instanceof JFloat alreadyMatches) {
            return alreadyMatches;
        }
        if (value instanceof JNumber number) {
            return Janitor.getBuiltins().floatingPoint(number.toDouble());
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
        return narrowToInt(wholeNumber(value, "an integer value or null"));
    }

    /**
     * Converts a Janitor value to a Java long.
     * A float is taken if it is a whole number, so a script may write {@code 5.0} for 5; a float with a fraction is refused
     * instead of being cut off.
     *
     * @param value a Janitor number
     * @return the long value
     * @throws JanitorGlueException if the value is not a whole number
     */
    public static long toJavaLong(final @NotNull JanitorObject value) throws JanitorGlueException {
        return wholeNumber(value, "an integer value");
    }

    /**
     * Converts a Janitor value to a Java int, by the rules of {@link #toJavaLong(JanitorObject)}.
     *
     * @param value a Janitor number
     * @return the int value
     * @throws JanitorGlueException if the value is not a whole number, or too large for an int
     */
    public static int toJavaInt(final @NotNull JanitorObject value) throws JanitorGlueException {
        return narrowToInt(wholeNumber(value, "an integer value"));
    }

    private static int narrowToInt(final long number) throws JanitorGlueException {
        if (number > Integer.MAX_VALUE || number < Integer.MIN_VALUE) {
            throw new JanitorGlueException(JanitorArgumentException::fromGlue, "The number " + number + " is too large for this property.");
        }
        return (int) number;
    }

    private static long wholeNumber(final JanitorObject value, final String expected) throws JanitorGlueException {
        final JNumber number = numberIn(value);
        if (number == null) {
            throw new JanitorGlueException(JanitorArgumentException::fromGlue,
                    "Expected " + expected + " but got " + (value == null ? "<null>" : value.janitorClassName()) + " [" + simpleClassNameOf(value) + "].");
        }
        if (number instanceof JInt) {
            return number.toLong();
        }
        final double floating = number.toDouble();
        // 2^63 as a double: every whole double below it fits into a long
        if (floating != Math.rint(floating) || floating < -0x1p63 || floating >= 0x1p63) {
            throw new JanitorGlueException(JanitorArgumentException::fromGlue, "Expected " + expected + " but got " + floating + ", which is not a whole number.");
        }
        return (long) floating;
    }

    private static JNumber numberIn(final JanitorObject value) {
        if (value == null) {
            return null;
        }
        if (value instanceof JNumber number) {
            return number;
        }
        if (value instanceof TemporaryAssignable ta) {
            return numberIn(ta.getValue());
        }
        for (final JanitorObject unpacked : value.janitorUnpackAll()) {
            if (unpacked instanceof JNumber number) {
                return number;
            }
        }
        return null;
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
        return wholeNumber(value, "a long value or null");
    }

    /**
     * Converts a Janitor value to a Java Double.
     * @param value a Janitor number, or null
     * @return the double value, or null if the value is the Janitor null value
     * @throws JanitorGlueException if the value is not a number
     */
    public static Double toNullableJavaDouble(final @NotNull JanitorObject value) throws JanitorGlueException {
        if (value == Janitor.NULL) {
            return null;
        }
        if (value instanceof TemporaryAssignable ta) {
            return toNullableJavaDouble(ta.getValue());
        }
        if (value instanceof JNumber number) {
            return number.toDouble();
        }
        throw new JanitorGlueException(JanitorArgumentException::fromGlue, "Expected a floating point value or null but got " + value.janitorClassName() + " [" + simpleClassNameOf(value)+ "].");
    }

}
