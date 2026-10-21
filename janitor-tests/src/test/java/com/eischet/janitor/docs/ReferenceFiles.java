// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.docs;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/** Finds and parses the markdown files of the language reference in {@code docs/reference}. */
final class ReferenceFiles {

    private static final Pattern FENCE = Pattern.compile("^```\\s*([\\w-]*)\\s*(.*)$");
    private static final Pattern EXPLICIT_ANCHOR = Pattern.compile("<a id=\"([^\"]+)\"></a>");
    private static final Pattern HEADING = Pattern.compile("^#{1,6}\\s+(.*)$");

    private ReferenceFiles() {
    }

    /**
     * @return the directory of the reference; tests run in the janitor-tests module directory
     */
    static Path directory() {
        final Path base = Path.of(System.getProperty("basedir", "")).toAbsolutePath();
        for (final Path candidate : List.of(base.resolve("../docs/reference"), base.resolve("docs/reference"))) {
            if (Files.isDirectory(candidate)) {
                return candidate.normalize();
            }
        }
        throw new IllegalStateException("cannot find docs/reference from " + base);
    }

    /**
     * @return the markdown files of the reference, sorted by name
     */
    static List<Path> markdownFiles() {
        try (Stream<Path> files = Files.list(directory())) {
            return files.filter(p -> p.getFileName().toString().endsWith(".md")).sorted().toList();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    static String read(final Path file) {
        try {
            return Files.readString(file).replace("\r\n", "\n");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * A fenced code block.
     * @param file the file name
     * @param line the line of the opening fence, starting at 1
     * @param language the info string's first word, e.g. "janitor"
     * @param flags the rest of the info string, e.g. "error"
     * @param content the code
     */
    record CodeBlock(String file, int line, String language, String flags, String content) {
        boolean hasFlag(final String flag) {
            return List.of(flags.split("\\s+")).contains(flag);
        }
    }

    /**
     * @param file the file name, for messages
     * @param markdown the markdown text
     * @return all fenced code blocks, in order
     */
    static List<CodeBlock> codeBlocks(final String file, final String markdown) {
        final String[] lines = markdown.split("\n", -1);
        final List<CodeBlock> blocks = new ArrayList<>();
        for (int i = 0; i < lines.length; i++) {
            final Matcher open = FENCE.matcher(lines[i]);
            if (!open.matches()) {
                continue;
            }
            final int start = i;
            final StringBuilder content = new StringBuilder();
            i++;
            while (i < lines.length && !lines[i].startsWith("```")) {
                content.append(lines[i]).append("\n");
                i++;
            }
            if (i >= lines.length) {
                throw new IllegalStateException(file + ":" + (start + 1) + ": unterminated code block");
            }
            blocks.add(new CodeBlock(file, start + 1, open.group(1), open.group(2).strip(), content.toString()));
        }
        return blocks;
    }

    /**
     * Collects the anchors that links can point to: explicit {@code <a id="...">} anchors and the ids that GitHub derives
     * from headings.
     * @param markdown the markdown text
     * @return the anchors
     */
    static Set<String> anchors(final String markdown) {
        final Set<String> anchors = new LinkedHashSet<>();
        final Matcher explicit = EXPLICIT_ANCHOR.matcher(markdown);
        while (explicit.find()) {
            anchors.add(explicit.group(1));
        }
        boolean inCode = false;
        for (final String line : markdown.split("\n")) {
            if (line.startsWith("```")) {
                inCode = !inCode;
                continue;
            }
            final Matcher heading = HEADING.matcher(line);
            if (!inCode && heading.matches()) {
                anchors.add(slug(heading.group(1)));
            }
        }
        return anchors;
    }

    /**
     * @param heading the text of a heading
     * @return the anchor id that GitHub generates for it
     */
    static String slug(final String heading) {
        return heading.strip().toLowerCase(Locale.ROOT)
                .replaceAll("[^\\p{L}\\p{N} _-]", "")
                .replace(' ', '-');
    }
}
