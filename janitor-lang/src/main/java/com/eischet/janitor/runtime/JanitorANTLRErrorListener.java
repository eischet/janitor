// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.runtime;

import com.eischet.janitor.api.errors.compiler.ScriptDiagnostic;
import com.eischet.janitor.api.scopes.ScriptSource;
import com.eischet.janitor.lang.JanitorLexer;
import com.eischet.janitor.lang.JanitorParser;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.IntervalSet;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Collects syntax errors from the ANTLR lexer and parser, and words them for people who do not know the grammar: "unexpected 'bar'
 * after '2'" instead of "no viable alternative at input 'foo(1, 2 bar'".
 * <p>
 * Brackets that are not closed are found before parsing ({@link BracketCheck}), because the parser only stumbles over them later.
 * Each line reports only its first problem: after a syntax error, the parser mostly trips over what follows it.
 * </p>
 */
public class JanitorANTLRErrorListener extends BaseErrorListener implements ANTLRErrorListener {

    private static final int MAX_EXPECTED_TOKENS = 6;
    private static final int MAX_QUOTED_LENGTH = 20;

    private final String source;
    private final List<Problem> reported = new ArrayList<>();
    private @Nullable List<Problem> problems;

    /** A problem at a position: lines start at 1, columns at 0, the end column is exclusive. */
    private record Problem(int line, int column, int endColumn, String message) {
    }

    public JanitorANTLRErrorListener(final String source) {
        this.source = source == null ? "" : source;
    }

    @Override
    public void syntaxError(final Recognizer<?, ?> recognizer,
                            final Object offendingSymbol,
                            final int line,
                            final int charPositionInLine,
                            final String msg,
                            final RecognitionException e) {
        final Token token = offendingSymbol instanceof Token t ? t : null;
        final String message = recognizer instanceof Parser parser ? parserMessage(parser, token, msg, e) : lexerMessage(msg);
        reported.add(new Problem(line, charPositionInLine, charPositionInLine + tokenLength(token), message));
        problems = null;
    }

    // the offending token is marked as a whole if it is on one line, else only its first character
    private static int tokenLength(final @Nullable Token token) {
        if (token != null && token.getType() != Token.EOF) {
            final String text = token.getText();
            if (text != null && !text.isEmpty() && text.indexOf('\n') < 0) {
                return text.length();
            }
        }
        return 1;
    }

    private static String parserMessage(final Parser parser, final @Nullable Token token, final String msg, final @Nullable RecognitionException e) {
        final String found = describe(token);
        if (e instanceof FailedPredicateException failed && failed.getRuleIndex() == JanitorParser.RULE_stmtTerminator) {
            return "unexpected " + found + MISSING_SEPARATOR;
        }
        if (e instanceof NoViableAltException) {
            final Token previous = previous(parser, token);
            if (previous == null) {
                return "unexpected " + found;
            }
            // two values in a row, e.g. "print(1) print(2)" or "return 1 2"
            final boolean twoValues = token != null && token.getLine() == previous.getLine() && endsValue(previous.getType()) && startsValue(token.getType());
            return "unexpected " + found + " after " + describe(previous) + (twoValues ? MISSING_SEPARATOR : "");
        }
        final String expected = expected(parser);
        if (e == null && msg != null && msg.startsWith("missing ") && expected != null) {
            return "missing " + expected + " before " + found;
        }
        return expected == null ? "unexpected " + found : "unexpected " + found + ", expected " + expected;
    }

    private static final String MISSING_SEPARATOR = "; is an operator, a comma or a line break missing before it?";

    private static boolean endsValue(final int type) {
        return type == JanitorLexer.RPAREN || type == JanitorLexer.RBRACK || type == JanitorLexer.INC || type == JanitorLexer.DEC || isValue(type);
    }

    private static boolean startsValue(final int type) {
        return isValue(type) || type == JanitorLexer.IF || type == JanitorLexer.FOR || type == JanitorLexer.WHILE || type == JanitorLexer.RETURN;
    }

    // names and literals
    private static boolean isValue(final int type) {
        return switch (type) {
            case JanitorLexer.IDENTIFIER, JanitorLexer.TRUE, JanitorLexer.FALSE, JanitorLexer.NULL,
                 JanitorLexer.DECIMAL_LITERAL, JanitorLexer.HEX_LITERAL, JanitorLexer.OCT_LITERAL, JanitorLexer.BINARY_LITERAL,
                 JanitorLexer.FLOAT_LITERAL, JanitorLexer.STRING_LITERAL_SINGLE, JanitorLexer.STRING_LITERAL_DOUBLE,
                 JanitorLexer.STRING_LITERAL_TRIPLE_SINGLE, JanitorLexer.STRING_LITERAL_TRIPLE_DOUBLE, JanitorLexer.REGEX_LITERAL,
                 JanitorLexer.DATE_LITERAL, JanitorLexer.DATE_TIME_LITERAL, JanitorLexer.TODAY_LITERAL, JanitorLexer.NOW_LITERAL,
                 JanitorLexer.YEARS_LITERAL, JanitorLexer.MONTHS_LITERAL, JanitorLexer.WEEKS_LITERAL, JanitorLexer.DAYS_LITERAL,
                 JanitorLexer.HOURS_LITERAL, JanitorLexer.MINUTES_LITERAL, JanitorLexer.SECONDS_LITERAL -> true;
            default -> false;
        };
    }

    private static String lexerMessage(final String msg) {
        // ANTLR: "token recognition error at: 'text'"
        final int start = msg == null ? -1 : msg.indexOf("at: '");
        final String text = start < 0 ? "" : msg.substring(start + "at: '".length(), msg.length() - 1);
        if (text.startsWith("'") || text.startsWith("\"")) {
            // ANTLR shows a line break in the text as \n, so that one is the end of the line, not an escape sequence
            if (text.matches("(?s).*\\\\[^nr]")) {
                return "invalid escape sequence '" + text.substring(text.length() - 2) + "' in a string; write '\\\\' for a backslash";
            }
            return "string not closed: " + text.charAt(0) + " is missing at the end of the line";
        }
        return "unknown character " + quote(text);
    }

    private static @Nullable Token previous(final Parser parser, final @Nullable Token token) {
        if (token == null || token.getTokenIndex() <= 0) {
            return null;
        }
        final TokenStream stream = parser.getTokenStream();
        for (int i = token.getTokenIndex() - 1; i >= 0; i--) {
            final Token candidate = stream.get(i);
            if (candidate.getChannel() == Token.DEFAULT_CHANNEL) {
                return candidate;
            }
        }
        return null;
    }

    // what the parser expected, e.g. "')' or ','"; null if that is too much to be of help
    private static @Nullable String expected(final Parser parser) {
        final IntervalSet tokens;
        try {
            tokens = parser.getExpectedTokens();
        } catch (RuntimeException e) {
            return null;
        }
        final Set<String> names = new LinkedHashSet<>();
        for (final int type : tokens.toList()) {
            names.add(tokenName(type));
        }
        if (names.isEmpty() || names.size() > MAX_EXPECTED_TOKENS) {
            return null;
        }
        return String.join(" or ", names);
    }

    private static String tokenName(final int type) {
        return switch (type) {
            case Token.EOF -> "the end of the script";
            case JanitorLexer.IDENTIFIER -> "a name";
            case JanitorLexer.DECIMAL_LITERAL, JanitorLexer.HEX_LITERAL, JanitorLexer.OCT_LITERAL, JanitorLexer.BINARY_LITERAL,
                 JanitorLexer.FLOAT_LITERAL -> "a number";
            case JanitorLexer.STRING_LITERAL_SINGLE, JanitorLexer.STRING_LITERAL_DOUBLE, JanitorLexer.STRING_LITERAL_TRIPLE_SINGLE,
                 JanitorLexer.STRING_LITERAL_TRIPLE_DOUBLE -> "a string";
            case JanitorLexer.REGEX_LITERAL -> "a regex";
            case JanitorLexer.DATE_LITERAL, JanitorLexer.DATE_TIME_LITERAL, JanitorLexer.TODAY_LITERAL, JanitorLexer.NOW_LITERAL -> "a date";
            case JanitorLexer.YEARS_LITERAL, JanitorLexer.MONTHS_LITERAL, JanitorLexer.WEEKS_LITERAL, JanitorLexer.DAYS_LITERAL,
                 JanitorLexer.HOURS_LITERAL, JanitorLexer.MINUTES_LITERAL, JanitorLexer.SECONDS_LITERAL -> "a duration";
            default -> {
                final String literal = JanitorLexer.VOCABULARY.getLiteralName(type);
                yield literal != null ? literal : JanitorLexer.VOCABULARY.getDisplayName(type);
            }
        };
    }

    private static String describe(final @Nullable Token token) {
        if (token == null || token.getType() == Token.EOF) {
            return "end of script";
        }
        return quote(token.getText());
    }

    private static String quote(final @Nullable String text) {
        String flat = text == null ? "" : text.replaceAll("\\s+", " ");
        if (flat.length() > MAX_QUOTED_LENGTH) {
            flat = flat.substring(0, MAX_QUOTED_LENGTH) + "…";
        }
        // a string brings its own quotes
        return flat.startsWith("'") || flat.startsWith("\"") ? flat : "'" + flat + "'";
    }

    // the brackets first, then what lexer and parser reported; one problem per line, sorted by position. Brackets are only looked at
    // after other errors: a script with a bracket problem never parses.
    private List<Problem> problems() {
        if (problems == null) {
            final List<Problem> all = new ArrayList<>();
            if (!reported.isEmpty()) {
                for (final BracketCheck.Problem bracket : BracketCheck.check(source)) {
                    all.add(new Problem(bracket.line(), bracket.column(), bracket.column() + 1, bracket.message()));
                }
            }
            all.addAll(reported);
            final Set<Integer> lines = new HashSet<>();
            final List<Problem> result = new ArrayList<>();
            for (final Problem problem : all) {
                if (lines.add(problem.line())) {
                    result.add(problem);
                }
            }
            result.sort(Comparator.comparingInt(Problem::line).thenComparingInt(Problem::column));
            problems = result;
        }
        return problems;
    }

    /**
     * @return the issues that were found, with their positions
     */
    public List<ScriptDiagnostic> getDiagnostics() {
        return problems().stream()
                .map(p -> ScriptDiagnostic.error(p.line(), p.column(), p.line(), p.endColumn(), p.message()))
                .toList();
    }

    /**
     * @return the issues that were found
     */
    public List<String> getIssues() {
        final @Unmodifiable List<String> lines = List.of(source.split("\r?\n\r?"));
        final List<String> issues = new ArrayList<>();
        for (final Problem problem : problems()) {
            final String errorLine = ScriptSource.getLine(lines, problem.line());
            if (errorLine != null && !errorLine.isBlank()) {
                issues.add("line %s:%s --> %s \n    %s".formatted(problem.line(), problem.column(), problem.message(), errorLine));
            } else {
                issues.add("line %s:%s --> %s".formatted(problem.line(), problem.column(), problem.message()));
            }
        }
        return issues;
    }

    /**
     * @return true if there were any issues
     */
    public boolean hasIssues() {
        return !problems().isEmpty();
    }

    /**
     * @return the number of issues
     */
    public int numberOfIssues() {
        return problems().size();
    }
}
