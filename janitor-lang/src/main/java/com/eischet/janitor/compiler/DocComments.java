// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.compiler;

import com.eischet.janitor.api.scopes.NameDoc;
import com.eischet.janitor.lang.JanitorLexer;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.Token;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * The doc comments of a script: a comment that starts with {@code /**} documents what follows it, like in Java and
 * TypeScript. Other comments ({@code /* ... *}{@code /}, {@code //}) are no documentation, and neither is the empty
 * comment {@code /**}{@code /}.
 * <p>
 * The lexer keeps comments on the hidden channel, so the parser does not see them. This class lexes the source once more
 * and remembers, for each token of the default channel, the last doc comment between it and the token before it. Callers
 * ask by the start offset of a token, e.g. of the first token of a statement, a map key or a parameter.
 * </p>
 */
public final class DocComments {

    private static final DocComments NONE = new DocComments(Map.of());

    private final Map<Integer, String> textsByTokenStart;

    private DocComments(final Map<Integer, String> textsByTokenStart) {
        this.textsByTokenStart = textsByTokenStart;
    }

    /**
     * Collects the doc comments of a script.
     *
     * @param source the source code
     * @return the doc comments
     */
    public static @NotNull DocComments of(final @Nullable String source) {
        if (source == null || !source.contains("/**")) {
            return NONE;
        }
        final JanitorLexer lexer = new JanitorLexer(CharStreams.fromString(source));
        lexer.removeErrorListeners();
        final Map<Integer, String> texts = new HashMap<>();
        String pending = null;
        for (Token token = lexer.nextToken(); token.getType() != Token.EOF; token = lexer.nextToken()) {
            if (token.getChannel() == Token.DEFAULT_CHANNEL) {
                if (pending != null) {
                    texts.put(token.getStartIndex(), pending);
                    pending = null;
                }
            } else if (isDocComment(token)) {
                final String text = NameDoc.textOf(token.getText());
                if (text != null) {
                    pending = text;
                }
            }
        }
        return new DocComments(texts);
    }

    /**
     * @param token a token
     * @return true if the token is a doc comment
     */
    public static boolean isDocComment(final @NotNull Token token) {
        final String text = token.getText();
        return token.getType() == JanitorLexer.COMMENT && text.startsWith("/**") && !"/**/".equals(text);
    }

    /**
     * @param token the first token of what may be documented
     * @return the text of the doc comment in front of it, or null
     */
    public @Nullable String before(final @Nullable Token token) {
        return token == null ? null : textsByTokenStart.get(token.getStartIndex());
    }

}
