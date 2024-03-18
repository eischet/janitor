package com.eischet.janitor.json;

import org.eclipse.collections.api.stack.ImmutableStack;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.util.Date;

public interface JsonProducer {

    ImmutableStack<String> getPath();

    @NotNull JsonExportControls exportControls();

    void close() throws JsonException;
    void flush() throws JsonException;

    JsonProducer beginObject() throws JsonException;
    JsonProducer endObject() throws JsonException;

    JsonProducer beginArray() throws JsonException;
    JsonProducer endArray() throws JsonException;

    JsonProducer name(String name) throws JsonException;

    JsonProducer nullValue() throws JsonException;
    JsonProducer value(String value) throws JsonException;
    JsonProducer value(long value) throws JsonException;
    JsonProducer value(double value) throws JsonException;
    JsonProducer value(Number value) throws JsonException;
    JsonProducer value(boolean value) throws JsonException;

    default JsonProducer value(LocalDateTime value) throws JsonException {
        return value(DateTimeUtils.toJsonZulu(value));
    }

    default JsonProducer value(Date value) throws JsonException {
        return value(DateTimeUtils.toJsonZulu(DateTimeUtils.convert(value)));
    }

    default JsonProducer pair(final String name, String value) throws JsonException {
        // LATER: wenn die export controls sagen, dass leere Objekte nicht geliefert werden sollen, beide unterdrücken!
        return name(name).value(value);
    }

}
