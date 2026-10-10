package com.eischet.janitor.toolbox.json.api;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

/** A push-style writer for JSON documents. */
@SuppressWarnings("UnusedReturnValue") // these are builder methods, stupid IDE
public interface JsonOutputStream {

    /**
     * Checks whether an object is configured to be left out of the output.
     * @param object the object, e.g. a field
     * @return true if the object is omitted
     */
    boolean isOmitting(final Object object);

    /**
     * Closes the writer.
     * @throws JsonException on errors
     */
    void close() throws JsonException;
    /**
     * Flushes the writer.
     * @throws JsonException on errors
     */
    void flush() throws JsonException;

    /**
     * Writes the start of an object.
     * @return this
     * @throws JsonException on errors
     */
    JsonOutputStream beginObject() throws JsonException;
    /**
     * Writes the end of an object.
     * @return this
     * @throws JsonException on errors
     */
    JsonOutputStream endObject() throws JsonException;

    /**
     * Writes the start of an array.
     * @return this
     * @throws JsonException on errors
     */
    JsonOutputStream beginArray() throws JsonException;
    /**
     * Writes the end of an array.
     * @return this
     * @throws JsonException on errors
     */
    JsonOutputStream endArray() throws JsonException;

    /**
     * Writes the name of the next property.
     * @param key the name
     * @return this
     * @throws JsonException on errors
     */
    JsonOutputStream key(String key) throws JsonException; // GSON calls this "name", but I prefer "key"

    /**
     * Writes a null value.
     * @return this
     * @throws JsonException on errors
     */
    JsonOutputStream nullValue() throws JsonException;
    /**
     * Writes a value.
     * @param value the value
     * @return this
     * @throws JsonException on errors
     */
    JsonOutputStream value(String value) throws JsonException;
    /**
     * Writes a value.
     * @param value the value
     * @return this
     * @throws JsonException on errors
     */
    JsonOutputStream value(long value) throws JsonException;
    /**
     * Writes a value.
     * @param value the value
     * @return this
     * @throws JsonException on errors
     */
    JsonOutputStream value(double value) throws JsonException;
    /**
     * Writes a value.
     * @param value the value
     * @return this
     * @throws JsonException on errors
     */
    JsonOutputStream value(Number value) throws JsonException;
    /**
     * Writes a value.
     * @param value the value
     * @return this
     * @throws JsonException on errors
     */
    JsonOutputStream value(boolean value) throws JsonException;

    /**
     * Writes a value.
     * @param value the value
     * @return this
     * @throws JsonException on errors
     */
    JsonOutputStream value(LocalDateTime value) throws JsonException;
    /**
     * Writes a value.
     * @param value the value
     * @return this
     * @throws JsonException on errors
     */
    JsonOutputStream value(Date value) throws JsonException;
    /**
     * Writes a property with a string value.
     * @param name the name of the property
     * @param value the value
     * @return this
     * @throws JsonException on errors
     */
    JsonOutputStream pair(final String name, String value) throws JsonException;


    /**
     * Writes a property, unless the value is null.
     * @param key the name of the property
     * @param value the value, may be null
     * @return this
     * @throws JsonException on errors
     */
    default JsonOutputStream optional(final @NotNull String key, final @Nullable Boolean value) throws JsonException {
        if (value != null) {
            this.key(key).value(value);
        }
        return this;
    }

    /**
     * Writes a property, unless the value is null.
     * @param key the name of the property
     * @param value the value, may be null
     * @return this
     * @throws JsonException on errors
     */
    default JsonOutputStream optional(final @NotNull String key, final @Nullable String value) throws JsonException {
        if (value != null) {
            this.key(key).value(value);
        }
        return this;
    }

    /**
     * Writes a property, unless the value is null.
     * @param key the name of the property
     * @param value the value, may be null
     * @return this
     * @throws JsonException on errors
     */
    default JsonOutputStream optional(final @NotNull String key, final @Nullable Number value) throws JsonException {
        if (value != null) {
            this.key(key).value(value);
        }
        return this;
    }


    /**
     * Writes a property, unless the object is null, default or empty.
     * @param key the name of the property
     * @param object the object, may be null
     * @return this
     * @throws JsonException on errors
     */
    default JsonOutputStream optional(final @NotNull String key, @Nullable JsonExportable object) throws JsonException {
        if (object != null && !object.isDefaultOrEmpty()) {
            this.key(key);
            object.writeJson(this);
        }
        return this;
    }

    /**
     * Writes a property that is an array, unless the list is null or empty.
     * @param key the name of the property
     * @param list the list, may be null
     * @return this
     * @throws JsonException on errors
     */
    default JsonOutputStream optional(@NotNull String key, @Nullable List<? extends JsonExportable> list) throws JsonException {
        if (list != null && !list.isEmpty()) {
            this.key(key).beginArray();
            for (final JsonExportable item : list) {
                item.writeJson(this);
            }
            this.endArray();
        }
        return this;
    }

    /**
     * Writes a property, unless the value is null or says that it should be omitted.
     * @param key the name of the property
     * @param value the value, may be null
     * @return this
     * @throws JsonException on errors
     */
    default JsonOutputStream optional(final @NotNull String key, final @Nullable JsonExportable.JsonOutputMapped value) throws JsonException {
        if (value != null && !value.isOmittedInJson()) {
            this.key(key);
            value.writeAsJsonValue(this);
        }
        return this;
    }

}
