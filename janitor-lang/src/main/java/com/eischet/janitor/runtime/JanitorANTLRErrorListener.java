package com.eischet.janitor.runtime;

import com.eischet.janitor.api.scopes.ScriptModule;
import com.eischet.janitor.lang.JanitorLexer;
import org.antlr.v4.runtime.*;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.LinkedList;
import java.util.List;

public class JanitorANTLRErrorListener extends BaseErrorListener implements ANTLRErrorListener {

    protected final String source;
    protected final List<String> issues = new LinkedList<>();
    protected List<String> lines;

    public JanitorANTLRErrorListener(final String source) {
        this.source = source;
    }

    private @Unmodifiable List<String> splitSource() {
        return List.of(source.split("\r?\n\r?"));
    }

    @Override
    public void syntaxError(final Recognizer<?, ?> recognizer,
                            final Object offendingSymbol,
                            final int line,
                            final int charPositionInLine,
                            final String msg,
                            final RecognitionException e) {
        if (lines == null) {
            lines = splitSource();
        }

        // When semicolons were made optional, we started getting warnings like this:
        //      ... print("hello") }
        //                         ^ here at the closing brace
        // "no viable alternative at input '}'"
        // It would be desirable to remove these warnings, but until I figure out how to upgrade the grammar to do that,
        // I'll simply mute them.
        if (offendingSymbol instanceof Token token) {
            int type = token.getType();
            if (type == JanitorLexer.RBRACE && msg.contains("no viable alternative")) {
                return;
            }
        }

        final String improvedMessage = improveAntlrMessage(msg);
        if (improvedMessage == null) {
            return;
        }

        final String errorLine = ScriptModule.getLine(lines, line);
        if (errorLine != null && !errorLine.isBlank()) {
            issues.add("line %s:%s --> %s \n    %s".formatted(line, charPositionInLine, improvedMessage, errorLine));
        } else {
            issues.add("line %s:%s --> %s".formatted(line, charPositionInLine, improvedMessage));
        }
    }

    public List<String> getIssues() {
        return issues;
    }

    private @Nullable String improveAntlrMessage(final String msg) {
        if (msg.contains("failed predicate") && msg.contains("stmtTerminator")) {
            // ignore; it's just the predicate returning false when trying to determine if a ';' is required
            return null;
        }
        String improved = msg;
        if (improved != null) {
            if (improved.startsWith("token recognition error at: '\"") || improved.startsWith("token recognition error at: ''")) {
                improved = improved.replaceFirst("token recognition error", "invalid string");
            }
            if (improved.contains("STMT_TERM")) {
                improved = improved.replace("STMT_TERM", "';'");
            }
            if (improved.contains(("'<EOF>'"))) {
                improved = improved.replace("'<EOF>'", "end of file");
            }
        }
        return improved;
    }

    public boolean hasIssues() {
        return !issues.isEmpty();
    }

    public int numberOfIssues() {
        return issues.size();
    }
}
