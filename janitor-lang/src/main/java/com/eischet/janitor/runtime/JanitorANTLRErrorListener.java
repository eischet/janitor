// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.runtime;

import com.eischet.janitor.api.errors.compiler.ScriptDiagnostic;
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
    protected final List<ScriptDiagnostic> diagnostics = new LinkedList<>();
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

        diagnostics.add(ScriptDiagnostic.error(line, charPositionInLine, line, charPositionInLine + tokenLength(offendingSymbol), improvedMessage));

        final String errorLine = ScriptSource.getLine(lines, line);
        if (errorLine != null && !errorLine.isBlank()) {
            issues.add("line %s:%s --> %s \n    %s".formatted(line, charPositionInLine, improvedMessage, errorLine));
        } else {
            issues.add("line %s:%s --> %s".formatted(line, charPositionInLine, improvedMessage));
        }
    }

    // the offending token is marked as a whole if it is on one line, else only its first character
    private static int tokenLength(final Object offendingSymbol) {
        if (offendingSymbol instanceof Token token && token.getType() != Token.EOF) {
            final String text = token.getText();
            if (text != null && !text.isEmpty() && text.indexOf('\n') < 0) {
                return text.length();
            }
        }
        return 1;
    }

    /**
     * @return the issues that were found, with their positions
     */
    public List<ScriptDiagnostic> getDiagnostics() {
        return diagnostics;
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
            improved = shortenExpectedTokens(improved);
            // ANTLR quotes line breaks in the input as \n, followed by the next line's indentation
            improved = improved.replaceAll("(\\\\[rn])+\\s*", " ");
        }
        return improved;
    }

    private static final int MAX_EXPECTED_TOKENS = 6;

    // "expecting {REGEX_LITERAL, 'function', ...}" lists every token that could follow, often dozens; that helps nobody
    private static String shortenExpectedTokens(final String message) {
        final int start = message.indexOf(" expecting {");
        if (start < 0) {
            return message;
        }
        final int end = message.indexOf('}', start);
        if (end < 0) {
            return message;
        }
        final String expected = message.substring(start + " expecting {".length(), end);
        if (expected.split(",").length <= MAX_EXPECTED_TOKENS) {
            return message;
        }
        return message.substring(0, start) + message.substring(end + 1);
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
