package com.eischet.janitor.env;

import com.eischet.janitor.api.FilterPredicate;
import com.eischet.janitor.api.JanitorEnvironment;
import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.calls.JCallArgs;
import com.eischet.janitor.api.errors.compiler.JanitorCompilerException;
import com.eischet.janitor.api.errors.runtime.JanitorAssertionException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.i18n.JanitorFormatting;
import com.eischet.janitor.api.modules.JanitorModuleRegistration;
import com.eischet.janitor.api.scopes.Location;
import com.eischet.janitor.api.scopes.Scope;
import com.eischet.janitor.api.scopes.ScriptModule;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.api.types.builtin.*;
import com.eischet.janitor.api.util.ObjectUtilities;
import com.eischet.janitor.json.impl.GsonInputStream;
import com.eischet.janitor.json.impl.GsonOutputStream;
import com.eischet.janitor.json.impl.JsonExportControls;
import com.eischet.janitor.runtime.DateTimeUtilities;
import com.eischet.janitor.runtime.JanitorSemantics;
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
import java.util.Date;
import java.util.function.Consumer;

/**
 * Default environment for the Janitor scripting language.
 * Create a subclass and override methods to customize the environment.
 * Every host application is expected to have at least one such subclass.
 */
public abstract class JanitorDefaultEnvironment implements JanitorEnvironment {

    public static final FilterPredicate NUMB = x -> true;
    protected final JanitorDefaultBuiltins builtins;
    private final JanitorFormatting formatting;

    private final Scope builtinScope = Scope.createBuiltinScope(this, Location.at(
            ScriptModule.builtin(),
            0,
            0,
            0,
            0)); //  new Scope(Location.at(ScriptModule.builtin(), 0, 0), null, null);

    // TODO: date, datetime, float, list, set, map!

    /*

    // private final Dispatcher<JanitorWrapper<?>> dispatchAny = new DispatchTable<>();
    // private final DispatchTable<?> dispatchRoot = new DispatchTable<>(FOO, null);
    private final DispatchTable<JString> dispatchString = new DispatchTable<>(JString.CASTER, null);

    {
        //var disp = Dispatcher.chain(dispatchBuiltin, dispatchString);
        var x = JString.of("foo");
        dispatchString.dispatch(x, null, "foobar");
    }

     */

    {
        builtinScope.bindF("print", (rs, args) -> rs.getRuntime().print(rs, args));
        builtinScope.bindF("assert", JanitorDefaultEnvironment::doAssert);
        builtinScope.bind("__builtin__", builtinScope); // not sure if this is actually a good idea, because that's a perfect circle of references.
        builtinScope.seal();
    }
    // TODO: Float is missing here in the old approach

    public JanitorDefaultEnvironment(JanitorFormatting formatting) {
        this.formatting = formatting;
        this.builtins = new JanitorDefaultBuiltins();


    }

    public void setupBuiltinScope(final Consumer<Scope> consumer) {
        consumer.accept(builtinScope);
    }



    public static FilterPredicate of(final JanitorEnvironment env, final String name, final String code) {
        return env.filterScript(name, code);
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

    @Override
    public @NotNull JanitorFormatting getFormatting() {
        return formatting;
    }

    @Override
    public void addModule(final @NotNull JanitorModuleRegistration registration) {

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
            return JBool.of(bool);
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

    // TODO: das gehört ins ENV, nicht hierher!
    @Override
    public FilterPredicate filterScript(final String name, final String code) {
        if (code == null || code.isEmpty() || code.trim().isEmpty()) {
            return NUMB;
        }
        try {
            return new FilterScript(this, name, code);
        } catch (JanitorCompilerException JanitorParserException) {
            warn("error compiling filter: " + code + ":" + JanitorParserException);
            // TODO: report an exception -> exception(JanitorParserException);
            return NUMB;
        }
    }

    @NotNull
    @Override
    public JanitorDefaultBuiltins getBuiltins() {
        return builtins;
    }

    @Override
    public Scope getBuiltinScope() {
        return builtinScope;
    }

    @Override
    public @NotNull JMap parseJsonToMap(final String json) throws JsonException {
        return JMapClass.parseJson(getBuiltins().map(), json, this);
    }

    @Override
    public @NotNull JList parseJsonToList(final String json) throws JsonException {
        return JListClass.parseJson(getBuiltins().list(), json, this);
    }
}
