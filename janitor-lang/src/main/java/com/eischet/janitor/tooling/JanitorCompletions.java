// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.tooling;

import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.JanitorRuntime;
import com.eischet.janitor.api.metadata.HasMetaData;
import com.eischet.janitor.api.metadata.ScriptType;
import com.eischet.janitor.api.JanitorEnvironment;
import com.eischet.janitor.api.modules.JanitorModuleRegistration;
import com.eischet.janitor.api.modules.ModuleResolver;
import com.eischet.janitor.api.scopes.NameDoc;
import com.eischet.janitor.api.scopes.Scope;
import com.eischet.janitor.api.scopes.VariableScope;
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
 * A member suggestion knows the receiver for literals ({@code "abc".}, {@code [1].}) and for expressions that start at a
 * global, one the host declares for the script ({@link ScriptBindings}) or one of the environment, and go on with
 * properties and methods whose types are known: {@link Janitor.MetaData#RETURNS}, {@link Janitor.MetaData#DISPATCHER} and
 * {@link Janitor.MetaData#TYPE_HINT} of properties, {@link Janitor.MetaData#RETURNS} of methods
 * ({@code shop.Order.findById(1).customer.}). It also knows the
 * parameters of a lambda passed to a method with {@link Janitor.MetaData#PARAMETERS}
 * ({@code shop.Order.select(o -> o.}), variables assigned such an expression and the variables of
 * {@code for (x in list)} over one. After {@code import} it offers the modules the environment registers, and a name an
 * import binds has the members of its module, if the registration names its dispatcher
 * ({@link JanitorModuleRegistration#withDispatcher}); for {@code import "name" as x}, if a resolver has the module's scope at hand
 * ({@link ModuleResolver#describeModuleByStringName}). Otherwise it offers the members of all built-in types, with the types that have them.
 * Build one instance per environment and keep it: it reads the dispatchers once.
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

    /** Suggestions of modules after {@code import} are variables with this owner. */
    public static final String MODULE = "module";

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
    private final JanitorEnvironment environment;
    private final VariableScope builtinScope;
    private final Set<String> commonNames;
    // type name -> members; COMMON first
    private final Map<String, List<DispatcherMembers.Member>> typeMembers = new LinkedHashMap<>();
    private final List<Completion> allMembers;
    // the modules scripts can import by name; the first registration of a name wins, as for import
    private final Map<String, JanitorModuleRegistration> modules = new TreeMap<>();

    /**
     * Read keywords, globals and the members of the built-in types.
     * @param runtime a runtime of the environment the scripts run in; used to evaluate samples of the built-in types
     */
    public JanitorCompletions(final @NotNull JanitorRuntime runtime) {
        keywords = keywords();
        environment = runtime.getEnvironment();
        builtinScope = environment.getBuiltinScope();
        globals = builtinScope.names().stream()
                .filter(name -> !name.startsWith("__"))
                .sorted()
                .map(name -> global(name, builtinScope.getVariable(name)))
                .toList();
        final Dispatcher<?> base = runtime.getEnvironment().getBuiltinTypes().internals().getBaseDispatcher();
        commonNames = base.streamAttributeNames().collect(Collectors.toSet());
        typeMembers.put(COMMON, DispatcherMembers.of(base, Set.of()));
        SAMPLES.forEach((type, sample) -> {
            try {
                typeMembers.put(type, DispatcherMembers.of(dispatcherOf(runtime.compile("completions", sample).run(runtime)), commonNames));
            } catch (Exception e) {
                log.warn("cannot list the members of {}", type, e);
            }
        });
        allMembers = merge(typeMembers.keySet());
        for (final JanitorModuleRegistration registration : runtime.getEnvironment().getModuleRegistrations()) {
            modules.putIfAbsent(registration.getQualifiedName(), registration);
        }
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
        if (last >= 0 && expectsModuleName(before, last)) {
            return modules.keySet().stream().map(name -> new Completion(name, Kind.VARIABLE, name, "", MODULE)).toList();
        }
        if (last >= 0 && isDot(before.get(last))) {
            final Expression receiver = last > 0 ? new Inference(before, declared).expressionEndingAt(last - 1, 0) : null;
            final List<Completion> known = receiver == null ? null : membersOf(receiver.type());
            return withDocumentedMembers(source, before, last - 1, known != null ? known : members(last > 0 ? before.get(last - 1) : null));
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

    // the members of a known type, or null if the type tells nothing, e.g. ANY
    private @Nullable List<Completion> membersOf(final @NotNull ScriptType type) {
        return switch (type) {
            case ScriptType.Of of -> membersOf(of.dispatcher(), of.name());
            case ScriptType.ListOf ignored -> merge(List.of("list", COMMON));
            case ScriptType.Builtin builtin -> typeMembers.containsKey(builtin.name()) && !builtin.name().equals(COMMON)
                    ? merge(List.of(builtin.name(), COMMON)) : null;
            default -> null;
        };
    }

    /**
     * An expression that ends before a dot, with what is known about it.
     *
     * @param type  its type
     * @param start the index of its first token
     */
    private record Expression(@NotNull ScriptType type, int start) {
    }

    /**
     * Follows the tokens before the cursor back to what an expression is. It knows globals, chains of properties and
     * methods with types ({@link Janitor.MetaData#DISPATCHER}, {@link Janitor.MetaData#TYPE_HINT},
     * {@link Janitor.MetaData#RETURNS}), the parameters of lambdas passed to such methods ({@link Janitor.MetaData#PARAMETERS}),
     * variables assigned once and the variables of {@code for (x in list)}. Everything else is unknown: then there is no
     * type, never an error.
     */
    private final class Inference {

        private static final int MAX_DEPTH = 20;

        private final List<Token> tokens;
        private final List<ScriptBindings.Declaration> declared;

        private Inference(final List<Token> tokens, final List<ScriptBindings.Declaration> declared) {
            this.tokens = tokens;
            this.declared = declared;
        }

        // the expression whose last token is at index end, or null if it is unknown
        private @Nullable Expression expressionEndingAt(final int end, final int depth) {
            if (end < 0 || depth > MAX_DEPTH) {
                return null;
            }
            final Token token = tokens.get(end);
            final String literal = literalType(token.getType());
            if (literal != null) {
                return new Expression(new ScriptType.Builtin(literal), end);
            }
            switch (token.getType()) {
                case JanitorLexer.IDENTIFIER -> {
                    if (end >= 2 && isDot(tokens.get(end - 1))) {
                        final Expression receiver = expressionEndingAt(end - 2, depth + 1);
                        final ScriptType type = receiver == null ? null : propertyType(receiver.type(), token.getText());
                        return type == null ? null : new Expression(type, receiver.start());
                    }
                    final ScriptType type = nameType(token.getText(), end, depth);
                    return type == null ? null : new Expression(type, end);
                }
                case JanitorLexer.RPAREN -> {
                    final int open = opener(end);
                    if (open < 3 || tokens.get(open - 1).getType() != JanitorLexer.IDENTIFIER || !isDot(tokens.get(open - 2))) {
                        return null; // a function call or brackets
                    }
                    final Expression receiver = expressionEndingAt(open - 3, depth + 1);
                    final ScriptType type = receiver == null ? null : returnType(receiver.type(), tokens.get(open - 1).getText());
                    return type == null ? null : new Expression(type, receiver.start());
                }
                case JanitorLexer.RBRACK -> {
                    final int open = opener(end);
                    if (open < 0) {
                        return null;
                    }
                    if (open == 0 || !endsAnExpression(tokens.get(open - 1))) {
                        return new Expression(ScriptType.LIST, open); // a list literal
                    }
                    final Expression list = expressionEndingAt(open - 1, depth + 1);
                    return list != null && list.type() instanceof ScriptType.ListOf listOf && listOf.element() != ScriptType.ANY
                            ? new Expression(listOf.element(), list.start()) : null;
                }
                default -> {
                    return null;
                }
            }
        }

        // the type of a name at index at: a lambda parameter, a loop variable or an assigned variable, else a global
        private @Nullable ScriptType nameType(final String name, final int at, final int depth) {
            final ScriptType parameter = lambdaParameterType(name, at, depth);
            if (parameter != null) {
                return parameter;
            }
            for (int i = at - 1; i >= 0; i--) {
                if (tokens.get(i).getType() != JanitorLexer.IDENTIFIER || !tokens.get(i).getText().equals(name)) {
                    continue;
                }
                if (i >= 2 && tokens.get(i - 1).getType() == JanitorLexer.LPAREN && tokens.get(i - 2).getType() == JanitorLexer.FOR
                    && i + 1 < at && tokens.get(i + 1).getType() == JanitorLexer.IN) {
                    final int close = closer(i - 1, at);
                    if (close < 0) {
                        return null;
                    }
                    final Expression list = expressionEndingAt(close - 1, depth + 1);
                    return list != null && list.start() == i + 2 && list.type() instanceof ScriptType.ListOf listOf ? listOf.element() : null;
                }
                if (i + 1 < at && tokens.get(i + 1).getType() == JanitorLexer.ASSIGN && (i == 0 || !isDot(tokens.get(i - 1)))) {
                    final int last = statementEnd(i + 2, at);
                    final Expression value = last < 0 ? null : expressionEndingAt(last, depth + 1);
                    return value != null && value.start() == i + 2 ? value.type() : null;
                }
            }
            final ScriptType module = importedModuleType(name, at);
            return module != null ? module : globalType(name);
        }

        // the type of a name that an import before index at binds: "import mcp;", "import mcp as m;" or "import "name" as m;"
        private @Nullable ScriptType importedModuleType(final String name, final int at) {
            for (int i = 0; i < at; i++) {
                if (tokens.get(i).getType() != JanitorLexer.IMPORT) {
                    continue;
                }
                for (int j = i + 1; j < at && isModuleName(tokens.get(j)); ) {
                    final boolean byString = tokens.get(j).getType() != JanitorLexer.IDENTIFIER;
                    final String module = byString ? unquote(tokens.get(j).getText()) : tokens.get(j).getText();
                    String bound = byString ? null : module;
                    int next = j + 1;
                    if (next + 1 < at && isAs(tokens.get(next)) && tokens.get(next + 1).getType() == JanitorLexer.IDENTIFIER) {
                        bound = tokens.get(next + 1).getText();
                        next += 2;
                    }
                    if (name.equals(bound)) {
                        return byString ? resolvedModuleType(module) : registeredModuleType(module);
                    }
                    if (next >= at || tokens.get(next).getType() != JanitorLexer.COMMA) {
                        break;
                    }
                    j = next + 1;
                }
            }
            return null;
        }

        private @Nullable ScriptType registeredModuleType(final String module) {
            final JanitorModuleRegistration registration = modules.get(module);
            final Dispatcher<?> dispatcher = registration == null ? null : registration.getDispatcher();
            return dispatcher == null ? null : new ScriptType.Of(module, dispatcher);
        }

        // a module imported by a string: the first resolver that has its scope at hand describes it
        private @Nullable ScriptType resolvedModuleType(final String module) {
            for (final ModuleResolver resolver : environment.getModuleResolvers()) {
                final VariableScope scope = resolver.describeModuleByStringName(module);
                if (scope != null) {
                    return new ScriptType.Of(module, ScopeDescription.of(module, scope));
                }
            }
            return null;
        }

        // the type of a lambda parameter, if the name at index at is one: the lambda is an argument of a method with PARAMETERS
        private @Nullable ScriptType lambdaParameterType(final String name, final int at, final int depth) {
            int level = 0;
            int lowest = 0;
            boolean ended = false; // an expression body ended (comma or semicolon) at the lowest level
            for (int i = at - 1; i >= 0; i--) {
                final int type = tokens.get(i).getType();
                if (isCloser(type)) {
                    level++;
                } else if (isOpener(type)) {
                    level--;
                    if (level < lowest) {
                        lowest = level;
                        ended = false;
                    }
                } else if (level == lowest && (type == JanitorLexer.COMMA || type == JanitorLexer.SEMICOLON)) {
                    ended = true;
                } else if (level == lowest && !ended && type == JanitorLexer.ARROW) {
                    final ScriptType parameter = parameterOfLambda(i, name, depth);
                    if (parameter != null) {
                        return parameter;
                    }
                }
            }
            return null;
        }

        // the type of the parameter name of the lambda whose arrow is at index arrow, or null
        private @Nullable ScriptType parameterOfLambda(final int arrow, final String name, final int depth) {
            final List<String> parameters = new ArrayList<>();
            final int start;
            if (arrow >= 1 && tokens.get(arrow - 1).getType() == JanitorLexer.IDENTIFIER) {
                parameters.add(tokens.get(arrow - 1).getText());
                start = arrow - 1;
            } else if (arrow >= 1 && tokens.get(arrow - 1).getType() == JanitorLexer.RPAREN) {
                start = opener(arrow - 1);
                if (start < 0) {
                    return null;
                }
                for (int i = start + 1; i < arrow - 1; i++) {
                    if (tokens.get(i).getType() == JanitorLexer.IDENTIFIER) {
                        parameters.add(tokens.get(i).getText());
                    }
                }
            } else {
                return null;
            }
            final int index = parameters.indexOf(name);
            if (index < 0) {
                return null;
            }
            // the call the lambda is an argument of: receiver.method(..., lambda
            int argument = 0;
            int level = 0;
            int open = -1;
            for (int i = start - 1; i >= 0; i--) {
                final int type = tokens.get(i).getType();
                if (isCloser(type)) {
                    level++;
                } else if (isOpener(type)) {
                    if (level == 0) {
                        open = i;
                        break;
                    }
                    level--;
                } else if (level == 0 && type == JanitorLexer.COMMA) {
                    argument++;
                }
            }
            if (open < 3 || tokens.get(open).getType() != JanitorLexer.LPAREN
                || tokens.get(open - 1).getType() != JanitorLexer.IDENTIFIER || !isDot(tokens.get(open - 2))) {
                return null;
            }
            final Expression receiver = expressionEndingAt(open - 3, depth + 1);
            if (receiver == null || !(receiver.type() instanceof ScriptType.Of of)) {
                return null;
            }
            final List<ScriptType.Parameter> declaredParameters = of.dispatcher().getMetaData(tokens.get(open - 1).getText(), Janitor.MetaData.PARAMETERS);
            if (declaredParameters == null || argument >= declaredParameters.size()) {
                return null;
            }
            final ScriptType expected = declaredParameters.get(argument).type().resolve(of.dispatcher());
            if (!(expected instanceof ScriptType.Function function) || index >= function.parameters().size()) {
                return null;
            }
            return known(function.parameters().get(index));
        }

        // the type of a global: declared by the host, or from the value in the environment
        private @Nullable ScriptType globalType(final String name) {
            for (final ScriptBindings.Declaration declaration : declared) {
                if (declaration.name().equals(name)) {
                    return known(declaration.type());
                }
            }
            final JanitorObject value = builtinScope.getVariable(name);
            final Dispatcher<?> dispatcher = value == null ? null : dispatcherOf(value);
            return dispatcher == null ? null : new ScriptType.Of(value.janitorClassName(), dispatcher);
        }

        // the index of the bracket that opens the one that closes at index close, or -1
        private int opener(final int close) {
            int level = 0;
            for (int i = close; i >= 0; i--) {
                final int type = tokens.get(i).getType();
                if (isCloser(type)) {
                    level++;
                } else if (isOpener(type) && --level == 0) {
                    return i;
                }
            }
            return -1;
        }

        // the index of the bracket that closes the one that opens at index open, before index limit, or -1
        private int closer(final int open, final int limit) {
            int level = 0;
            for (int i = open; i < limit; i++) {
                final int type = tokens.get(i).getType();
                if (isOpener(type)) {
                    level++;
                } else if (isCloser(type) && --level == 0) {
                    return i;
                }
            }
            return -1;
        }

        // the last token of the statement that starts at index from, before index limit, or -1 if it does not end before
        private int statementEnd(final int from, final int limit) {
            int level = 0;
            for (int i = from; i < limit; i++) {
                final Token token = tokens.get(i);
                if (isOpener(token.getType())) {
                    level++;
                } else if (isCloser(token.getType())) {
                    if (--level < 0) {
                        return -1;
                    }
                } else if (level == 0 && token.getType() == JanitorLexer.SEMICOLON) {
                    return i > from ? i - 1 : -1;
                }
                final Token next = i + 1 < limit ? tokens.get(i + 1) : null;
                if (level == 0 && next != null && next.getLine() > token.getLine() && endsAnExpression(token) && !isDot(next)) {
                    return i; // a line break ends the statement
                }
            }
            return -1;
        }
    }

    // the type of a property, from its RETURNS, its dispatcher or its type hint; null if unknown
    private static @Nullable ScriptType propertyType(final ScriptType owner, final String name) {
        if (!(owner instanceof ScriptType.Of of)) {
            return null;
        }
        final Janitor.MetaData.TypeHint hint = of.dispatcher().getMetaData(name, Janitor.MetaData.TYPE_HINT);
        if (hint == Janitor.MetaData.TypeHint.METHOD) {
            return null; // a method that is not called
        }
        final ScriptType returns = of.dispatcher().getMetaData(name, Janitor.MetaData.RETURNS);
        if (returns != null) {
            return known(returns.resolve(of.dispatcher()));
        }
        final Dispatcher<?> dispatcher = of.dispatcher().getMetaData(name, Janitor.MetaData.DISPATCHER);
        if (dispatcher != null) {
            return new ScriptType.Of(name, dispatcher);
        }
        return hint == null ? null : switch (hint) {
            case STRING -> ScriptType.STRING;
            case INTEGER -> ScriptType.INT;
            case FLOAT -> ScriptType.FLOAT;
            case BOOLEAN -> ScriptType.BOOL;
            case DATE -> ScriptType.DATE;
            case DATETIME -> ScriptType.DATETIME;
            case LIST -> ScriptType.LIST;
            case NUMBER, METHOD -> null;
        };
    }

    // what a method returns, resolved for its receiver; null if unknown
    private static @Nullable ScriptType returnType(final ScriptType owner, final String name) {
        if (!(owner instanceof ScriptType.Of of)) {
            return null;
        }
        final ScriptType returns = of.dispatcher().getMetaData(name, Janitor.MetaData.RETURNS);
        return returns == null ? null : known(returns.resolve(of.dispatcher()));
    }

    private static @Nullable ScriptType known(final ScriptType type) {
        return type == ScriptType.ANY ? null : type;
    }

    // whether a module name comes after the token at index last: "import |" or "import a, |" or "import a as b, |"
    private static boolean expectsModuleName(final List<Token> tokens, final int last) {
        int i = last;
        while (i >= 0 && tokens.get(i).getType() == JanitorLexer.COMMA) {
            // skip back over "name" or "name as alias" before the comma
            int j = i - 1;
            if (j >= 2 && tokens.get(j).getType() == JanitorLexer.IDENTIFIER && isAs(tokens.get(j - 1))) {
                j -= 2;
            }
            if (j < 0 || tokens.get(j).getType() != JanitorLexer.IDENTIFIER) {
                return false;
            }
            i = j - 1;
        }
        return i >= 0 && tokens.get(i).getType() == JanitorLexer.IMPORT;
    }

    // what may name a module after import: a name, or a string with "as" after it
    private static boolean isModuleName(final Token token) {
        return token.getType() == JanitorLexer.IDENTIFIER || token.getType() == JanitorLexer.STRING_LITERAL_SINGLE
               || token.getType() == JanitorLexer.STRING_LITERAL_DOUBLE;
    }

    private static String unquote(final String literal) {
        return literal.length() >= 2 ? literal.substring(1, literal.length() - 1) : literal;
    }

    private static boolean isAs(final Token token) {
        return "as".equals(token.getText()) && token.getType() != JanitorLexer.IDENTIFIER;
    }

    private static boolean isOpener(final int type) {
        return type == JanitorLexer.LPAREN || type == JanitorLexer.LBRACK || type == JanitorLexer.LBRACE;
    }

    private static boolean isCloser(final int type) {
        return type == JanitorLexer.RPAREN || type == JanitorLexer.RBRACK || type == JanitorLexer.RBRACE;
    }

    // tokens after which an expression may end: names, literals, closing brackets
    private static boolean endsAnExpression(final Token token) {
        final int type = token.getType();
        return type == JanitorLexer.IDENTIFIER || type == JanitorLexer.RPAREN || type == JanitorLexer.RBRACK
               || literalType(type) != null || type == JanitorLexer.TRUE || type == JanitorLexer.FALSE || type == JanitorLexer.NULL;
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

    /**
     * Puts the members that the script documents for a name path, e.g. the keys of the map literal assigned to
     * {@code config}, in front of the other suggestions after {@code config.}.
     */
    private static List<Completion> withDocumentedMembers(final String source, final List<Token> before, final int end, final List<Completion> others) {
        final List<String> path = new ArrayList<>();
        int i = end;
        while (i >= 0 && before.get(i).getType() == JanitorLexer.IDENTIFIER) {
            path.addFirst(before.get(i).getText());
            if (i >= 2 && isDot(before.get(i - 1))) {
                i -= 2;
            } else {
                break;
            }
        }
        if (path.isEmpty() || i < 0 || before.get(i).getType() != JanitorLexer.IDENTIFIER) {
            return others;
        }
        final NameDoc doc = ScriptDocs.lookup(ScriptDocs.of(source), path);
        if (doc == null || doc.members().isEmpty()) {
            return others;
        }
        final String owner = String.join(".", path);
        final List<Completion> result = new ArrayList<>();
        doc.members().forEach((name, member) -> result.add(new Completion(name,
                member.signature() != null ? Kind.METHOD : Kind.PROPERTY,
                member.signature() != null ? member.signature() : name,
                member.text() == null ? "" : member.text(), owner)));
        final Set<String> documented = doc.members().keySet();
        others.stream().filter(c -> !documented.contains(c.name())).forEach(result::add);
        return result;
    }

    // the names declared or used in the script, except the word at the cursor and member names, with their doc comments
    private static List<Completion> namesIn(final String source, final int offset) {
        final List<Token> all = tokens(source);
        final Map<String, NameDoc> docs = ScriptDocs.of(source);
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
        names.forEach((name, kind) -> {
            final NameDoc doc = docs.get(name);
            final String signature = doc == null ? null : doc.signature();
            final Kind shown = signature != null ? Kind.FUNCTION : kind;
            result.add(new Completion(name, shown, signature != null ? signature : shown == Kind.FUNCTION ? name + "(...)" : name,
                    doc == null || doc.text() == null ? "" : doc.text(), ""));
        });
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
