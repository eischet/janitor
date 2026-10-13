// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.json.impl;

import com.eischet.janitor.toolbox.json.api.JsonException;
import com.eischet.janitor.toolbox.json.api.JsonOutputStream;
import com.eischet.janitor.toolbox.json.api.JsonWriteOptions;
import com.google.gson.stream.JsonWriter;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * JsonOutputStream implementation using Gson.
 */
public abstract class GsonOutputStream implements JsonOutputStream {

    /**
     * Creates a writer that produces indented JSON.
     * @param outputStreamWriter the target
     * @return the writer
     */
    public static GsonOutputStream prettyWriter(final OutputStreamWriter outputStreamWriter) {
        return new GsonStreamOut(JsonWriteOptions.PRETTY, outputStreamWriter);
    }

    /**
     * Creates a writer that produces JSON in memory.
     * @param options controls how the JSON is produced
     * @return the writer
     */
    public static GsonStringOut stringWriter(final JsonWriteOptions options) {
        return new GsonStringOut(options);
    }

    protected final JsonWriteOptions options;
    protected final Writer sw;
    protected final JsonWriter jw;
    protected int level = 0;

    protected GsonOutputStream(final JsonWriteOptions options, final Writer sw) {
        this.options = options != null ? options : JsonWriteOptions.COMPACT;
        this.sw = sw;
        this.jw = new JsonWriter(sw);
        jw.setIndent(this.options.indent());
    }

    @Override
    public @NotNull JsonWriteOptions options() {
        return options;
    }

    /**
     * Writes a NaN or infinite number as the options say.
     * @param value the number
     * @return this
     * @throws JsonException if the options say that such numbers are not allowed
     */
    private JsonOutputStream nonFinite(final Number value) throws JsonException {
        return switch (options.nonFinite()) {
            case FAIL -> throw new JsonException("JSON cannot represent the number " + value);
            case NULL -> nullValue();
            case STRING -> value(value.toString());
        };
    }

    private static boolean isNonFinite(final Number value) {
        return (value instanceof Double d && !Double.isFinite(d)) || (value instanceof Float f && !Float.isFinite(f));
    }

    @Override
    public JsonOutputStream value(LocalDateTime value) throws JsonException {
        return value(DateTimeUtils.toJsonZulu(value));
    }

    @Override
    public JsonOutputStream value(Date value) throws JsonException {
        return value(DateTimeUtils.toJsonZulu(DateTimeUtils.convert(value)));
    }

    @Override
    public void close() throws JsonException {
        try {
            jw.close();
            sw.close();
        } catch (IOException e) {
            throw new JsonException("error closing gson writer", e);
        }
    }

    @Override
    public JsonOutputStream beginObject() throws JsonException {
        try {
            jw.beginObject();
            return this;
        } catch (IOException e) {
            throw new JsonException("error generating JSON", e);
        }
    }


    @Override
    public JsonOutputStream endObject() throws JsonException {
        try {
            jw.endObject();
            return this;
        } catch (IOException e) {
            throw new JsonException("error generating JSON", e);
        }
    }


    @Override
    public JsonOutputStream key(final String key) throws JsonException {
        try {
            jw.name(key);
            return this;
        } catch (IOException e) {
            throw new JsonException("error generating JSON", e);
        }
    }

    @Override
    public JsonOutputStream value(final String value) throws JsonException {
        try {
            jw.value(value);
            return this;
        } catch (IOException e) {
            throw new JsonException("error generating JSON", e);
        }
    }


    @Override
    public JsonOutputStream nullValue() throws JsonException {
        try {
            jw.nullValue();
            return this;
        } catch (IOException e) {
            throw new JsonException("error generating JSON", e);
        }
    }

    @Override
    public JsonOutputStream value(final long value) throws JsonException {
        try {
            jw.value(value);
            return this;
        } catch (IOException e) {
            throw new JsonException("error generating JSON", e);
        }
    }

    @Override
    public JsonOutputStream value(final double value) throws JsonException {
        if (!Double.isFinite(value)) {
            return nonFinite(value);
        }
        try {
            jw.value(value);
            return this;
        } catch (IOException e) {
            throw new JsonException("error generating JSON", e);
        }
    }

    @Override
    public JsonOutputStream value(final Number value) throws JsonException {
        if (isNonFinite(value)) {
            return nonFinite(value);
        }
        try {
            jw.value(value);
            return this;
        } catch (IOException e) {
            throw new JsonException("error generating JSON", e);
        }
    }

    @Override
    public JsonOutputStream value(final boolean value) throws JsonException {
        try {
            jw.value(value);
            return this;
        } catch (IOException e) {
            throw new JsonException("error generating JSON", e);
        }
    }

    @Override
    public JsonOutputStream beginArray() throws JsonException {
        try {
            jw.beginArray();
            return this;
        } catch (IOException e) {
            throw new JsonException("error generating JSON", e);
        }

    }

    @Override
    public JsonOutputStream endArray() throws JsonException {
        try {
            jw.endArray();
            return this;
        } catch (IOException e) {
            throw new JsonException("error generating JSON", e);
        }
    }

    @Override
    public void flush() throws JsonException {
        try {
            jw.flush();
        } catch (IOException e) {
            throw new JsonException("error generating JSON", e);
        }
    }

    /** A JSON output stream that writes to a string. */
    public static class GsonStringOut extends GsonOutputStream {

        public GsonStringOut(final JsonWriteOptions options) {
            super(options, new StringWriter());
        }

        /**
         * @return the JSON that was written so far
         */
        public String getString() {
            return sw.toString();
        }

    }

    /** A JSON output stream that writes to a {@link Writer}. */
    public static class GsonStreamOut extends GsonOutputStream {
        public GsonStreamOut(final JsonWriteOptions options, final Writer target) {
            super(options, target);
        }
    }

}
