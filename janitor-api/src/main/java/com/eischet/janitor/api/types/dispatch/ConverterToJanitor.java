package com.eischet.janitor.api.types.dispatch;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.glue.JanitorGlueException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.types.JanitorObject;

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
    JanitorObject convertToJanitor(T value) throws JanitorGlueException;

}
