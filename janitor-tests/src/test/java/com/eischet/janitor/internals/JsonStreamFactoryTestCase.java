// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.internals;

import com.eischet.janitor.JanitorTest;
import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.env.JanitorDefaultEnvironment;
import com.eischet.janitor.json.impl.GsonJsonStreams;
import com.eischet.janitor.runtime.JanitorFormattingGerman;
import com.eischet.janitor.toolbox.json.api.JsonException;
import com.eischet.janitor.toolbox.json.api.JsonExportableObject;
import com.eischet.janitor.toolbox.json.api.JsonInputStream;
import com.eischet.janitor.toolbox.json.api.JsonOutputStream;
import com.eischet.janitor.toolbox.json.api.JsonReadMode;
import com.eischet.janitor.toolbox.json.api.JsonStreamFactory;
import com.eischet.janitor.toolbox.json.api.JsonWriteOptions;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Checks how JSON stream factories are found and configured, and the JSON helpers in {@link Janitor}.
 */
public class JsonStreamFactoryTestCase extends JanitorTest {

    /** Exportable only, not declared as a JsonWriter: must still be accepted by Janitor.writeJson. */
    private record Point(int x, int y) implements JsonExportableObject {
        @Override
        public boolean isDefaultOrEmpty() {
            return false;
        }

        @Override
        public void writeJson(final JsonOutputStream producer) throws JsonException {
            producer.beginObject().key("x").value(x).key("y").value(y).endObject();
        }
    }

    /** Delegates to Gson and counts the streams it creates. */
    private static final class CountingStreams implements JsonStreamFactory {
        private final JsonStreamFactory delegate = new GsonJsonStreams();
        private final AtomicInteger streams = new AtomicInteger();

        @Override
        public @NotNull String name() {
            return "counting";
        }

        @Override
        public @NotNull JsonInputStream reader(final @NotNull String json, final @NotNull JsonReadMode mode) {
            streams.incrementAndGet();
            return delegate.reader(json, mode);
        }

        @Override
        public @NotNull JsonInputStream reader(final @NotNull Reader source, final @NotNull JsonReadMode mode) {
            streams.incrementAndGet();
            return delegate.reader(source, mode);
        }

        @Override
        public @NotNull JsonOutputStream writer(final @NotNull JsonWriteOptions options, final @NotNull Writer target) {
            streams.incrementAndGet();
            return delegate.writer(options, target);
        }
    }

    @Test
    public void discovery() {
        assertEquals(GsonJsonStreams.NAME, JsonStreamFactory.defaultFactory().name());
        assertTrue(JsonStreamFactory.available().stream().anyMatch(f -> f.name().equals(GsonJsonStreams.NAME)));
        assertSame(JsonStreamFactory.defaultFactory(), JsonStreamFactory.named(GsonJsonStreams.NAME));
        final IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> JsonStreamFactory.named("nope"));
        assertTrue(e.getMessage().contains(GsonJsonStreams.NAME), e.getMessage());
    }

    @Test
    public void writeHelpers() throws JsonException {
        assertEquals("{\"a\":\"x\"}", Janitor.writeJson(out -> out.beginObject().pair("a", "x").endObject()));
        assertEquals("{\n  \"a\": \"x\"\n}", Janitor.writeJson(JsonWriteOptions.PRETTY, out -> out.beginObject().pair("a", "x").endObject()));
        assertEquals("{\"x\":1,\"y\":2}", Janitor.writeJson(new Point(1, 2)));
        final Point point = new Point(3, 4);
        assertEquals("{\"x\":3,\"y\":4}", Janitor.writeJson(JsonWriteOptions.COMPACT, point::writeJson));
    }

    @Test
    public void writingToWriterLeavesItOpen() throws JsonException {
        final boolean[] closed = {false};
        final StringWriter target = new StringWriter() {
            @Override
            public void close() {
                closed[0] = true;
            }
        };
        Janitor.writeJson(JsonWriteOptions.COMPACT, target, new Point(1, 2));
        Janitor.writeJson(JsonWriteOptions.COMPACT, target, new Point(3, 4));
        assertFalse(closed[0]);
        assertEquals("{\"x\":1,\"y\":2}{\"x\":3,\"y\":4}", target.toString());
    }

    private static long readA(final JsonInputStream in) throws JsonException {
        in.beginObject();
        assertEquals("a", in.nextKey());
        final long value = in.nextLong();
        in.endObject();
        return value;
    }

    @Test
    public void readModes() throws JsonException {
        final long[] result = {0};
        Janitor.readJson("{'a': 17 /* comment */}", in -> result[0] = readA(in)); // no trailing comma: Gson rejects it
        assertEquals(17, result[0]);
        Janitor.readJson(JsonReadMode.STRICT, new StringReader("{\"a\": 18}"), in -> result[0] = readA(in));
        assertEquals(18, result[0]);
        assertThrows(JsonException.class, () -> Janitor.readJson(JsonReadMode.STRICT, "{'a': 19}", JsonStreamFactoryTestCase::readA));
    }

    @Test
    public void environmentChoosesFactory() throws JsonException {
        final CountingStreams counting = new CountingStreams();
        final JanitorDefaultEnvironment env = new JanitorDefaultEnvironment(new JanitorFormattingGerman()) {
            @Override
            public void warn(final String message) {
                System.err.println("WARN: " + message);
            }

            @Override
            protected @NotNull JsonStreamFactory createJsonStreamFactory() {
                return counting;
            }
        };
        assertSame(counting, env.getJsonStreams());
        assertSame(counting, env.getJsonStreams());
        assertEquals("{\"x\":1,\"y\":2}", env.writeJson(new Point(1, 2)));
        assertEquals(17, readA(env.getLenientJsonConsumer("{a: 17}")));
        assertEquals(2, counting.streams.get());
    }

}
