// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.json.jackson;

import com.eischet.janitor.toolbox.json.api.JsonException;
import com.eischet.janitor.toolbox.json.api.JsonInputStream;
import com.eischet.janitor.toolbox.json.api.JsonOutputStream;
import com.eischet.janitor.toolbox.json.api.JsonReadMode;
import com.eischet.janitor.toolbox.json.api.JsonStreamFactory;
import com.eischet.janitor.toolbox.json.api.JsonWriteOptions;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.StreamWriteFeature;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;

/**
 * Creates JSON streams that use Jackson's streaming API (jackson-core only, no data binding).
 * Select it with {@code -Djanitor.json=jackson} or {@link JsonStreamFactory#named(String)}.
 */
public class JacksonJsonStreams implements JsonStreamFactory {

    /** The name of this implementation. */
    public static final String NAME = "jackson";

    /** Parses strictly; also used for writing. Jackson's factories are thread-safe. */
    private static final JsonFactory STRICT = JsonFactory.builder()
            .disable(StreamWriteFeature.AUTO_CLOSE_CONTENT) // an incomplete document is an error, not silently completed
            .build();

    /** Parses with the additions listed in {@link JsonReadMode#LENIENT}; the BOM is handled by {@link JacksonInputStream}. */
    private static final JsonFactory LENIENT = JsonFactory.builder()
            .enable(JsonReadFeature.ALLOW_JAVA_COMMENTS)
            .enable(JsonReadFeature.ALLOW_SINGLE_QUOTES)
            .enable(JsonReadFeature.ALLOW_UNQUOTED_FIELD_NAMES)
            .enable(JsonReadFeature.ALLOW_TRAILING_COMMA)
            .enable(JsonReadFeature.ALLOW_NON_NUMERIC_NUMBERS)
            .build();

    @Override
    public @NotNull String name() {
        return NAME;
    }

    @Override
    public int priority() {
        return 50;
    }

    @Override
    public @NotNull JsonInputStream reader(final @NotNull String json, final @NotNull JsonReadMode mode) {
        return open(new StringReader(json), mode, json);
    }

    @Override
    public @NotNull JsonInputStream reader(final @NotNull Reader source, final @NotNull JsonReadMode mode) {
        return open(source, mode, null);
    }

    private static JsonInputStream open(final Reader source, final JsonReadMode mode, final String text) {
        final boolean lenient = mode == JsonReadMode.LENIENT;
        try {
            return new JacksonInputStream((lenient ? LENIENT : STRICT).createParser(lenient ? JacksonInputStream.skipBom(source) : source), text, lenient);
        } catch (IOException e) {
            throw new JsonException("error opening JSON reader", e);
        }
    }

    @Override
    public @NotNull JsonOutputStream writer(final @NotNull JsonWriteOptions options, final @NotNull Writer target) {
        try {
            return new JacksonOutputStream(options, STRICT.createGenerator(target));
        } catch (IOException e) {
            throw new JsonException("error opening JSON writer", e);
        }
    }

    @Override
    public String toString() {
        return NAME;
    }

}
