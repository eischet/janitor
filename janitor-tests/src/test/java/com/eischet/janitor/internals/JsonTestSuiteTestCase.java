// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.internals;

import com.eischet.janitor.JanitorTest;
import com.eischet.janitor.json.gson.GsonJsonStreams;
import com.eischet.janitor.toolbox.json.api.JsonException;
import com.eischet.janitor.toolbox.json.api.JsonInputStream;
import com.eischet.janitor.toolbox.json.api.JsonReadMode;
import com.eischet.janitor.toolbox.json.api.JsonStreamFactory;
import com.eischet.janitor.toolbox.json.api.JsonTokenType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Runs the parsing tests of <a href="https://github.com/nst/JSONTestSuite">JSONTestSuite</a> through every available JSON stream
 * implementation: files starting with {@code y_} must be accepted, {@code n_} rejected in STRICT mode, {@code i_} read as Gson
 * reads them (with two known exceptions), and no file may cause anything but a {@link JsonException}.
 * <p>
 * The streams read characters, so decoding bytes is the caller's job: files that are not valid UTF-8 are left out.
 * </p>
 */
public class JsonTestSuiteTestCase extends JanitorTest {

    private enum Outcome { ACCEPT, REJECT }

    /** The test files that are valid UTF-8, by file name. */
    private static final Map<String, String> FILES = new LinkedHashMap<>();

    @BeforeAll
    public static void loadFiles() throws IOException, URISyntaxException {
        final Path folder = Path.of(Objects.requireNonNull(JsonTestSuiteTestCase.class.getResource("/JSONTestSuite/test_parsing")).toURI());
        try (Stream<Path> paths = Files.list(folder)) {
            for (final Path path : paths.sorted().toList()) {
                try {
                    final String text = StandardCharsets.UTF_8.newDecoder()
                            .onMalformedInput(CodingErrorAction.REPORT)
                            .onUnmappableCharacter(CodingErrorAction.REPORT)
                            .decode(ByteBuffer.wrap(Files.readAllBytes(path)))
                            .toString();
                    FILES.put(path.getFileName().toString(), text);
                } catch (CharacterCodingException e) {
                    // not valid UTF-8, so this tests the decoding, not the streams
                }
            }
        }
        assertTrue(FILES.size() > 280, () -> "only " + FILES.size() + " test files found");
    }

    /**
     * Reads a whole document; numbers are read as strings, so this checks the syntax, not the conversion.
     * Anything other than a JsonException is passed on, failing the test.
     */
    private static Outcome read(final JsonStreamFactory json, final JsonReadMode mode, final String text) {
        try {
            final JsonInputStream in = json.reader(text, mode);
            readValue(in);
            final boolean complete = in.peek() == JsonTokenType.END_DOCUMENT;
            in.close();
            return complete ? Outcome.ACCEPT : Outcome.REJECT;
        } catch (JsonException e) {
            return Outcome.REJECT;
        }
    }

    private static void readValue(final JsonInputStream in) throws JsonException {
        switch (in.peek()) {
            case BEGIN_ARRAY -> {
                in.beginArray();
                while (in.hasNext()) {
                    readValue(in);
                }
                in.endArray();
            }
            case BEGIN_OBJECT -> {
                in.beginObject();
                while (in.hasNext()) {
                    in.nextKey();
                    readValue(in);
                }
                in.endObject();
            }
            case STRING, NUMBER -> in.nextString();
            case BOOLEAN -> in.nextBoolean();
            case NULL -> in.nextNull();
            default -> throw new JsonException("unexpected token " + in.peek() + " at " + in.getPath());
        }
    }

    /** Asserts that every implementation reads the files with the given prefix as expected; lists all mismatches at once. */
    private static void assertOutcome(final String prefix, final JsonReadMode mode, final Outcome expected) {
        final List<String> wrong = new ArrayList<>();
        for (final Map.Entry<String, String> file : FILES.entrySet()) {
            if (file.getKey().startsWith(prefix)) {
                for (final JsonStreamFactory json : JsonStreamFactory.available()) {
                    final Outcome outcome = read(json, mode, file.getValue());
                    if (outcome != expected) {
                        wrong.add(json.name() + ": " + file.getKey() + " → " + outcome);
                    }
                }
            }
        }
        assertTrue(wrong.isEmpty(), () -> mode + ", expected " + expected + ":\n" + String.join("\n", wrong));
    }

    @Test
    public void acceptsValidDocuments() {
        assertOutcome("y_", JsonReadMode.STRICT, Outcome.ACCEPT);
        assertOutcome("y_", JsonReadMode.LENIENT, Outcome.ACCEPT);
    }

    @Test
    public void rejectsInvalidDocuments() {
        assertOutcome("n_", JsonReadMode.STRICT, Outcome.REJECT);
    }

    /** Gson always skips a byte order mark; STRICT does not accept one. */
    private static final String BOM_FILE = "i_structure_UTF-8_BOM_empty_object.json";
    /** Nesting limits are each library's own default, e.g. 255 for Gson and 1000 for the native implementation, so this one is left out. */
    private static final String DEEP_FILE = "i_structure_500_nested_arrays.json";

    @Test
    public void implementationDefinedLikeGson() {
        final JsonStreamFactory gson = JsonStreamFactory.named(GsonJsonStreams.NAME);
        final List<String> wrong = new ArrayList<>();
        for (final Map.Entry<String, String> file : FILES.entrySet()) {
            if (file.getKey().startsWith("i_") && !file.getKey().equals(DEEP_FILE)) {
                for (final JsonReadMode mode : JsonReadMode.values()) {
                    final Outcome expected = file.getKey().equals(BOM_FILE) && mode == JsonReadMode.STRICT
                            ? Outcome.REJECT
                            : read(gson, mode, file.getValue());
                    for (final JsonStreamFactory json : JsonStreamFactory.available()) {
                        final Outcome outcome = read(json, mode, file.getValue());
                        if (outcome != expected && json != gson) {
                            wrong.add(json.name() + ", " + mode + ": " + file.getKey() + " → " + outcome);
                        }
                    }
                }
            }
        }
        assertTrue(wrong.isEmpty(), () -> "expected the same as Gson:\n" + String.join("\n", wrong));
    }

    @Test
    public void neverFailsOtherwise() {
        final List<String> failures = new ArrayList<>();
        for (final Map.Entry<String, String> file : FILES.entrySet()) {
            for (final JsonStreamFactory json : JsonStreamFactory.available()) {
                for (final JsonReadMode mode : JsonReadMode.values()) {
                    try {
                        read(json, mode, file.getValue());
                    } catch (RuntimeException | StackOverflowError e) {
                        failures.add(json.name() + ", " + mode + ": " + file.getKey() + " → " + e);
                    }
                }
            }
        }
        assertTrue(failures.isEmpty(), () -> String.join("\n", failures));
    }

}
