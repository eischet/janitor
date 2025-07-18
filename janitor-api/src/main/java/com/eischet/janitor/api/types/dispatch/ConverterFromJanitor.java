package com.eischet.janitor.api.types.dispatch;

import com.eischet.janitor.api.errors.glue.JanitorGlueException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.types.JanitorObject;

@FunctionalInterface
public interface ConverterFromJanitor<T> {

    T convertFromJanitor(JanitorObject janitorObject) throws JanitorGlueException;


}
