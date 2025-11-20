package com.eischet.janitor.generator.writing;

import org.jetbrains.annotations.NotNull;

public class CodeOutputStream {

    private static final String DEFAULT_INDENT = "    ";
    private static final String NEWLINE = "\n";
    private final StringBuilder builder = new StringBuilder();
    private int indent = 0;

    public CodeOutputStream optional(final boolean onlyWhen, final Runnable runnable) {
        if (onlyWhen) {
            runnable.run();
        }
        return this;
    }

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

    public CodeOutputStream newline() {
        return write(NEWLINE);
    }

    public CodeOutputStream emptyLine() {
        write(NEWLINE);
        return this;
    }

    public CodeOutputStream writeLine(final String s) {
        for (int i = 0; i < indent; i++) {
            write(DEFAULT_INDENT);
        }
        write(s);
        write(NEWLINE);
        return this;
    }

    public CodeOutputStream indent() {
        indent++;
        return this;
    }

    public CodeOutputStream dedent() {
        indent--;
        return this;
    }

    public CodeOutputStream startBlock() {
        writeLine("{");
        indent();
        return this;
    }

    public CodeOutputStream endBlock() {
        dedent();
        writeLine("}");
        return this;
    }


    @Override
    public String toString() {
        return builder.toString();
    }

    public CodeOutputStream writeIndent() {
        for (int i = 0; i < indent; i++) {
            write(DEFAULT_INDENT);
        }
        return this;
    }
}
