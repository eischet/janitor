// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.tooling;

import com.eischet.janitor.api.JanitorRuntime;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.api.types.dispatch.Dispatcher;
import com.eischet.janitor.api.types.dispatch.HasDispatcher;
import com.eischet.janitor.api.types.wrapped.JanitorWrapper;
import com.eischet.janitor.lang.JanitorLexer;
import com.eischet.janitor.logging.JanitorLogger;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.Token;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Suggestions for an editor: keywords, the environment's global functions and the names used in the script, and after a
 * dot the members of the built-in types with their HELP texts.
 * <p>
 * Without types, a member suggestion cannot know the receiver, except for literals ({@code "abc".}, {@code @2024-01-01.});
 * then it offers the members of all built-in types, with the types that have them. Build one instance per environment
 * and keep it: it reads the dispatchers once.
 * </p>
 */
public final class JanitorCompletions {

    private static final JanitorLogger log = JanitorLogger.getLogger(JanitorCompletions.class);

    /** What a suggestion is. */
    public enum Kind {
        KEYWORD, FUNCTION, VARIABLE, METHOD, PROPERTY
    }

    /**
     * A suggestion.
     *
     * @param name        what to insert
     * @param kind        what it is
     * @param signature   the call form, e.g. "indexOf(value)", or the name
     * @param description what it does, empty if unknown
     * @param owners      for members, the types that have it, e.g. "string, list"; else empty
     */
    public record Completion(@NotNull String name, @NotNull Kind kind, @NotNull String signature, @NotNull String description,
                             @NotNull String owners) {
    }

    /** Every object has these members; they are listed under this owner. */
    public static final String COMMON = "any";

    // samples of the built-in types, like the language reference uses them
    private static final Map<String, String> SAMPLES = new LinkedHashMap<>();

    static {
        SAMPLES.put("string", "\"\"");
        SAMPLES.put("int", "1");
        SAMPLES.put("float", "1.5");
        SAMPLES.put("list", "[]");
        SAMPLES.put("map", "return {};");
        SAMPLES.put("binary", "\"\".toBinaryUtf8()");
        SAMPLES.put("regex", "re/x/");
        SAMPLES.put("duration", "@1d");
        SAMPLES.put("date", "@2024-01-01");
        SAMPLES.put("datetime", "@2024-01-01-00:00");
    }

    private final List<Completion> keywords;
    private final List<Completion> globals;
    // type name -> members; COMMON first
    private final Map<String, List<DispatcherMembers.Member>> typeMembers = new LinkedHashMap<>();
    private final List<Completion> allMembers;

    /**
     * Read keywords, globals and the members of the built-in types.
     * @param runtime a runtime of the environment the scripts run in; used to evaluate samples of the built-in types
     */
    public JanitorCompletions(final @NotNull JanitorRuntime runtime) {
        keywords = keywords();
        globals = runtime.getEnvironment().getBuiltinScope().dir().stream()
                .filter(name -> !name.startsWith("__"))
                .sorted()
                .map(name -> new Completion(name, Kind.FUNCTION, name + "(...)", "", ""))
                .toList();
        final Dispatcher<?> base = runtime.getEnvironment().getBuiltinTypes().internals().getBaseDispatcher();
        final Set<String> commonNames = base.streamAttributeNames().collect(Collectors.toSet());
        typeMembers.put(COMMON, DispatcherMembers.of(base, Set.of()));
        SAMPLES.forEach((type, sample) -> {
            try {
                typeMembers.put(type, DispatcherMembers.of(dispatcherOf(runtime.compile("completions", sample).run()), commonNames));
            } catch (Exception e) {
                log.warn("cannot list the members of {}", type, e);
            }
        });
        allMembers = merge(typeMembers.keySet());
    }

    /**
     * Suggest what may be written at a position.
     * @param source the script
     * @param line   the cursor's line, starting at 1
     * @param column the cursor's column, starting at 0
     * @return the suggestions; the editor filters them by what is already typed
     */
    public @NotNull List<Completion> complete(final @NotNull String source, final int line, final int column) {
        final int offset = offsetOf(source, line, column);
        final List<Token> before = tokens(source.substring(0, offset));
        int last = before.size() - 1;
        // a word that is being typed belongs to the suggestion, not to the context
        if (last >= 0 && isWord(before.get(last)) && before.get(last).getStopIndex() + 1 == offset) {
            last--;
        }
        if (last >= 0 && isDot(before.get(last))) {
            return members(last > 0 ? before.get(last - 1) : null);
        }
        final List<Completion> result = new ArrayList<>(keywords);
        result.addAll(globals);
        final Set<String> known = globals.stream().map(Completion::name).collect(Collectors.toSet());
        namesIn(source, offset).stream().filter(c -> !known.contains(c.name())).forEach(result::add);
        return result;
    }

    private List<Completion> members(final @Nullable Token receiver) {
        final String type = receiver == null ? null : literalType(receiver.getType());
        if (type == null || !typeMembers.containsKey(type)) {
            return allMembers;
        }
        return merge(List.of(type, COMMON));
    }

    // members of several types, each name once, with the types that have it
    private List<Completion> merge(final Iterable<String> types) {
        final Map<String, DispatcherMembers.Member> first = new TreeMap<>();
        final Map<String, Set<String>> owners = new TreeMap<>();
        for (final String type : types) {
            for (final DispatcherMembers.Member member : typeMembers.getOrDefault(type, List.of())) {
                first.putIfAbsent(member.name(), member);
                owners.computeIfAbsent(member.name(), k -> new LinkedHashSet<>()).add(type);
            }
        }
        return first.values().stream()
                .map(m -> new Completion(m.name(), m.method() ? Kind.METHOD : Kind.PROPERTY, m.signature(), m.description(),
                        String.join(", ", owners.get(m.name()))))
                .toList();
    }

    // the names declared or used in the script, except the word at the cursor and member names
    private static List<Completion> namesIn(final String source, final int offset) {
        final List<Token> all = tokens(source);
        final Map<String, Kind> names = new TreeMap<>();
        for (int i = 0; i < all.size(); i++) {
            final Token token = all.get(i);
            if (token.getType() != JanitorLexer.IDENTIFIER) {
                continue;
            }
            if (token.getStartIndex() <= offset && offset <= token.getStopIndex() + 1) {
                continue;
            }
            final Token previous = i > 0 ? all.get(i - 1) : null;
            if (previous != null && isDot(previous)) {
                continue;
            }
            final boolean function = previous != null && previous.getType() == JanitorLexer.FUNCTION;
            names.merge(token.getText(), function ? Kind.FUNCTION : Kind.VARIABLE, (a, b) -> a == Kind.FUNCTION ? a : b);
        }
        final List<Completion> result = new ArrayList<>();
        names.forEach((name, kind) -> result.add(new Completion(name, kind, kind == Kind.FUNCTION ? name + "(...)" : name, "", "")));
        return result;
    }

    private static List<Completion> keywords() {
        final Set<String> words = new java.util.TreeSet<>();
        for (int type = 1; type <= JanitorLexer.VOCABULARY.getMaxTokenType(); type++) {
            final String literal = JanitorLexer.VOCABULARY.getLiteralName(type);
            if (literal != null && literal.matches("'[a-z]+'")) {
                words.add(literal.substring(1, literal.length() - 1));
            }
        }
        return words.stream().map(word -> new Completion(word, Kind.KEYWORD, word, "", "")).toList();
    }

    private static @Nullable String literalType(final int tokenType) {
        return switch (tokenType) {
            case JanitorLexer.STRING_LITERAL_SINGLE, JanitorLexer.STRING_LITERAL_DOUBLE,
                 JanitorLexer.STRING_LITERAL_TRIPLE_SINGLE, JanitorLexer.STRING_LITERAL_TRIPLE_DOUBLE -> "string";
            case JanitorLexer.DECIMAL_LITERAL, JanitorLexer.HEX_LITERAL, JanitorLexer.BINARY_LITERAL -> "int";
            case JanitorLexer.FLOAT_LITERAL -> "float";
            case JanitorLexer.REGEX_LITERAL -> "regex";
            case JanitorLexer.DATE_LITERAL, JanitorLexer.TODAY_LITERAL -> "date";
            case JanitorLexer.DATE_TIME_LITERAL, JanitorLexer.NOW_LITERAL -> "datetime";
            case JanitorLexer.YEARS_LITERAL, JanitorLexer.MONTHS_LITERAL, JanitorLexer.WEEKS_LITERAL, JanitorLexer.DAYS_LITERAL,
                 JanitorLexer.HOURS_LITERAL, JanitorLexer.MINUTES_LITERAL, JanitorLexer.SECONDS_LITERAL -> "duration";
            default -> null;
        };
    }

    private static boolean isDot(final Token token) {
        return token.getType() == JanitorLexer.DOT || token.getType() == JanitorLexer.QDOT;
    }

    // identifiers and keywords: what the cursor may be in the middle of
    private static boolean isWord(final Token token) {
        final String text = token.getText();
        return text != null && !text.isEmpty() && Character.isJavaIdentifierStart(text.charAt(0));
    }

    private static List<Token> tokens(final String text) {
        final JanitorLexer lexer = new JanitorLexer(CharStreams.fromString(text));
        lexer.removeErrorListeners();
        final List<Token> tokens = new ArrayList<>();
        for (Token token = lexer.nextToken(); token.getType() != Token.EOF; token = lexer.nextToken()) {
            if (token.getChannel() == Token.DEFAULT_CHANNEL) {
                tokens.add(token);
            }
        }
        return tokens;
    }

    private static int offsetOf(final String source, final int line, final int column) {
        int offset = 0;
        for (int current = 1; current < line; current++) {
            final int next = source.indexOf('\n', offset);
            if (next < 0) {
                return source.length();
            }
            offset = next + 1;
        }
        return Math.min(source.length(), offset + Math.max(0, column));
    }

    private static @Nullable Dispatcher<?> dispatcherOf(final JanitorObject object) {
        if (object instanceof HasDispatcher<?> hasDispatcher) {
            return hasDispatcher.getDispatcher();
        }
        if (object instanceof JanitorWrapper<?> wrapper) {
            return wrapper.getDispatcher();
        }
        return null;
    }
}
