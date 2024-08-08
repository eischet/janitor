package com.eischet.janitor.api.types.dispatch;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.api.types.builtin.JList;
import com.eischet.janitor.api.types.builtin.JNull;

import java.util.List;

/**
 * A functional interface for converting a value to a JanitorObject.
 * @param <T>
 */
@FunctionalInterface
public interface ConverterToJanitor<T> {

    /**
     * Convert the given value to a JanitorObject.
     * @param value the value to convert
     * @return the JanitorObject
     */
    JanitorObject convertToJanitor(JanitorScriptProcess process, T value) throws JanitorRuntimeException;

    static <E> JanitorObject toJanitorList(JanitorScriptProcess process, List<E> list, ConverterToJanitor<E> converter) throws JanitorRuntimeException {
        if (list == null) {
            return JNull.NULL;
        }
        if (list.isEmpty()) {
            return process.getBuiltins().list();
        }
        final JList result = process.getBuiltins().list();
        for (final E element : list) {
            result.add(converter.convertToJanitor(process, element));
        }
        return result;
    }

}
