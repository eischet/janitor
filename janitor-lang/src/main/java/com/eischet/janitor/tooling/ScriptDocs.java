// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.tooling;

import com.eischet.janitor.api.scopes.NameDoc;
import com.eischet.janitor.compiler.DocComments;
import com.eischet.janitor.lang.JanitorLexer;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.Token;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The documentation of the names a script defines, read from its tokens for editors, so that it works while the script
 * is incomplete, e.g. right after a dot. It finds what the compiler documents ({@link NameDoc}): {@code function}
 * declarations, assignments to a name or a member path, the keys of map literals and the parameters of functions and
 * lambdas. Scopes are not told apart; a later definition of the same name adds to an earlier one.
 */
final class ScriptDocs {

    private final List<Token> tokens;
    private final DocComments docComments;
    private final Map<String, NameDoc> names = new HashMap<>();

    private ScriptDocs(final String source) {
        this.tokens = new ArrayList<>();
        final JanitorLexer lexer = new JanitorLexer(CharStreams.fromString(source));
        lexer.removeErrorListeners();
        for (Token token = lexer.nextToken(); token.getType() != Token.EOF; token = lexer.nextToken()) {
            if (token.getChannel() == Token.DEFAULT_CHANNEL) {
                tokens.add(token);
            }
        }
        this.docComments = DocComments.of(source);
    }

    /**
     * @param source the script
     * @return the documentation of the names the script defines, by name
     */
    static @NotNull Map<String, NameDoc> of(final @NotNull String source) {
        final ScriptDocs docs = new ScriptDocs(source);
        docs.scan();
        return docs.names;
    }

    /**
     * @param names the documentation of the names of a script, from {@link #of}
     * @param path a name and member names, e.g. {@code ["config", "handlers"]}
     * @return the documentation at the end of the path, or null
     */
    static @Nullable NameDoc lookup(final @NotNull Map<String, NameDoc> names, final @NotNull List<String> path) {
        NameDoc doc = path.isEmpty() ? null : names.get(path.getFirst());
        for (int i = 1; doc != null && i < path.size(); i++) {
            doc = doc.member(path.get(i));
        }
        return doc;
    }

    private void scan() {
        for (int i = 0; i < tokens.size(); i++) {
            final Token token = tokens.get(i);
            if (token.getType() == JanitorLexer.FUNCTION && i + 2 < tokens.size()
                    && tokens.get(i + 1).getType() == JanitorLexer.IDENTIFIER && tokens.get(i + 2).getType() == JanitorLexer.LPAREN) {
                final String name = tokens.get(i + 1).getText();
                final int close = closing(i + 2);
                final List<NameDoc.Param> parameters = new ArrayList<>();
                final String signature = name + "(" + parameters(i + 3, close, parameters) + ")";
                add(List.of(name), new NameDoc(docComments.before(token), signature, parameters, Map.of()));
            } else if (token.getType() == JanitorLexer.IDENTIFIER && (i == 0 || !isDot(tokens.get(i - 1)))) {
                final List<String> path = new ArrayList<>();
                path.add(token.getText());
                int j = i + 1;
                while (j + 1 < tokens.size() && isDot(tokens.get(j)) && tokens.get(j + 1).getType() == JanitorLexer.IDENTIFIER) {
                    path.add(tokens.get(j + 1).getText());
                    j += 2;
                }
                if (j < tokens.size() && tokens.get(j).getType() == JanitorLexer.ASSIGN) {
                    final NameDoc doc = docOf(docComments.before(token), path.getLast(), j + 1);
                    if (doc != null) {
                        add(path, doc);
                    }
                }
            }
        }
    }

    private void add(final List<String> path, final NameDoc doc) {
        names.merge(path.getFirst(), doc.nestedIn(path.subList(1, path.size())), NameDoc::mergedWith);
    }

    // the documentation of a name whose value starts at index start: a lambda gets a signature, a map literal its keys
    private @Nullable NameDoc docOf(final @Nullable String text, final String name, final int start) {
        String signature = null;
        final List<NameDoc.Param> parameters = new ArrayList<>();
        final Map<String, NameDoc> members = new LinkedHashMap<>();
        if (start < tokens.size()) {
            final Token first = tokens.get(start);
            if (first.getType() == JanitorLexer.IDENTIFIER && start + 1 < tokens.size() && tokens.get(start + 1).getType() == JanitorLexer.ARROW) {
                signature = name + "(" + parameters(start, start + 1, parameters) + ")";
            } else if (first.getType() == JanitorLexer.LPAREN) {
                final int close = closing(start);
                if (close + 1 < tokens.size() && tokens.get(close + 1).getType() == JanitorLexer.ARROW) {
                    signature = name + "(" + parameters(start + 1, close, parameters) + ")";
                }
            } else if (first.getType() == JanitorLexer.LBRACE) {
                mapKeys(start, members);
            }
        }
        final NameDoc doc = new NameDoc(text, signature, parameters, members);
        return doc.isEmpty() ? null : doc;
    }

    // the keys of the map literal whose opening brace is at index open
    private void mapKeys(final int open, final Map<String, NameDoc> members) {
        final int close = closing(open);
        int i = open + 1;
        while (i + 1 < close && tokens.get(i + 1).getType() == JanitorLexer.COLON) {
            final Token key = tokens.get(i);
            final String name = keyOf(key);
            final NameDoc member = name == null ? null : docOf(docComments.before(key), name, i + 2);
            if (member != null) {
                members.merge(name, member, NameDoc::mergedWith);
            }
            i = endOfElement(i + 2, close) + 1;
        }
    }

    private static @Nullable String keyOf(final Token key) {
        final String text = key.getText();
        return switch (key.getType()) {
            case JanitorLexer.IDENTIFIER -> text;
            case JanitorLexer.STRING_LITERAL_SINGLE, JanitorLexer.STRING_LITERAL_DOUBLE -> text.substring(1, text.length() - 1);
            case JanitorLexer.STRING_LITERAL_TRIPLE_SINGLE, JanitorLexer.STRING_LITERAL_TRIPLE_DOUBLE -> text.substring(3, text.length() - 3);
            default -> null;
        };
    }

    // the parameters between the indexes from (inclusive) and to (exclusive), as written; collects their doc comments
    private String parameters(final int from, final int to, final List<NameDoc.Param> docs) {
        final List<String> written = new ArrayList<>();
        int start = from;
        while (start < to) {
            final int end = endOfElement(start, to);
            if (end > start) {
                final Token first = tokens.get(start);
                written.add(text(start, end));
                final Token nameToken = first.getType() == JanitorLexer.MUL || first.getType() == JanitorLexer.DOUBLE_STAR
                        ? tokens.get(Math.min(start + 1, end - 1)) : first;
                final String doc = docComments.before(first);
                if (doc != null) {
                    docs.add(new NameDoc.Param(nameToken.getText(), doc));
                }
            }
            start = end + 1;
        }
        return String.join(", ", written);
    }

    // the index of the comma or closing token that ends the element starting at start, at most limit
    private int endOfElement(final int start, final int limit) {
        int depth = 0;
        for (int i = start; i < limit && i < tokens.size(); i++) {
            final int type = tokens.get(i).getType();
            if (type == JanitorLexer.LPAREN || type == JanitorLexer.LBRACK || type == JanitorLexer.LBRACE) {
                depth++;
            } else if (type == JanitorLexer.RPAREN || type == JanitorLexer.RBRACK || type == JanitorLexer.RBRACE) {
                depth--;
            } else if (type == JanitorLexer.COMMA && depth == 0) {
                return i;
            }
        }
        return Math.min(limit, tokens.size());
    }

    // the index of the token that closes the bracket at index open, or the end of the tokens
    private int closing(final int open) {
        int depth = 0;
        for (int i = open; i < tokens.size(); i++) {
            final int type = tokens.get(i).getType();
            if (type == JanitorLexer.LPAREN || type == JanitorLexer.LBRACK || type == JanitorLexer.LBRACE) {
                depth++;
            } else if (type == JanitorLexer.RPAREN || type == JanitorLexer.RBRACK || type == JanitorLexer.RBRACE) {
                depth--;
                if (depth == 0) {
                    return i;
                }
            }
        }
        return tokens.size();
    }

    // the tokens between from (inclusive) and to (exclusive), with a space where the source has white space or comments
    private String text(final int from, final int to) {
        final StringBuilder out = new StringBuilder();
        for (int i = from; i < to; i++) {
            if (i > from && tokens.get(i - 1).getStopIndex() + 1 < tokens.get(i).getStartIndex()) {
                out.append(' ');
            }
            out.append(tokens.get(i).getText());
        }
        return out.toString();
    }

    private static boolean isDot(final Token token) {
        return token.getType() == JanitorLexer.DOT || token.getType() == JanitorLexer.QDOT;
    }

}
