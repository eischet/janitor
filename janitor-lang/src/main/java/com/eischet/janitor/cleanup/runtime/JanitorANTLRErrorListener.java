package com.eischet.janitor.cleanup.runtime;

import com.eischet.janitor.cleanup.api.api.errors.runtime.JanitorRuntimeException;
import org.antlr.v4.runtime.ANTLRErrorListener;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.atn.ATNConfigSet;
import org.antlr.v4.runtime.dfa.DFA;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;

import java.util.BitSet;

class JanitorANTLRErrorListener implements ANTLRErrorListener {

    private final String source;
    private final MutableList<String> issues = Lists.mutable.empty();
    private ImmutableList<String> lines;

    public JanitorANTLRErrorListener(final String source) {
        this.source = source;
    }

    private ImmutableList<String> splitSource() {
        return Lists.immutable.of(source.split("\r?\n\r?"));
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
        final String errorLine = JanitorRuntimeException.getLine(lines, line);
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

    public ImmutableList<String> getIssues() {
        return issues.toImmutable();
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
