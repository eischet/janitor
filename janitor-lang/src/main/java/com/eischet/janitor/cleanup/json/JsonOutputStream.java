package com.eischet.janitor.cleanup.json;

import com.eischet.janitor.api.json.JsonException;
import org.eclipse.collections.api.stack.ImmutableStack;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.util.Date;

public interface JsonOutputStream {

    ImmutableStack<String> getPath();

    @NotNull JsonExportControls exportControls();

    void close() throws JsonException;
    void flush() throws JsonException;

    JsonOutputStream beginObject() throws JsonException;
    JsonOutputStream endObject() throws JsonException;

    JsonOutputStream beginArray() throws JsonException;
    JsonOutputStream endArray() throws JsonException;

    JsonOutputStream name(String name) throws JsonException;

    JsonOutputStream nullValue() throws JsonException;
    JsonOutputStream value(String value) throws JsonException;
    JsonOutputStream value(long value) throws JsonException;
    JsonOutputStream value(double value) throws JsonException;
    JsonOutputStream value(Number value) throws JsonException;
    JsonOutputStream value(boolean value) throws JsonException;

    default JsonOutputStream value(LocalDateTime value) throws JsonException {
        return value(DateTimeUtils.toJsonZulu(value));
    }

    default JsonOutputStream value(Date value) throws JsonException {
        return value(DateTimeUtils.toJsonZulu(DateTimeUtils.convert(value)));
    }

    default JsonOutputStream pair(final String name, String value) throws JsonException {
        // LATER: wenn die export controls sagen, dass leere Objekte nicht geliefert werden sollen, beide unterdrücken!
        return name(name).value(value);
    }

}
