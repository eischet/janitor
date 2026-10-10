// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.generator.writing;

import org.jetbrains.annotations.NotNull;

/** A helper for writing source code: it collects text, and keeps track of the indentation. */
public class CodeOutputStream {

    private static final String DEFAULT_INDENT = "    ";
    private static final String NEWLINE = "\n";
    private final StringBuilder builder = new StringBuilder();
    private int indent = 0;

    /**
     * Runs some code only if a condition is true.
     * @param onlyWhen the condition
     * @param runnable the code to run
     * @return this
     */
    public CodeOutputStream optional(final boolean onlyWhen, final Runnable runnable) {
        if (onlyWhen) {
            runnable.run();
        }
        return this;
    }

    /**
     * Writes a name with its first letter in upper case.
     * @param name the name
     * @return this
     */
    public CodeOutputStream capitalize(final @NotNull String name) {
        if (!name.isEmpty()) {
            if (name.length() == 1) {
                return write(name.toUpperCase());
            } else {
                return write(name.substring(0, 1).toUpperCase() + name.substring(1));
            }
        }
        return this;
    }

    /**
     * Writes text.
     * @param s the text, ignored if null
     * @return this
     */
    public CodeOutputStream write(final String s) {
        if (s != null) {
            builder.append(s);
        }
        return this;
    }

    /**
     * Writes a space character, unless the last character is already a space.
     * @return this
     */
    public CodeOutputStream space() {
        if (!builder.isEmpty() && builder.charAt(builder.length() - 1) != ' ') {
            return write(" ");
        } else {
            return this;
        }
    }

    /**
     * Ends the current line.
     * @return this
     */
    public CodeOutputStream newline() {
        return write(NEWLINE);
    }

    /**
     * Writes an empty line.
     * @return this
     */
    public CodeOutputStream emptyLine() {
        write(NEWLINE);
        return this;
    }

    /**
     * Writes a line of text, indented to the current level.
     * @param s the text
     * @return this
     */
    public CodeOutputStream writeLine(final String s) {
        for (int i = 0; i < indent; i++) {
            write(DEFAULT_INDENT);
        }
        write(s);
        write(NEWLINE);
        return this;
    }

    /**
     * Increases the indentation by one level.
     * @return this
     */
    public CodeOutputStream indent() {
        indent++;
        return this;
    }

    /**
     * Decreases the indentation by one level.
     * @return this
     */
    public CodeOutputStream dedent() {
        indent--;
        return this;
    }

    /**
     * Writes an opening brace, and indents.
     * @return this
     */
    public CodeOutputStream startBlock() {
        writeLine("{");
        indent();
        return this;
    }

    /**
     * Dedents, and writes a closing brace.
     * @return this
     */
    public CodeOutputStream endBlock() {
        dedent();
        writeLine("}");
        return this;
    }


    @Override
    public String toString() {
        return builder.toString();
    }

    /**
     * Writes the indentation for the current level.
     * @return this
     */
    public CodeOutputStream writeIndent() {
        for (int i = 0; i < indent; i++) {
            write(DEFAULT_INDENT);
        }
        return this;
    }
}
