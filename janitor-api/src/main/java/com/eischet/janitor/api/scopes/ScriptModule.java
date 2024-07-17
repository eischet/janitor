package com.eischet.janitor.api.scopes;


import com.eischet.janitor.api.calls.Memoized;

import java.util.List;

/**
 * A script module.
 * This is not (directly) what gets imported by the "import" statement, it's a representation of a script file.
 */
public class ScriptModule {
    private static final ScriptModule BUILTIN = new ScriptModule("builtin", "");

    private final String name;
    private final String source;
    private final Memoized<List<String>> sourceLines;

    /**
     * Constructs a new ScriptModule.
     *
     * @param name   the name of the module
     * @param source the source code of the module
     */
    public ScriptModule(final String name, final String source) {
        this.name = name;
        this.source = source;
        this.sourceLines = new Memoized<>(() -> List.of(source.split("\r?\n\r?")));
    }

    /**
     * Create a new unnamed ScriptModule.
     *
     * @param source the source code of the module
     * @return the new ScriptModule
     */
    public static ScriptModule unnamed(final String source) {
        return new ScriptModule("unnamed", source);
    }

    /**
     * Check if the module is the builtin module.
     *
     * @param module the module
     * @return true if the module is the builtin module
     */
    public static boolean isBuiltin(final ScriptModule module) {
        return module == BUILTIN;
    }

    /**
     * Get the builtin module.
     *
     * @return the builtin module
     * TODO: I want to move the Builtin module away, into the Environment, where the host has more control over it.
     */
    public static ScriptModule builtin() {
        return BUILTIN;
    }

    /**
     * From the list of lines, retrieve the line at the given index.
     *
     * @param lines the list of lines
     * @param line  the index of the line to retrieve
     * @return null on any errors, or else the line at the given index
     */
    public static String getLine(final List<String> lines, int line) {
        if (line > lines.size() || line < 1 || lines.isEmpty()) {
            return null;
        }
        return lines.get(line - 1);
    }

    /**
     * Gets the name of the module.
     *
     * @return the name of the module
     */
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }

    /**
     * Get the source code of the module.
     *
     * @return the source code of the module
     */
    public String getSource() {
        return source;
    }

    /**
     * Get the source line at the given line number.
     *
     * @param line the line number
     * @return the source line
     */
    public String getSourceLine(final int line) {
        final String text = getLine(sourceLines.get(), line);
        if (text != null && text.trim().equals("{")) {
            return getLine(sourceLines.get(), line - 1) + "\n    " + text;
            // die 4 Spaces kommen daher, dass sonst die Einrückung im Stack Trace nicht passt...
        }
        return text;
    }

}
