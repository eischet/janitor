// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.benchmarks;

import com.eischet.janitor.toolbox.json.api.JsonException;
import com.eischet.janitor.toolbox.json.api.JsonInputStream;
import com.eischet.janitor.toolbox.json.api.JsonOutputStream;
import com.eischet.janitor.toolbox.json.api.JsonReadMode;
import com.eischet.janitor.toolbox.json.api.JsonStreamFactory;
import com.eischet.janitor.toolbox.json.api.JsonWriteOptions;
import com.eischet.janitor.toolbox.json.api.JsonWriter;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Compares the JSON stream implementations when reading and writing the same document.
 * Reading consumes values the way Janitor does it: strings with nextString(), numbers with nextDouble().
 * <p>
 * Run with {@code mvn -pl janitor-tests -am -Pbench -DskipTests test "-Djmh.args=JsonStreamsBenchmark"}.
 * </p>
 */
@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(1)
public class JsonStreamsBenchmark {

    @Param({"gson", "native"})
    public String impl;

    /** The number of records in the document: about 250 bytes each. */
    @Param({"10", "5000"})
    public int records;

    private JsonStreamFactory json;
    private Object document;
    private String text;

    @Setup
    public void setup() throws JsonException {
        json = JsonStreamFactory.named(impl);
        document = createDocument(records);
        text = JsonStreamFactory.named("gson").write(JsonWriteOptions.COMPACT, out -> writeValue(out, document));
    }

    /** Creates records with typical content: names, numbers, booleans, nulls, a date, a nested object, and some escapes. */
    private static Object createDocument(final int records) {
        final List<Object> list = new ArrayList<>();
        for (int i = 0; i < records; i++) {
            final Map<String, Object> record = new LinkedHashMap<>();
            record.put("id", (long) i);
            record.put("name", "Item " + i);
            record.put("description", "A \"quoted\" description\twith a tab, a line break\nand some non-ASCII: äöü € " + i);
            record.put("price", i * 1.25);
            record.put("active", i % 2 == 0);
            record.put("tags", List.of("alpha", "beta", "gamma"));
            record.put("created", "2026-10-16T12:34:56.789Z");
            final Map<String, Object> nested = new LinkedHashMap<>();
            nested.put("x", (long) i * 1000);
            nested.put("y", null);
            nested.put("z", List.of(1.5, -2.25e10, 0.001));
            record.put("nested", nested);
            list.add(record);
        }
        return list;
    }

    private static void writeValue(final JsonOutputStream out, final Object value) throws JsonException {
        switch (value) {
            case null -> out.nullValue();
            case String string -> out.value(string);
            case Boolean bool -> out.value(bool.booleanValue());
            case Long number -> out.value(number.longValue());
            case Double number -> out.value(number.doubleValue());
            case List<?> list -> {
                out.beginArray();
                for (final Object item : list) {
                    writeValue(out, item);
                }
                out.endArray();
            }
            case Map<?, ?> map -> {
                out.beginObject();
                for (final Map.Entry<?, ?> entry : map.entrySet()) {
                    out.key((String) entry.getKey());
                    writeValue(out, entry.getValue());
                }
                out.endObject();
            }
            default -> throw new IllegalArgumentException("cannot write " + value.getClass());
        }
    }

    private static void readValue(final JsonInputStream in, final Blackhole bh) throws JsonException {
        switch (in.peek()) {
            case BEGIN_ARRAY -> {
                in.beginArray();
                while (in.hasNext()) {
                    readValue(in, bh);
                }
                in.endArray();
            }
            case BEGIN_OBJECT -> {
                in.beginObject();
                while (in.hasNext()) {
                    bh.consume(in.nextKey());
                    readValue(in, bh);
                }
                in.endObject();
            }
            case STRING -> bh.consume(in.nextString());
            case NUMBER -> bh.consume(in.nextDouble());
            case BOOLEAN -> bh.consume(in.nextBoolean());
            case NULL -> in.nextNull();
            default -> throw new IllegalStateException("unexpected token " + in.peek());
        }
    }

    @Benchmark
    public void readString(final Blackhole bh) throws JsonException {
        final JsonInputStream in = json.reader(text, JsonReadMode.STRICT);
        readValue(in, bh);
        in.close();
    }

    @Benchmark
    public void readReader(final Blackhole bh) throws JsonException {
        final JsonInputStream in = json.reader(new StringReader(text), JsonReadMode.STRICT);
        readValue(in, bh);
        in.close();
    }

    @Benchmark
    public String write() throws JsonException {
        final JsonWriter writer = out -> writeValue(out, document);
        return json.write(JsonWriteOptions.COMPACT, writer);
    }

}
