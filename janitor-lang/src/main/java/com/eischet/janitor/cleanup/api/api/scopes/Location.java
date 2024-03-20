package com.eischet.janitor.cleanup.api.api.scopes;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.MutableList;
import org.jetbrains.annotations.Nullable;

public class Location {

    private final ScriptModule module;
    private final int line;
    private final int column;
    private final int endLine;
    private final int endColumn;
    private final String nesting;

    private Location(final ScriptModule module, final int line, final int column, final int endLine, final int endColumn) {
        this.module = module;
        this.line = line;
        this.column = column;
        this.endLine = endLine;
        this.endColumn = endColumn;
        this.nesting = null;
    }

    private Location(final ScriptModule module, final int line, final int column, final int endLine, final int endColumn, final String nesting) {
        this.module = module;
        this.line = line;
        this.column = column;
        this.endLine = endLine;
        this.endColumn = endColumn;
        this.nesting = nesting;
    }

    public static Location startOf(final ScriptModule module) {
        return new Location(module, 0, 0, 0, 0);
    }

    public ScriptModule getModule() {
        return module;
    }

    public int getLine() {
        return line;
    }

    public int getColumn() {
        return column;
    }

    public int getEndLine() {
        return endLine;
    }

    public int getEndColumn() {
        return endColumn;
    }

    @Override
    public String toString() {
        if (nesting == null) {
            if (line == 0 && column == 0) {
                return "Module '%s'".formatted(module);
            }
            return "Module '%s', line %s, column %s".formatted(module, line, column);
        } else {
            return "Module '%s', line %s, column %s, in '%s'".formatted(module, line, column, nesting);
        }
    }

    public @Nullable String getSourceLine() {
        if (line > 0) {
            return module.getSourceLine(line);
        } else {
            return null;
        }
    }

    public @Nullable String getFullSourceLines() {
        if (line > 0 && endLine >= line) {
            final MutableList<String> lines = Lists.mutable.empty();
            for (int i=line; i<=endLine; i++) {
                final String lineText = module.getSourceLine(i);
                if (lineText != null) {
                    lines.add(lineText);
                }
            }
            return lines.makeString("\n");
        } else {
            return getSourceLine();
        }
    }

    public static Location at(final ScriptModule module, final int line, final int column, final int endLine, final int endColumn) {
        return new Location(module, line, column, endLine, endColumn);
    }

    public Location nested(final String name) {
        return new Location(module, line, column, endLine, endColumn, name);
    }

}
