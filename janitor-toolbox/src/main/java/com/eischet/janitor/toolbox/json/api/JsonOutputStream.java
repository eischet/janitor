package com.eischet.janitor.toolbox.json.api;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

public interface JsonOutputStream {

    boolean isOmitting(final Object object);

    void close() throws JsonException;
    void flush() throws JsonException;

    JsonOutputStream beginObject() throws JsonException;
    JsonOutputStream endObject() throws JsonException;

    JsonOutputStream beginArray() throws JsonException;
    JsonOutputStream endArray() throws JsonException;

    JsonOutputStream key(String key) throws JsonException; // GSON calls this "name", but I prefer "key"

    JsonOutputStream nullValue() throws JsonException;
    JsonOutputStream value(String value) throws JsonException;
    JsonOutputStream value(long value) throws JsonException;
    JsonOutputStream value(double value) throws JsonException;
    JsonOutputStream value(Number value) throws JsonException;
    JsonOutputStream value(boolean value) throws JsonException;

    JsonOutputStream value(LocalDateTime value) throws JsonException;
    JsonOutputStream value(Date value) throws JsonException;
    JsonOutputStream pair(final String name, String value) throws JsonException;



    static void writeOptionalBooleanKeyValue(@NotNull final JsonOutputStream producer, final @Nullable Boolean value, final @NotNull String key) throws JsonException {
        if (value != null) {
            producer.key(key).value(value);
        }
    }

    static void writeOptionalStringKeyValue(@NotNull final JsonOutputStream producer, final @Nullable String value, final @NotNull String key) throws JsonException {
        if (value != null) {
            producer.key(key).value(value);
        }
    }

    static void writeOptionalNumberKeyValue(@NotNull final JsonOutputStream producer, final @Nullable Number value, final @NotNull String key) throws JsonException {
        if (value != null) {
            producer.key(key).value(value);
        }
    }


    static void writeOptional(@NotNull JsonOutputStream producer, @Nullable JsonExportable object, final @NotNull String key) throws JsonException {
        if (object != null && !object.isDefaultOrEmpty()) {
            producer.key(key);
            object.writeJson(producer);
        }
    }

    static void writeOptionalList(@NotNull JsonOutputStream producer, @Nullable List<? extends JsonExportable> list, @NotNull String key) throws JsonException {
        if (list != null && !list.isEmpty()) {
            producer.key(key);
            producer.beginArray();
            for (final JsonExportable item : list) {
                item.writeJson(producer);
            }
            producer.endArray();
        }
    }

    static void writeOptionalMappedKeyValue(final @NotNull JsonOutputStream producer, final @Nullable JsonExportable.JsonOutputMapped value, final @NotNull String key) throws JsonException {
        if (value != null && !value.isOmittedInJson()) {
            producer.key(key);
            value.writeAsJsonValue(producer);
        }
    }





}
