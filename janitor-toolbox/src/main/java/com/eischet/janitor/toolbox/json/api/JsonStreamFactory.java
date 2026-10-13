// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.toolbox.json.api;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.List;

/**
 * Creates JSON input and output streams, hiding which JSON library does the actual work.
 * <p>
 * Implementations are discovered with the {@link java.util.ServiceLoader}. Unless the system property
 * {@value #SYSTEM_PROPERTY} names one of them, the one with the highest {@link #priority()} is the default.
 * Given the same options, all implementations must produce the same JSON.
 * </p>
 */
public interface JsonStreamFactory {

    /** The system property that selects the default implementation by its {@link #name()}, e.g. "gson". */
    String SYSTEM_PROPERTY = "janitor.json";

    /**
     * @return the default implementation, chosen once per JVM
     * @throws IllegalStateException if no implementation is available, or the system property names an unknown one
     */
    static @NotNull JsonStreamFactory defaultFactory() {
        return JsonStreamRegistry.defaultFactory();
    }

    /**
     * Looks up an implementation by its name.
     * @param name the name, e.g. "gson"
     * @return the implementation
     * @throws IllegalArgumentException if there is no such implementation; the message lists the available ones
     */
    static @NotNull JsonStreamFactory named(final @NotNull String name) {
        return JsonStreamRegistry.named(name);
    }

    /**
     * @return all implementations on the class path, e.g. for comparing them
     */
    static @NotNull @Unmodifiable List<JsonStreamFactory> available() {
        return JsonStreamRegistry.available();
    }

    /**
     * @return a short, unique name, e.g. "gson"
     */
    @NotNull String name();

    /**
     * @return the priority when choosing the default implementation; higher wins
     */
    default int priority() {
        return 0;
    }

    /**
     * Creates a stream that reads JSON text.
     * @param json the JSON text
     * @param mode how strictly to parse
     * @return the stream
     */
    @NotNull JsonInputStream reader(@NotNull String json, @NotNull JsonReadMode mode);

    /**
     * Creates a stream that reads JSON from a reader. Closing the stream closes the reader.
     * @param source the source
     * @param mode how strictly to parse
     * @return the stream
     */
    @NotNull JsonInputStream reader(@NotNull Reader source, @NotNull JsonReadMode mode);

    /**
     * Creates a stream that writes JSON to a writer. Closing the stream closes the writer.
     * @param options controls how JSON is written
     * @param target the target
     * @return the stream
     */
    @NotNull JsonOutputStream writer(@NotNull JsonWriteOptions options, @NotNull Writer target);

    /**
     * Writes JSON to a string.
     * @param options controls how JSON is written
     * @param writer writes the content
     * @return the JSON text
     * @throws JsonException on errors
     */
    default @NotNull String write(final @NotNull JsonWriteOptions options, final @NotNull JsonWriter writer) throws JsonException {
        final StringWriter target = new StringWriter();
        write(options, target, writer);
        return target.toString();
    }

    /**
     * Writes JSON to a writer, which is flushed, but not closed.
     * @param options controls how JSON is written
     * @param target the target, e.g. an entry in a zip file
     * @param writer writes the content
     * @throws JsonException on errors
     */
    default void write(final @NotNull JsonWriteOptions options, final @NotNull Writer target, final @NotNull JsonWriter writer) throws JsonException {
        final JsonOutputStream out = writer(options, target);
        writer.writeJson(out);
        out.flush();
    }

    /**
     * Reads JSON text.
     * @param mode how strictly to parse
     * @param json the JSON text
     * @param reader reads the content
     * @throws JsonException on errors
     */
    default void read(final @NotNull JsonReadMode mode, final @NotNull String json, final @NotNull JsonReader reader) throws JsonException {
        reader.readJson(reader(json, mode));
    }

    /**
     * Reads JSON from a reader, which is not closed.
     * @param mode how strictly to parse
     * @param source the source
     * @param reader reads the content
     * @throws JsonException on errors
     */
    default void read(final @NotNull JsonReadMode mode, final @NotNull Reader source, final @NotNull JsonReader reader) throws JsonException {
        reader.readJson(reader(source, mode));
    }

}
