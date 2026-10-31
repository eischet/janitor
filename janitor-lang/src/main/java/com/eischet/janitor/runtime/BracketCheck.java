// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.runtime;

import com.eischet.janitor.lang.JanitorLexer;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.Token;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * Finds brackets that are not closed, or closed by the wrong one. The parser only notices them where it cannot go on, often lines
 * later and with a message about something else; this names the bracket. Brackets in strings, regexes and comments do not count.
 */
final class BracketCheck {

    private BracketCheck() {
    }

    /** A bracket problem at the position of the bracket: lines start at 1, columns at 0. */
    record Problem(int line, int column, String message) {
    }

    static List<Problem> check(final String source) {
        final JanitorLexer lexer = new JanitorLexer(CharStreams.fromString(source));
        lexer.removeErrorListeners();
        final Deque<Token> open = new ArrayDeque<>();
        final List<Problem> problems = new ArrayList<>();
        for (Token token = lexer.nextToken(); token.getType() != Token.EOF; token = lexer.nextToken()) {
            switch (token.getType()) {
                case JanitorLexer.LPAREN, JanitorLexer.LBRACK, JanitorLexer.LBRACE -> open.push(token);
                case JanitorLexer.RPAREN, JanitorLexer.RBRACK, JanitorLexer.RBRACE -> close(open, token, problems);
                default -> {
                }
            }
        }
        // the innermost first; an outer bracket is usually only open because of it
        for (final Token token : open) {
            problems.add(at(token, quote(token) + " is not closed"));
        }
        return problems;
    }

    private static void close(final Deque<Token> open, final Token closing, final List<Problem> problems) {
        final int opening = openingOf(closing.getType());
        if (open.stream().noneMatch(t -> t.getType() == opening)) {
            problems.add(at(closing, quote(closing) + " has no matching " + quote(opening) + " before it"));
            return;
        }
        // the brackets opened after the matching one are not closed
        while (open.peek().getType() != opening) {
            final Token unclosed = open.pop();
            final String where = unclosed.getLine() == closing.getLine() ? "" : " in line " + closing.getLine();
            problems.add(at(unclosed, quote(unclosed) + " is not closed before " + quote(closing) + where));
        }
        open.pop();
    }

    private static int openingOf(final int closing) {
        return switch (closing) {
            case JanitorLexer.RPAREN -> JanitorLexer.LPAREN;
            case JanitorLexer.RBRACK -> JanitorLexer.LBRACK;
            default -> JanitorLexer.LBRACE;
        };
    }

    private static Problem at(final Token token, final String message) {
        return new Problem(token.getLine(), token.getCharPositionInLine(), message);
    }

    private static String quote(final Token token) {
        return "'" + token.getText() + "'";
    }

    private static String quote(final int type) {
        return JanitorLexer.VOCABULARY.getLiteralName(type);
    }
}
