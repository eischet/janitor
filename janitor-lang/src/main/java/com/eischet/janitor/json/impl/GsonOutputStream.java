package com.eischet.janitor.json.impl;

import com.eischet.janitor.toolbox.json.api.JsonException;
import com.eischet.janitor.toolbox.json.api.JsonOutputStream;
import com.google.gson.FormattingStyle;
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

    public static GsonOutputStream prettyWriter(final OutputStreamWriter outputStreamWriter) {
        return new GsonStreamOut(JsonExportControls.pretty(), outputStreamWriter);
    }

    public static GsonStringOut stringWriter(final JsonExportControls jsonExportControls) {
        return new GsonStringOut(jsonExportControls);
    }

    protected final JsonExportControls jsonExportControls;
    protected final Writer sw;
    protected final JsonWriter jw;
    protected int level = 0;

    protected GsonOutputStream(final JsonExportControls jsonExportControls, final Writer sw) {
        this.jsonExportControls = jsonExportControls != null ? jsonExportControls : JsonExportControls.standard();
        this.sw = sw;
        this.jw = new JsonWriter(sw);

        if (jsonExportControls != null && jsonExportControls.isPretty()) {
            jw.setIndent("  ");
        }
    }

    @Override
    public boolean isOmitting(final Object object) {
        return jsonExportControls.isOmitting(object);
    }

    public @NotNull JsonExportControls exportControls() {
        return jsonExportControls;
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
    public JsonOutputStream pair(final String name, String value) throws JsonException {
        // LATER: wenn die export controls sagen, dass leere Objekte nicht geliefert werden sollen, beide unterdrücken!
        return key(name).value(value);
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
        try {
            jw.value(value);
            return this;
        } catch (IOException e) {
            throw new JsonException("error generating JSON", e);
        }
    }

    @Override
    public JsonOutputStream value(final Number value) throws JsonException {
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

    public static class GsonStringOut extends GsonOutputStream {

        public GsonStringOut(final JsonExportControls jsonExportControls) {
            super(jsonExportControls, new StringWriter());
        }

        public String getString() {
            return sw.toString();
        }

    }

    public static class GsonStreamOut extends GsonOutputStream {
        public GsonStreamOut(final JsonExportControls pretty, final OutputStreamWriter outputStreamWriter) {
            super(pretty, outputStreamWriter);
        }
    }

}
