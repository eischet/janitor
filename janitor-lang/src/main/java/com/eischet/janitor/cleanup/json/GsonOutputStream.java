package com.eischet.janitor.cleanup.json;

import com.eischet.janitor.api.json.JsonException;
import com.google.gson.stream.JsonWriter;
import org.eclipse.collections.api.factory.Stacks;
import org.eclipse.collections.api.stack.ImmutableStack;
import org.eclipse.collections.api.stack.MutableStack;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;

public abstract class GsonOutputStream implements JsonOutputStream {

    private static final Logger log = LoggerFactory.getLogger(GsonOutputStream.class);
    protected final JsonExportControls jsonExportControls;
    protected final Writer sw;
    protected final JsonWriter jw;
    protected final MutableStack<String> path = Stacks.mutable.empty();
    protected int level = 0;
    protected int lastNameLevel = -1;

    public static boolean STACKING_LOG = false;

    @Override
    public @NotNull JsonExportControls exportControls() {
        return jsonExportControls;
    }

    protected GsonOutputStream(final JsonExportControls jsonExportControls, final Writer sw) {
        this.jsonExportControls = jsonExportControls != null ? jsonExportControls : JsonExportControls.standard();
        this.sw = sw;
        this.jw = new JsonWriter(sw);
        if (jsonExportControls != null && jsonExportControls.isPretty()) {
            jw.setIndent("  ");
        }
    }

    public static GsonOutputStream prettyWriter(final OutputStreamWriter outputStreamWriter) {
        return new GsonStreamOut(JsonExportControls.pretty(), outputStreamWriter);
    }

    public static GsonStringOut stringWriter(final JsonExportControls jsonExportControls) {
        return new GsonStringOut(jsonExportControls);
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
            enter();
            return this;
        } catch (IOException e) {
            throw new JsonException("error generating JSON", e);
        }
    }


    @Override
    public JsonOutputStream endObject() throws JsonException {
        try {
            jw.endObject();
            leave();
            return this;
        } catch (IOException e) {
            throw new JsonException("error generating JSON", e);
        }
    }


    @Override
    public JsonOutputStream name(final String name) throws JsonException {
        try {
            jw.name(name);
            pushName(name);
            return this;
        } catch (IOException e) {
            throw new JsonException("error generating JSON", e);
        }
    }

    @Override
    public JsonOutputStream value(final String value) throws JsonException {
        try {
            jw.value(value);
            popName();
            return this;
        } catch (IOException e) {
            throw new JsonException("error generating JSON", e);
        }
    }


    @Override
    public JsonOutputStream nullValue() throws JsonException {
        try {
            jw.nullValue();
            popName();
            return this;
        } catch (IOException e) {
            throw new JsonException("error generating JSON", e);
        }
    }

    @Override
    public JsonOutputStream value(final long value) throws JsonException {
        try {
            jw.value(value);
            popName();
            return this;
        } catch (IOException e) {
            throw new JsonException("error generating JSON", e);
        }
    }

    @Override
    public JsonOutputStream value(final double value) throws JsonException {
        try {
            jw.value(value);
            popName();
            return this;
        } catch (IOException e) {
            throw new JsonException("error generating JSON", e);
        }
    }

    @Override
    public JsonOutputStream value(final Number value) throws JsonException {
        try {
            jw.value(value);
            popName();
            return this;
        } catch (IOException e) {
            throw new JsonException("error generating JSON", e);
        }
    }

    @Override
    public JsonOutputStream value(final boolean value) throws JsonException {
        try {
            jw.value(value);
            popName();
            return this;
        } catch (IOException e) {
            throw new JsonException("error generating JSON", e);
        }
    }

    @Override
    public JsonOutputStream beginArray() throws JsonException {
        try {
            jw.beginArray();
            enter();
            return this;
        } catch (IOException e) {
            throw new JsonException("error generating JSON", e);
        }

    }

    @Override
    public JsonOutputStream endArray() throws JsonException {
        try {
            jw.endArray();
            leave();
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

    @Override
    public ImmutableStack<String> getPath() {
        return path.toImmutable();
    }

    private void enter() {
        ++level;
        if (STACKING_LOG) {
            log.info("entered level {} at {}", level, path);
        }
    }

    private void leave() {
        --level;
        if (STACKING_LOG) {
            log.info("exited to level {} at {}", level, path);
        }
    }

    private void pushName(final String name) {
        lastNameLevel = level;
        path.push(name);
        if (STACKING_LOG) {
            log.info("at level {}, path {}", level, path);
        }
    }


    private void popName() {
        if (lastNameLevel == level) {
            if (STACKING_LOG) {
                log.info("popping previous name at level {}", level);
            }
            path.pop();
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
