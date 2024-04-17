package com.eischet.janitor.api.json.api;

import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.util.Date;

public interface JsonOutputStream {

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

    JsonOutputStream value(LocalDateTime value) throws JsonException;
    JsonOutputStream value(Date value) throws JsonException;
    JsonOutputStream pair(final String name, String value) throws JsonException;

}
