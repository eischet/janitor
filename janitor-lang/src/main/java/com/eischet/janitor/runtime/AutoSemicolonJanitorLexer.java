package com.eischet.janitor.runtime;

import com.eischet.janitor.lang.JanitorLexer;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonToken;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenSource;
import org.antlr.v4.runtime.misc.Pair;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Set;

public class AutoSemicolonJanitorLexer extends JanitorLexer {
    // Nach diesen Token darf ein implizites ';' folgen (bewusst KEIN RBRACE/'}')
    private static final Set<Integer> MAY_END_STATEMENT = Set.of(
            IDENTIFIER,
            DECIMAL_LITERAL, HEX_LITERAL, OCT_LITERAL, BINARY_LITERAL, FLOAT_LITERAL,
            YEARS_LITERAL, MONTHS_LITERAL, WEEKS_LITERAL, DAYS_LITERAL, HOURS_LITERAL, MINUTES_LITERAL, SECONDS_LITERAL,
            DATE_LITERAL, DATE_TIME_LITERAL, TODAY_LITERAL, NOW_LITERAL,
            STRING_LITERAL_SINGLE, STRING_LITERAL_DOUBLE, STRING_LITERAL_TRIPLE_SINGLE, STRING_LITERAL_TRIPLE_DOUBLE,
            REGEX_LITERAL,
            TRUE, FALSE, NULL,
            RPAREN, RBRACK,
            INC, DEC,
            // leere Varianten erlauben:
            RETURN, BREAK, CONTINUE, THROW
    );
    private final Deque<Token> pending = new ArrayDeque<>();
    private Token prevVisible;

    public AutoSemicolonJanitorLexer(CharStream input) {
        super(input);
    }

    @Override
    public Token nextToken() {
        // 1. Falls noch gepufferte Tokens vorhanden, zuerst die zurückgeben
        if (!pending.isEmpty()) return pending.pollFirst();

        // 2. Nächstes echtes Token vom Basislexer holen
        Token t = super.nextToken();

        // --- FALL A: NEWLINE – ggf. implizites ';' einfügen ---
        if (t.getType() == NEWLINE) {
            Token lookahead = null;
            // mehrere Newlines überspringen
            do {
                lookahead = super.nextToken();
            } while (lookahead.getType() == NEWLINE);

            // Wenn davor ein Ausdruck enden durfte → Semikolon
            if (prevMayEndStatement()) {
                pending.addLast(makeSemiAfter(prevVisible, t));
            }
            // Wenn kein EOF → lookahead zurückstellen
            if (lookahead.getType() != Token.EOF) {
                pending.addLast(lookahead);
            } else {
                // falls Datei direkt endet → Semikolon + EOF
                if (prevMayEndStatement()) pending.addLast(makeSemiAtEOF(prevVisible, lookahead));
                pending.addLast(lookahead);
            }
            return pending.pollFirst();
        }

        // --- FALL B: EOF – evtl. Semikolon davor einfügen ---
        if (t.getType() == Token.EOF) {
            if (prevMayEndStatement()) {
                pending.addLast(makeSemiAtEOF(prevVisible, t));
            }
            pending.addLast(t);
            return pending.pollFirst();
        }

        // --- FALL C: Normales Token ---
        if (t.getChannel() == Token.DEFAULT_CHANNEL) prevVisible = t;
        return t;
    }

    private Token poll() {
        Token x = pending.pollFirst();
        if (x != null && x.getChannel() == Token.DEFAULT_CHANNEL) prevVisible = x;
        return x;
    }

    private boolean prevMayEndStatement() {
        return prevVisible != null && MAY_END_STATEMENT.contains(prevVisible.getType());
    }

    // liest bis zum nächsten Nicht-NEWLINE-Token; gibt null zurück, wenn EOF erreicht
    private Token fetchNextNonNewline() {
        Token t;
        do {
            t = super.nextToken();
        } while (t.getType() == NEWLINE);
        return (t.getType() == Token.EOF) ? null : t;
    }

    private CommonToken makeSemiAfter(Token prev, Token newline) {
        // Wir verankern das ';' am Newline-Start in der Eingabe (stabil für Intervals)
        int start = newline.getStartIndex();
        int line = prev.getLine();
        int col = prev.getCharPositionInLine() + (prev.getText() != null ? prev.getText().length() : 1);
        return createSemi(start, line, col);
    }

    private CommonToken makeSemiAtEOF(Token prev, Token eof) {
        int start = prev.getStopIndex() + 1; // virtuelle Pos direkt hinter dem letzten sichtbaren Token
        int line = prev.getLine();
        int col = prev.getCharPositionInLine() + (prev.getText() != null ? prev.getText().length() : 1);
        return createSemi(start, line, col);
    }

    private CommonToken createSemi(int start, int line, int col) {
        // WICHTIG: Pair(TokenSource, CharStream) benutzen – keine Setter vorhanden.
        CommonToken semi = new CommonToken(
                new Pair<TokenSource, CharStream>(this, _input),
                SEMICOLON,
                Token.DEFAULT_CHANNEL,
                start,
                start
        );
        semi.setText(";");
        semi.setLine(line);
        semi.setCharPositionInLine(col);
        return semi;
    }
}