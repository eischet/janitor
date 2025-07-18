package com.eischet.janitor.api.types.dispatch;

import com.eischet.janitor.api.errors.glue.JanitorGlueException;
import com.eischet.janitor.api.errors.runtime.JanitorArgumentException;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.types.builtin.JFloat;
import com.eischet.janitor.api.types.builtin.JInt;
import com.eischet.janitor.api.types.builtin.JList;
import com.eischet.janitor.api.types.builtin.JNull;

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
    public static JInt requireInt(final JanitorObject value) throws JanitorGlueException {
        if (value instanceof JInt ok) {
            return ok;
        }
        throw new JanitorGlueException(JanitorArgumentException::fromGlue, "Expected an integer value but got " + value.janitorClassName() + ".");
    }


    public static JFloat requireFloat(final Object value) throws JanitorGlueException {
        if (value instanceof JFloat alreadyMatches) {
            return alreadyMatches;
        }
        if (value instanceof Number num) {
            return Janitor.getBuiltins().floatingPoint(num.doubleValue());
        }
        throw new JanitorGlueException(JanitorArgumentException::fromGlue, "Expected a  floating point value but got " + simpleClassNameOf(value) + ".");
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
}
