package com.eischet.janitor.api.types.dispatch;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.api.types.builtin.JList;

import java.util.ArrayList;
import java.util.List;

@FunctionalInterface
public interface ConverterFromJanitor<T> {


    T convertFromJanitor(JanitorScriptProcess process, JanitorObject janitorObject) throws JanitorRuntimeException;

    static <E> List<E> toList(final JanitorScriptProcess process, JList list, ConverterFromJanitor<E> converter) throws JanitorRuntimeException {
        final List<E> result = new ArrayList<>(list.size());
        for (final JanitorObject element : list) {
            final E converted = converter.convertFromJanitor(process, element);
            result.add(converted);
        }
        return result;
    }


}
