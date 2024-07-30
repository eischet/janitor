package com.eischet.janitor.toolbox.json.api;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@SuppressWarnings("UnusedReturnValue") // these are builder methods, stupid IDE
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


    default JsonOutputStream writeOptionalBooleanKeyValue(final @Nullable Boolean value, final @NotNull String key) throws JsonException {
        if (value != null) {
            this.key(key).value(value);
        }
        return this;
    }

    default JsonOutputStream writeOptionalStringKeyValue(final @Nullable String value, final @NotNull String key) throws JsonException {
        if (value != null) {
            this.key(key).value(value);
        }
        return this;
    }

    default JsonOutputStream writeOptionalNumberKeyValue(final @Nullable Number value, final @NotNull String key) throws JsonException {
        if (value != null) {
            this.key(key).value(value);
        }
        return this;
    }


    default JsonOutputStream writeOptional(@Nullable JsonExportable object, final @NotNull String key) throws JsonException {
        if (object != null && !object.isDefaultOrEmpty()) {
            this.key(key);
            object.writeJson(this);
        }
        return this;
    }

    default JsonOutputStream writeOptionalList(@Nullable List<? extends JsonExportable> list, @NotNull String key) throws JsonException {
        if (list != null && !list.isEmpty()) {
            this.key(key).beginArray();
            for (final JsonExportable item : list) {
                item.writeJson(this);
            }
            this.endArray();
        }
        return this;
    }

    default JsonOutputStream writeOptionalMappedKeyValue(final @Nullable JsonExportable.JsonOutputMapped value, final @NotNull String key) throws JsonException {
        if (value != null && !value.isOmittedInJson()) {
            this.key(key);
            value.writeAsJsonValue(this);
        }
        return this;
    }

}
