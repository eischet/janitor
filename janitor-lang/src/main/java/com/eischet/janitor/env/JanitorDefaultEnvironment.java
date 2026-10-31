// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.env;

import com.eischet.janitor.api.FilterPredicate;
import com.eischet.janitor.api.JanitorEnvironment;
import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.metadata.HasMetaData;
import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.modules.DiscoverableModules;
import com.eischet.janitor.api.types.dispatch.HasDispatcher;
import com.eischet.janitor.api.types.functions.JCallArgs;
import com.eischet.janitor.api.errors.compiler.JanitorCompilerException;
import com.eischet.janitor.api.errors.runtime.JanitorAssertionException;
import com.eischet.janitor.api.errors.runtime.JanitorNameException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.i18n.JanitorFormatting;
import com.eischet.janitor.api.modules.JanitorModule;
import com.eischet.janitor.api.modules.JanitorModuleRegistration;
import com.eischet.janitor.api.scopes.Location;
import com.eischet.janitor.api.scopes.Scope;
import com.eischet.janitor.api.scopes.ScriptSource;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.api.types.builtin.JList;
import com.eischet.janitor.api.types.builtin.JMap;
import com.eischet.janitor.api.types.builtin.JNull;
import com.eischet.janitor.api.util.ObjectUtilities;
import com.eischet.janitor.logging.JanitorLogger;
import com.eischet.janitor.toolbox.time.DateTimeUtils;
import com.eischet.janitor.runtime.JanitorSemantics;
import com.eischet.janitor.api.modules.ModuleResolver;
import com.eischet.janitor.toolbox.json.api.JsonException;
import com.eischet.janitor.toolbox.json.api.JsonInputStream;
import com.eischet.janitor.toolbox.json.api.JsonReadMode;
import com.eischet.janitor.toolbox.json.api.JsonStreamFactory;
import com.eischet.janitor.toolbox.json.api.JsonWriteOptions;
import com.eischet.janitor.toolbox.json.api.JsonWriter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * Default environment for the Janitor scripting language.
 * Create a subclass and override methods to customize the environment.
 * Every host application is expected to have at least one such subclass.
 */
public abstract class JanitorDefaultEnvironment implements JanitorEnvironment {

    public static final FilterPredicate NUMB = x -> true;
    protected final DefaultBuiltinTypes builtins;
    private final JanitorFormatting formatting;
    private volatile long maxInstructionCount = 0;
    private volatile @Nullable Duration maxRunTime = null;
    private volatile @NotNull JsonWriteOptions jsonWriteOptions = JsonWriteOptions.COMPACT;
    private final List<JanitorModuleRegistration> moduleRegistrations = new ArrayList<>();
    private final List<ModuleResolver> resolvers = new ArrayList<>();
    protected @Nullable Scope.ImplicitObjectProvider implicitTemplateObjectProvider = null;
    private volatile @Nullable JsonStreamFactory jsonStreams = null;

    private final Scope builtinScope = Scope.createBuiltinScope(this, Location.virtual(ScriptSource.builtin()));

    {
        builtinScope.bindF("print", "print(values...): Prints the values, separated by spaces; where the output goes is up to the host, e.g. the console or a log.",
                (rs, args) -> rs.getRuntime().print(rs, args));
        builtinScope.bindF("assert", "assert(condition, message): Fails the script with an assertion error unless the condition is truthy; the message is optional. Returns the condition.",
                JanitorDefaultEnvironment::doAssert);
        builtinScope.bind("__builtin__", builtinScope); // not sure if this is actually a good idea, because that's a perfect circle of references.

        // Very experimental... and will not yet work like it does in Python, if ever.
        // For example, help(foo.bar) will print HELP from JInt when the bar property contains an integral number.
        // That's because we're using "unpack()" all over the place to get to the innermost value, which is
        // what we want in most cases, but maybe not here. I'm not sure if it's worth making things more complicated
        // just for this feature, though.
        builtinScope.bindF("help", "help(value): Returns the HELP text of the value, e.g. of a function or method, or null if it has none.", (rs, args) -> {
            final JanitorObject subject = args.get(0);
            if (subject instanceof HasMetaData hasMetaData) {
                return getBuiltinTypes().nullableString(hasMetaData.getMetaData(Janitor.MetaData.HELP));
            }
            if (subject instanceof HasDispatcher<?> hasDispatcher) {
                return getBuiltinTypes().nullableString(hasDispatcher.getDispatcher().getMetaData(Janitor.MetaData.HELP));
            }
            return JNull.NULL;
        });
        builtinScope.bindF("dir", "dir(value): Returns the sorted names of the value's properties and methods as a list of strings.", (rs, args) -> {
            final JanitorObject subject = args.get(0);
            if (subject instanceof HasDispatcher<?> hasDispatcher) {
                return deduplicatedListOfStrings(hasDispatcher.getDispatcher().streamAttributeNames());
            }
            return getBuiltinTypes().list(0);
        });
        builtinScope.seal();
    }

    private JList deduplicatedListOfStrings(final Stream<String> stream) {
        final TreeSet<String> ts = new TreeSet<>();
        stream.forEach(ts::add);
        return getBuiltinTypes().list(ts.stream().map(element -> getBuiltinTypes().string(element)));
    }

    public JanitorDefaultEnvironment(JanitorFormatting formatting) {
        this.formatting = formatting;
        this.builtins = new DefaultBuiltinTypes();
    }

    /**
     * Creates an environment that hands all warnings to the given consumer.
     * @param formatting the formatting rules, e.g. for dates
     * @param warningEmitter receives the warning messages
     * @return the new environment
     */
    public static JanitorDefaultEnvironment create(final JanitorFormatting formatting, final Consumer<String> warningEmitter) {
        return new JanitorDefaultEnvironment(formatting) {
            @Override
            public void warn(final String message) {
                warningEmitter.accept(message);
            }
        };
    }

    /**
     * Assert a condition.
     *
     * @param process the running script
     * @param args    the arguments
     * @return the condition
     * @throws JanitorRuntimeException if the condition is not true
     */
    public static JanitorObject doAssert(final JanitorScriptProcess process, final JCallArgs args) throws JanitorRuntimeException {
        // TODO: this is really not the right place to put this method, because it is not at all related to the scope. JanitorSemantics would be better, for example.
        // the condition is required, the message optional; this used to be require(0, 1), which rejected every message
        final JanitorObject condition = args.require(1, 2).get(0);
        final String message = args.getOptionalStringValue(1, "");
        if (!JanitorSemantics.isTruthy(condition)) {
            if (message == null || message.isBlank()) {
                throw new JanitorAssertionException(process, "assertion failed!");
            } else {
                throw new JanitorAssertionException(process, "assertion failed: " + message);
            }
        }
        return condition;
    }

    /**
     * Set up the builtin scope.
     * <p>Call this method to place variables/functions into the builtin scope.</p>
     * <p>Note that all scripts running against this environment will share these variables.
     * It is probably not a good idea to make them modifiable, because modifications will stick!</p>
     *
     * @param consumer a callback that receives the builtin scope
     */
    public void setupBuiltinScope(final Consumer<Scope> consumer) {
        consumer.accept(builtinScope);
    }

    @Override
    public long getMaxInstructionCount() {
        return maxInstructionCount;
    }

    /**
     * Sets the default limit for the number of instructions that a script process may run. Every process that is created
     * afterward takes it over; a host can still override it per run with {@link JanitorScriptProcess#setMaxInstructionCount(long)}.
     * @param maxInstructionCount the maximum number of instructions, or 0 for no limit
     */
    public void setMaxInstructionCount(final long maxInstructionCount) {
        this.maxInstructionCount = maxInstructionCount;
    }

    @Override
    public @Nullable Duration getMaxRunTime() {
        return maxRunTime;
    }

    /**
     * Sets the default limit for the time that a script process may run. Every process that is created afterward takes
     * it over; a host can still override it per run with {@link JanitorScriptProcess#setMaxRunTime(Duration)}.
     * @param maxRunTime the maximum run time, or null or zero for no limit
     */
    public void setMaxRunTime(final @Nullable Duration maxRunTime) {
        this.maxRunTime = maxRunTime;
    }

    @Override
    public @NotNull JsonWriteOptions getJsonWriteOptions() {
        return jsonWriteOptions;
    }

    /**
     * Sets the default options for JSON that scripts create. Every process that is created afterward takes them over;
     * a host can still override them per run with {@link JanitorScriptProcess#setJsonWriteOptions(JsonWriteOptions)}.
     * @param jsonWriteOptions the options, e.g. {@code JsonWriteOptions.COMPACT.withOmitConfidential(true)}
     */
    public void setJsonWriteOptions(final @NotNull JsonWriteOptions jsonWriteOptions) {
        this.jsonWriteOptions = Objects.requireNonNull(jsonWriteOptions, "jsonWriteOptions");
    }

    @Override
    public @NotNull JanitorFormatting getFormatting() {
        return formatting;
    }


    @Override
    public @Nullable JanitorObject nativeToScript(final @Nullable Object o) {
        if (o instanceof JanitorObject good) {
            return good;
        }
        if (o == null) {
            return JNull.NULL;
        }
        if (o instanceof Double dou) {
            return builtins.floatingPoint(dou);
        }
        if (o instanceof String string) {
            return builtins.string(string);
        }
        if (o instanceof Long lo) {
            return builtins.integer(lo);
        }
        if (o instanceof Integer in) {
            return builtins.integer(in);
        }
        if (o instanceof LocalDateTime localDateTime) {
            return builtins.nullableDateTime(localDateTime);
        }
        if (o instanceof LocalDate localDate) {
            return builtins.nullableDateTime(localDate.atStartOfDay());
        }
        if (o instanceof Timestamp timestamp) {
            return builtins.nullableDateTime(DateTimeUtils.convert(timestamp));
        }
        if (o instanceof Date date) {
            return builtins.nullableDateTime(DateTimeUtils.convert(date));
        }
        if (o instanceof BigDecimal bigDecimal) {
            return builtins.floatingPoint(bigDecimal.doubleValue());
        }
        if (o instanceof Boolean bool) {
            return Janitor.toBool(bool);
        }
        JanitorLogger log = JanitorLogger.getLogger(JanitorObject.class);
        log.warn("nativeToScript(object={} [{}]) --> don't know how to convert this into a script variable!", o, ObjectUtilities.simpleClassNameOf(o));
        return null;

    }

    @Override
    public String writeJson(final JsonWriter writer) throws JsonException {
        return getJsonStreams().write(JsonWriteOptions.COMPACT, writer);
    }

    @Override
    public @NotNull JsonInputStream getLenientJsonConsumer(final String json) {
        return getJsonStreams().reader(json, JsonReadMode.LENIENT);
    }

    @Override
    public @NotNull JsonStreamFactory getJsonStreams() {
        JsonStreamFactory factory = jsonStreams;
        if (factory == null) {
            // a benign race: factories are stateless, so creating one twice does no harm
            factory = createJsonStreamFactory();
            jsonStreams = factory;
        }
        return factory;
    }

    /**
     * Creates the factory for all JSON streams of this environment; called once, on first use.
     * Override this to choose an implementation, e.g. {@code return JsonStreamFactory.named("native");}
     * or an implementation of your own.
     * @return the factory; by default, {@link JsonStreamFactory#defaultFactory()}
     */
    protected @NotNull JsonStreamFactory createJsonStreamFactory() {
        return JsonStreamFactory.defaultFactory();
    }


    @Override
    public @NotNull FilterPredicate filterScript(final String name, final String code) {
        if (code == null || code.isEmpty() || code.trim().isEmpty()) {
            return NUMB;
        }
        // TODO accidentally broke the build here; add tests for old/new style filter scripts!
        try {
            return new FilterScript(this, name, code, process -> {});
        } catch (JanitorCompilerException janitorParserException) {
            warn("error compiling filter: " + code + ":" + janitorParserException);
            // TODO: report an exception -> exception(JanitorParserException);
            return NUMB;
        }
    }

    @Override
    public @NotNull FilterPredicate filterScript(@NotNull String name, @NotNull String code, Consumer<JanitorScriptProcess> setup) {
        if (code.isEmpty() || code.trim().isEmpty()) {
            return NUMB;
        }
        try {
            return new FilterScript(this, name, code, setup);
        } catch (JanitorCompilerException janitorParserException) {
            warn("error compiling filter: " + code + ":" + janitorParserException);
            // TODO: report an exception -> exception(JanitorParserException);
            return NUMB;
        }
    }

    @NotNull
    @Override
    public DefaultBuiltinTypes getBuiltinTypes() {
        return builtins;
    }

    @Override
    public Scope getBuiltinScope() {
        return builtinScope;
    }

    @Override
    public @NotNull JMap parseJsonToMap(final String json) throws JsonException {
        return JMapClass.parseJson(getBuiltinTypes().map(), json);
    }

    @Override
    public @NotNull JList parseJsonToList(final String json) throws JsonException {
        return JListClass.parseJson(getBuiltinTypes().list(), json);
    }

    @NotNull
    @Override
    public JanitorDefaultEnvironment addModule(final @NotNull JanitorModuleRegistration registration) {
        moduleRegistrations.add(registration);
        return this;
    }

    /** Registers all modules that are provided by {@link DiscoverableModules} services on the class path. */
    public void autoDiscoverModules() {
        for (final DiscoverableModules discoverableModules : ServiceLoader.load(DiscoverableModules.class)) {
            for (JanitorModuleRegistration registration : discoverableModules.getModules()) {
                addModule(registration);
            }
        }
    }


    @Override
    public @NotNull JanitorModule getModuleByQualifier(final JanitorScriptProcess process, final String name) throws JanitorRuntimeException {
        for (final JanitorModuleRegistration moduleRegistration : moduleRegistrations) {
            if (Objects.equals(name, moduleRegistration.getQualifiedName())) {
                return moduleRegistration.getModuleSupplier().getModule(process);
            }
        }
        throw new JanitorNameException(process, "Module not found: " + name);
    }


    /**
     * Adds a module resolver.
     * The resolvers will be called in reverse order of addition, so later resolvers can override earlier ones.

     * @param resolver a module resolver for string-based module names
     */
    @NotNull
    @Override
    public JanitorDefaultEnvironment addModuleResolver(final ModuleResolver resolver) {
        resolvers.addFirst(resolver);
        return this;
    }

    @Override
    public @NotNull JanitorModule getModuleByStringName(final JanitorScriptProcess process, final String name) throws JanitorRuntimeException {
        for (final ModuleResolver resolver : resolvers) {
            final JanitorModule module = resolver.resolveModuleByStringName(process, name);
            if (module != null) {
                return module;
            }
        }
        throw new JanitorNameException(process, "Module not found: '" + name + "'");
    }

    @Override
    public @Nullable Scope.ImplicitObjectProvider getImplicitTemplateObjectProvider() {
        return implicitTemplateObjectProvider;
    }

    @Override
    public void setImplicitTemplateObjectProvider(@Nullable Scope.ImplicitObjectProvider implicitTemplateObjectProvider) {
        this.implicitTemplateObjectProvider = implicitTemplateObjectProvider;
    }
}
