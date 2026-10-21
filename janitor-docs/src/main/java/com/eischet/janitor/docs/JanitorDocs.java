// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.docs;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

/**
 * Reads the Janitor language reference from the class path.
 * <p>
 * The reference consists of Markdown files under {@value #RESOURCE_PATH}, which describe the language as every host gets it.
 * Hosts can hand them to users or AI clients, and describe their own modules and bindings in documents of their own, which
 * refer to the extension points of {@code hosting.md}, e.g. {@code hosting.md#host-provided-modules}.
 * </p>
 * <p>
 * Without this class, read the index {@value #INDEX} as a properties file: {@code version} is the Janitor version that the
 * reference describes, and {@code files} lists the file names in reading order, separated by commas.
 * </p>
 */
public final class JanitorDocs {

    /** The class path directory of the reference, without a leading slash. */
    public static final String RESOURCE_PATH = "META-INF/janitor/docs/";

    /** The index of the reference, a properties file with the keys {@code version} and {@code files}. */
    public static final String INDEX = RESOURCE_PATH + "janitor-docs.properties";

    private static final Properties PROPERTIES = loadIndex();

    private JanitorDocs() {
    }

    private static Properties loadIndex() {
        final Properties properties = new Properties();
        try (InputStream in = JanitorDocs.class.getClassLoader().getResourceAsStream(INDEX)) {
            if (in == null) {
                throw new IllegalStateException("missing resource " + INDEX);
            }
            properties.load(in);
        } catch (IOException e) {
            throw new UncheckedIOException("cannot read " + INDEX, e);
        }
        return properties;
    }

    /**
     * @return the Janitor version that the reference describes, e.g. "0.9.70"
     */
    public static String version() {
        return PROPERTIES.getProperty("version", "");
    }

    /**
     * @return the file names of the reference, e.g. "language.md", in reading order
     */
    public static List<String> files() {
        return Arrays.stream(PROPERTIES.getProperty("files", "").split(","))
                .map(String::strip)
                .filter(name -> !name.isEmpty())
                .toList();
    }

    /**
     * Reads one file of the reference.
     * @param name a file name as returned by {@link #files()}
     * @return the Markdown text, or empty if there is no such file
     */
    public static Optional<String> read(final String name) {
        if (name == null || name.contains("/") || name.contains("\\")) {
            return Optional.empty();
        }
        try (InputStream in = JanitorDocs.class.getClassLoader().getResourceAsStream(RESOURCE_PATH + name)) {
            if (in == null) {
                return Optional.empty();
            }
            return Optional.of(new String(in.readAllBytes(), StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new UncheckedIOException("cannot read " + RESOURCE_PATH + name, e);
        }
    }

    /**
     * Reads the whole reference as one text, e.g. to give it to an AI model in one piece: all files in reading order, each one
     * preceded by a line {@code <!-- file: name -->}.
     * @return the Markdown text
     */
    public static String readAll() {
        final StringBuilder out = new StringBuilder();
        for (final String name : files()) {
            read(name).ifPresent(text -> {
                out.append("<!-- file: ").append(name).append(" -->\n\n").append(text);
                if (!text.endsWith("\n")) {
                    out.append('\n');
                }
                out.append('\n');
            });
        }
        return out.toString();
    }
}
