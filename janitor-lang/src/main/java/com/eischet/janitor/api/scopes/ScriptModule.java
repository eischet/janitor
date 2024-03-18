package com.eischet.janitor.api.scopes;


import com.eischet.janitor.tools.Memoized;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;

public class ScriptModule {
    private final String name;
    private final String source;
    private final Memoized<ImmutableList<String>> sourceLines;

    public ScriptModule(final String name, final String source) {
        this.name = name;
        this.source = source;
        this.sourceLines = new Memoized<>(() -> Lists.immutable.of(source.split("\r?\n\r?")));
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }

    public static ScriptModule unnamed(final String source) {
        return new ScriptModule("unnamed", source);
    }

    private static final ScriptModule BUILTIN = new ScriptModule("builtin", "");

    public static boolean isBuiltin(final ScriptModule module) {
        return module == BUILTIN;
    }

    public static ScriptModule builtin() {
        return BUILTIN;
    }

    public String getSource() {
        return source;
    }

    public static String getLine(final ImmutableList<String> lines, int line) {
        if (line > lines.size() || line < 1 || lines.isEmpty()) {
            return null;
        }
        return lines.get(line - 1);
    }

    public String getSourceLine(final int line) {
        final String text = getLine(sourceLines.get(), line);
        if (text != null && text.trim().equals("{")) {
            return getLine(sourceLines.get(), line-1) + "\n    " + text;
            // die 4 Spaces kommen daher, dass sonst die Einrückung im Stack Trace nicht passt...
        }
        return text;
    }

}
