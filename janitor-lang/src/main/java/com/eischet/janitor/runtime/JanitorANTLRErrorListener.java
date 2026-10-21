// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.runtime;

import com.eischet.janitor.api.scopes.ScriptSource;
import com.eischet.janitor.lang.JanitorLexer;
import org.antlr.v4.runtime.*;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.LinkedList;
import java.util.List;

/** Collects syntax errors from the ANTLR parser, and improves their messages. */
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

        final String improvedMessage = improveAntlrMessage(msg);
        if (improvedMessage == null) {
            return;
        }

        final String errorLine = ScriptSource.getLine(lines, line);
        if (errorLine != null && !errorLine.isBlank()) {
            issues.add("line %s:%s --> %s \n    %s".formatted(line, charPositionInLine, improvedMessage, errorLine));
        } else {
            issues.add("line %s:%s --> %s".formatted(line, charPositionInLine, improvedMessage));
        }
    }

    /**
     * @return the issues that were found
     */
    public List<String> getIssues() {
        return issues;
    }

    private @Nullable String improveAntlrMessage(final String msg) {
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

    /**
     * @return true if there were any issues
     */
    public boolean hasIssues() {
        return !issues.isEmpty();
    }

    /**
     * @return the number of issues
     */
    public int numberOfIssues() {
        return issues.size();
    }
}
