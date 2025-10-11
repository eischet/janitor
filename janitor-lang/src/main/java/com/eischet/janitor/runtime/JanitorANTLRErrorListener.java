package com.eischet.janitor.runtime;

import com.eischet.janitor.api.scopes.ScriptModule;
import com.eischet.janitor.lang.JanitorLexer;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.ATNConfigSet;
import org.antlr.v4.runtime.dfa.DFA;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

class JanitorANTLRErrorListener implements ANTLRErrorListener {

    private final String source;
    private final List<String> issues = new ArrayList<>();
    private List<String> lines;

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

        // Typischer "fehlendes Semikolon" Fall:
        if (offendingSymbol instanceof Token token) {
            int type = token.getType();
            // 1. Nur ignorieren, wenn das nächste Token eine '}' ist
            // 2. Und der Fehlertext das typische "no viable alternative" enthält
            if (type == JanitorLexer.RBRACE && msg.contains("no viable alternative")) {
                // optionale Debug-Ausgabe, falls du nachvollziehen willst:
                // System.out.println("Ignored semicolon warning at line " + line);
                issues.add("yodel");
                return;
            }
        }
        // TODO: make this actually work...
        // Since semicolons are optional, we get a parser warning at code like this:
        //      ... print("hello") }
        //                         ^ here at the closing brace.
        if (e == null && offendingSymbol instanceof Token t && t.getText().equals("}")) {
            if ("no viable alternative at input '}'".equals(msg)) {
                return;
            }
        }

        final String improvedMessage = improveAntlrMessage(msg);
        final String errorLine = ScriptModule.getLine(lines, line);
        if (errorLine != null && !errorLine.isBlank()) {
            issues.add("line %s:%s --> %s \n    %s".formatted(line, charPositionInLine, improvedMessage, errorLine));
        } else {
            issues.add("line %s:%s --> %s".formatted(line, charPositionInLine, improvedMessage));
        }
    }

    @Override
    public void reportAmbiguity(final Parser recognizer, final DFA dfa, final int startIndex, final int stopIndex, final boolean exact, final BitSet ambigAlts, final ATNConfigSet configs) {
        // LATER: actually report this, as warning of sorts?
        // log.info("ambiguity!");
    }

    @Override
    public void reportAttemptingFullContext(final Parser recognizer, final DFA dfa, final int startIndex, final int stopIndex, final BitSet conflictingAlts, final ATNConfigSet configs) {
        // LATER: actually report this, as warning of sorts?
        // log.info("attempting full context!");
    }

    @Override
    public void reportContextSensitivity(final Parser recognizer, final DFA dfa, final int startIndex, final int stopIndex, final int prediction, final ATNConfigSet configs) {
        // LATER: actually report this, as warning of sorts?
        // log.info("context sensitive!");
    }

    public List<String> getIssues() {
        return issues;
    }

    private String improveAntlrMessage(final String msg) {
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
}
