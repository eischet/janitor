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
import com.eischet.janitor.api.scopes.ScriptModule;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.api.types.builtin.JList;
import com.eischet.janitor.api.types.builtin.JMap;
import com.eischet.janitor.api.types.builtin.JNull;
import com.eischet.janitor.api.util.ObjectUtilities;
import com.eischet.janitor.json.impl.GsonInputStream;
import com.eischet.janitor.json.impl.GsonOutputStream;
import com.eischet.janitor.json.impl.JsonExportControls;
import com.eischet.janitor.runtime.DateTimeUtilities;
import com.eischet.janitor.runtime.JanitorSemantics;
import com.eischet.janitor.api.modules.ModuleResolver;
import com.eischet.janitor.toolbox.json.api.JsonException;
import com.eischet.janitor.toolbox.json.api.JsonInputStream;
import com.eischet.janitor.toolbox.json.api.JsonWriter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.Timestamp;
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
    private final List<JanitorModuleRegistration> moduleRegistrations = new ArrayList<>();
    private final List<ModuleResolver> resolvers = new ArrayList<>();

    private final Scope builtinScope = Scope.createBuiltinScope(this, Location.virtual(ScriptModule.builtin()));

    {
        builtinScope.bindF("print", (rs, args) -> rs.getRuntime().print(rs, args));
        builtinScope.bindF("assert", JanitorDefaultEnvironment::doAssert);
        builtinScope.bind("__builtin__", builtinScope); // not sure if this is actually a good idea, because that's a perfect circle of references.

        // Very experimental... and will not yet work like it does in Python, if ever.
        // For example, help(foo.bar) will print HELP from JInt when the bar property contains an integral number.
        // That's because we're using "unpack()" all over the place to get to the inner-most value, which is
        // what we want in most cases, but maybe not here. I'm not sure if it's worth it to make things more complicated
        // just for this feature, though.
        builtinScope.bindF("help", (rs, args) -> {
            final JanitorObject subject = args.get(0);
            if (subject instanceof HasMetaData hasMetaData) {
                return getBuiltinTypes().nullableString(hasMetaData.getMetaData(Janitor.MetaData.HELP));
            }
            if (subject instanceof HasDispatcher<?> hasDispatcher) {
                return getBuiltinTypes().nullableString(hasDispatcher.getDispatcher().getMetaData(Janitor.MetaData.HELP));
            }
            return JNull.NULL;
        });
        builtinScope.bindF("dir", (rs, args) -> {
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
        final JanitorObject condition = args.require(0, 1).get(0);
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
            return builtins.nullableDateTime(DateTimeUtilities.convert(timestamp));
        }
        if (o instanceof Date date) {
            return builtins.nullableDateTime(DateTimeUtilities.convert(date));
        }
        if (o instanceof BigDecimal bigDecimal) {
            return builtins.floatingPoint(bigDecimal.doubleValue());
        }
        if (o instanceof Boolean bool) {
            return Janitor.toBool(bool);
        }
        Logger log = LoggerFactory.getLogger(JanitorObject.class);
        log.warn("nativeToScript(object={} [{}]) --> don't know how to convert this into a script variable!", o, ObjectUtilities.simpleClassNameOf(o));
        return null;

    }

    @Override
    public String writeJson(final JsonWriter writer) throws JsonException {
        final GsonOutputStream.GsonStringOut out = new GsonOutputStream.GsonStringOut(JsonExportControls.standard());
        writer.writeJson(out);
        return out.getString();
    }

    @Override
    public JsonInputStream getLenientJsonConsumer(final String json) {
        return GsonInputStream.lenient(json);
    }


    @Override
    public FilterPredicate filterScript(final String name, final String code) {
        if (code == null || code.isEmpty() || code.trim().isEmpty()) {
            return NUMB;
        }
        // TODO accidentally broke the build here; add tests for old/new style filter scripts!
        try {
            return new FilterScript(this, name, code);
        } catch (JanitorCompilerException janitorParserException) {
            warn("error compiling filter: " + code + ":" + janitorParserException);
            // TODO: report an exception -> exception(JanitorParserException);
            return NUMB;
        }
    }

    @Override
    public @NotNull FilterPredicate filterScript(@NotNull String name, @NotNull String code, Consumer<Scope> globalsProvider) {
        if (code == null || code.isEmpty() || code.trim().isEmpty()) {
            return NUMB;
        }
        try {
            return new FilterScript(this, name, code, globalsProvider);
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
        return JMapClass.parseJson(getBuiltinTypes().map(), json, this);
    }

    @Override
    public @NotNull JList parseJsonToList(final String json) throws JsonException {
        return JListClass.parseJson(getBuiltinTypes().list(), json, this);
    }

    @Override
    public void addModule(final @NotNull JanitorModuleRegistration registration) {
        moduleRegistrations.add(registration);
    }

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
                return moduleRegistration.getModuleSupplier().get();
            }
        }
        throw new JanitorNameException(process, "Module not found: " + name);
    }


    /**
     * Adds a module resolver.
     * The resolvers will be called in reverse order of addition, so later resolvers can override earlier ones.

     * @param resolver a module resolver for string based module names
     */
    @Override
    public void addModuleResolver(final ModuleResolver resolver) {
        resolvers.add(0, resolver);
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

}
