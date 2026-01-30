package com.eischet.janitor.api.types.dispatch;

import com.eischet.janitor.api.errors.glue.JanitorGlueException;
import com.eischet.janitor.toolbox.json.api.JsonException;
import com.eischet.janitor.toolbox.json.api.JsonInputStream;
import com.eischet.janitor.toolbox.json.api.JsonOutputStream;

public interface JsonAdapter<T> {
    void write(final JsonOutputStream stream, final T instance) throws JanitorGlueException, JsonException;

    void read(final JsonInputStream stream, final T instance) throws JanitorGlueException, JsonException;

    boolean isDefault(final T instance) throws JanitorGlueException;
}
