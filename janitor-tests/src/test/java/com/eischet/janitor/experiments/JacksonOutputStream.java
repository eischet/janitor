// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.experiments;

import com.eischet.janitor.json.impl.DateTimeUtils;
import com.eischet.janitor.toolbox.json.api.JsonException;
import com.eischet.janitor.toolbox.json.api.JsonOutputStream;
import com.eischet.janitor.toolbox.json.api.JsonWriteOptions;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.time.LocalDateTime;
import java.util.Date;

public class JacksonOutputStream implements JsonOutputStream {

    /*
    public static JacksonOutputStream prettyWriter(final OutputStreamWriter outputStreamWriter) {
        return new GsonOutputStream.GsonStreamOut(JsonWriteOptions.PRETTY, outputStreamWriter);
    }

    public static GsonOutputStream.GsonStringOut stringWriter(final JsonWriteOptions options) {
        return new GsonOutputStream.GsonStringOut(options);
    }

     */

    protected final JsonWriteOptions options;
    protected final Writer sw;
    private final JsonFactory factory;
    private final JsonGenerator generator;
    protected int level = 0;

    protected JacksonOutputStream(final JsonWriteOptions options, final Writer sw) throws IOException {
        this.factory = JsonFactory.builder().build(); // LATER: options...
        this.options = options != null ? options : JsonWriteOptions.COMPACT;
        this.sw = sw;
        // if (options != null && options.isPretty()) {
            // jw.setIndent("  ");
        //}
        this.generator = factory.createGenerator(sw);
    }

    @Override
    public @NotNull JsonWriteOptions options() {
        return options;
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
            generator.close();
            sw.close();
        } catch (IOException e) {
            throw new JsonException("error closing gson writer", e);
        }
    }

    @Override
    public JsonOutputStream beginObject() throws JsonException {
        try {
            generator.writeStartObject();
            return this;
        } catch (IOException e) {
            throw new JsonException("error generating JSON", e);
        }
    }


    @Override
    public JsonOutputStream endObject() throws JsonException {
        try {
            generator.writeEndObject();
            return this;
        } catch (IOException e) {
            throw new JsonException("error generating JSON", e);
        }
    }


    @Override
    public JsonOutputStream key(final String key) throws JsonException {
        try {
            generator.writeFieldName(key);
            return this;
        } catch (IOException e) {
            throw new JsonException("error generating JSON", e);
        }
    }

    @Override
    public JsonOutputStream value(final String value) throws JsonException {
        try {
            generator.writeString(value);
            return this;
        } catch (IOException e) {
            throw new JsonException("error generating JSON", e);
        }
    }


    @Override
    public JsonOutputStream nullValue() throws JsonException {
        try {
            generator.writeNull();
            return this;
        } catch (IOException e) {
            throw new JsonException("error generating JSON", e);
        }
    }

    @Override
    public JsonOutputStream value(final long value) throws JsonException {
        try {
            generator.writeNumber(value);
            return this;
        } catch (IOException e) {
            throw new JsonException("error generating JSON", e);
        }
    }

    @Override
    public JsonOutputStream value(final double value) throws JsonException {
        try {
            generator.writeNumber(value);
            return this;
        } catch (IOException e) {
            throw new JsonException("error generating JSON", e);
        }
    }

    @Override
    public JsonOutputStream value(final Number value) throws JsonException {
        try {
            if (value instanceof Integer) {
                generator.writeNumber(value.intValue());
            } else if (value instanceof Long) {
                generator.writeNumber(value.longValue());
            } else if (value instanceof Double) {
                generator.writeNumber(value.doubleValue());
            } else if (value instanceof Float) {
                generator.writeNumber(value.doubleValue());
            } else {
                generator.writeNumber(value.doubleValue());
            }
            return this;
        } catch (IOException e) {
            throw new JsonException("error generating JSON", e);
        }
    }

    @Override
    public JsonOutputStream value(final boolean value) throws JsonException {
        try {
            generator.writeBoolean(value);
            return this;
        } catch (IOException e) {
            throw new JsonException("error generating JSON", e);
        }
    }

    @Override
    public JsonOutputStream beginArray() throws JsonException {
        try {
            generator.writeStartArray();
            return this;
        } catch (IOException e) {
            throw new JsonException("error generating JSON", e);
        }

    }

    @Override
    public JsonOutputStream endArray() throws JsonException {
        try {
            generator.writeEndArray();
            return this;
        } catch (IOException e) {
            throw new JsonException("error generating JSON", e);
        }
    }

    @Override
    public void flush() throws JsonException {
        try {
            generator.flush();
        } catch (IOException e) {
            throw new JsonException("error generating JSON", e);
        }
    }

    public static class JacksonStringOut extends JacksonOutputStream {

        public JacksonStringOut(final JsonWriteOptions options) throws IOException {
            super(options, new StringWriter());
        }

        public String getString() {
            return sw.toString();
        }

    }

    public static class JacksonStreamOut extends JacksonOutputStream {
        public JacksonStreamOut(final JsonWriteOptions pretty, final OutputStreamWriter outputStreamWriter) throws IOException {
            super(pretty, outputStreamWriter);
        }
    }

}
