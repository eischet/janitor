// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.tooling;

import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.JanitorRuntime;
import com.eischet.janitor.api.metadata.HasMetaData;
import com.eischet.janitor.api.scopes.Scope;
import com.eischet.janitor.api.scopes.ScriptBindings;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.api.types.dispatch.Dispatcher;
import com.eischet.janitor.api.types.dispatch.HasDispatcher;
import com.eischet.janitor.api.types.functions.JCallable;
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
 * Without types, a member suggestion knows the receiver only for literals ({@code "abc".}, {@code @2024-01-01.}) and for
 * chains of names that start at a global: one the host declares for the script ({@link ScriptBindings}) or one of the
 * environment, followed by properties whose dispatchers are known ({@code shop.Order.}, through the
 * {@link Janitor.MetaData#DISPATCHER} metadata). Otherwise it offers the members of all built-in types, with the types that
 * have them. Build one instance per environment and keep it: it reads the dispatchers once.
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
    private final Scope builtinScope;
    private final Set<String> commonNames;
    // type name -> members; COMMON first
    private final Map<String, List<DispatcherMembers.Member>> typeMembers = new LinkedHashMap<>();
    private final List<Completion> allMembers;

    /**
     * Read keywords, globals and the members of the built-in types.
     * @param runtime a runtime of the environment the scripts run in; used to evaluate samples of the built-in types
     */
    public JanitorCompletions(final @NotNull JanitorRuntime runtime) {
        keywords = keywords();
        builtinScope = runtime.getEnvironment().getBuiltinScope();
        globals = builtinScope.dir().stream()
                .filter(name -> !name.startsWith("__"))
                .sorted()
                .map(name -> global(name, builtinScope.getVariable(name)))
                .toList();
        final Dispatcher<?> base = runtime.getEnvironment().getBuiltinTypes().internals().getBaseDispatcher();
        commonNames = base.streamAttributeNames().collect(Collectors.toSet());
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
        return complete(source, line, column, List.of());
    }

    /**
     * Suggest what may be written at a position, knowing the globals the host declares for the script.
     * @param source   the script
     * @param line     the cursor's line, starting at 1
     * @param column   the cursor's column, starting at 0
     * @param declared the globals the host declares, see {@link ScriptBindings#declarations()}
     * @return the suggestions; the editor filters them by what is already typed
     */
    public @NotNull List<Completion> complete(final @NotNull String source, final int line, final int column,
                                              final @NotNull List<ScriptBindings.Declaration> declared) {
        final int offset = offsetOf(source, line, column);
        final List<Token> before = tokens(source.substring(0, offset));
        int last = before.size() - 1;
        // a word that is being typed belongs to the suggestion, not to the context
        if (last >= 0 && isWord(before.get(last)) && before.get(last).getStopIndex() + 1 == offset) {
            last--;
        }
        if (last >= 0 && isDot(before.get(last))) {
            final Type type = receiverType(before, last, declared);
            if (type != null) {
                return type.dispatcher() != null ? membersOf(type.dispatcher(), type.name()) : merge(List.of(type.name(), COMMON));
            }
            return members(last > 0 ? before.get(last - 1) : null);
        }
        final List<Completion> result = new ArrayList<>(keywords);
        final Set<String> declaredNames = declared.stream().map(ScriptBindings.Declaration::name).collect(Collectors.toSet());
        globals.stream().filter(g -> !declaredNames.contains(g.name())).forEach(result::add);
        declared.forEach(d -> result.add(new Completion(d.name(), Kind.VARIABLE, d.name(), d.help(), d.typeName())));
        final Set<String> known = result.stream().filter(c -> c.kind() != Kind.KEYWORD).map(Completion::name).collect(Collectors.toSet());
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

    /**
     * The type of a value: a dispatcher, or the name of a built-in type that the suggestions know.
     *
     * @param name       the type's name as shown to the user
     * @param dispatcher its dispatcher; null for a built-in type
     */
    private record Type(@NotNull String name, @Nullable Dispatcher<?> dispatcher) {
    }

    // the type before the dot at index dot, if it is a chain of names starting at a global with a known type, e.g. shop.Order
    private @Nullable Type receiverType(final List<Token> tokens, final int dot, final List<ScriptBindings.Declaration> declared) {
        final List<String> names = new ArrayList<>();
        int i = dot - 1;
        while (i >= 0 && tokens.get(i).getType() == JanitorLexer.IDENTIFIER) {
            names.addFirst(tokens.get(i).getText());
            if (i == 0 || !isDot(tokens.get(i - 1))) {
                break;
            }
            i -= 2;
        }
        if (names.isEmpty() || i < 0 || tokens.get(i).getType() != JanitorLexer.IDENTIFIER) {
            return null; // e.g. foo().bar: the chain does not start at a name
        }
        Type type = globalType(names.getFirst(), declared);
        for (final String name : names.subList(1, names.size())) {
            if (type == null || type.dispatcher() == null) {
                return null;
            }
            final Dispatcher<?> next = type.dispatcher().getMetaData(name, Janitor.MetaData.DISPATCHER);
            type = next == null ? null : new Type(name, next);
        }
        if (type != null && type.dispatcher() == null && !typeMembers.containsKey(type.name())) {
            return null;
        }
        return type;
    }

    // the type of a global: declared by the host, or from the value in the environment
    private @Nullable Type globalType(final String name, final List<ScriptBindings.Declaration> declared) {
        for (final ScriptBindings.Declaration declaration : declared) {
            if (declaration.name().equals(name)) {
                return new Type(declaration.typeName(), declaration.dispatcher());
            }
        }
        final JanitorObject value = builtinScope.getVariable(name);
        final Dispatcher<?> dispatcher = value == null ? null : dispatcherOf(value);
        return dispatcher == null ? null : new Type(value.janitorClassName(), dispatcher);
    }

    // the members of a dispatcher, then those of every object
    private List<Completion> membersOf(final Dispatcher<?> dispatcher, final String owner) {
        final Map<String, Completion> result = new TreeMap<>();
        for (final DispatcherMembers.Member m : DispatcherMembers.of(dispatcher, commonNames)) {
            result.put(m.name(), new Completion(m.name(), m.method() ? Kind.METHOD : Kind.PROPERTY, m.signature(), m.description(), owner));
        }
        for (final DispatcherMembers.Member m : typeMembers.get(COMMON)) {
            result.putIfAbsent(m.name(), new Completion(m.name(), m.method() ? Kind.METHOD : Kind.PROPERTY, m.signature(), m.description(), COMMON));
        }
        return List.copyOf(result.values());
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

    // a global with the call form and description from its HELP text, if it has one; values that cannot be called are variables
    private static Completion global(final String name, final @Nullable JanitorObject value) {
        final String help = value instanceof HasMetaData hasMetaData ? hasMetaData.getMetaData(Janitor.MetaData.HELP) : null;
        if (value != null && !(value instanceof JCallable)) {
            return new Completion(name, Kind.VARIABLE, name, help == null ? "" : help.strip(), value.janitorClassName());
        }
        final DispatcherMembers.Member member = DispatcherMembers.member(name, true, help);
        return new Completion(name, Kind.FUNCTION, member.signature(), member.description(), "");
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
