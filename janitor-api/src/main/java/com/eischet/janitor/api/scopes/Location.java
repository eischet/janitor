package com.eischet.janitor.api.scopes;

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

    /**
     * Create a location that refers to the start of a module.
     * @param module the module
     * @return the location at the start of the module
     */
    public static Location startOf(final ScriptModule module) {
        return new Location(module, 0, 0, 0, 0);
    }

    /**
     * Create a location that points to a specific area in a module.
     * @param module the module
     * @param line starting line
     * @param column starting column
     * @param endLine ending line
     * @param endColumn ending column
     * @return the location
     */
    public static Location at(final ScriptModule module, final int line, final int column, final int endLine, final int endColumn) {
        return new Location(module, line, column, endLine, endColumn);
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

    /**
     * Get the source line of this location.
     * @return the source line of this location, or null if not available
     */
    public @Nullable String getSourceLine() {
        if (line > 0) {
            return module.getSourceLine(line);
        } else {
            return null;
        }
    }

    /**
     * Create a call stack / location entry with an alias name.
     * @param name alias name, e.g. a function name
     * @return a location
     */
    public Location nested(final String name) {
        return new Location(module, line, column, endLine, endColumn, name);
    }

}
