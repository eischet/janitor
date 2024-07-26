package com.eischet.janitor.env;

import com.eischet.janitor.api.calls.JCallArgs;
import com.eischet.janitor.api.errors.runtime.JanitorAssertionException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.scopes.Location;
import com.eischet.janitor.api.scopes.Scope;
import com.eischet.janitor.api.scopes.ScriptModule;
import com.eischet.janitor.api.FilterPredicate;
import com.eischet.janitor.api.JanitorEnvironment;
import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.calls.JBoundMethod;
import com.eischet.janitor.api.calls.JUnboundMethod;
import com.eischet.janitor.api.errors.compiler.JanitorCompilerException;
import com.eischet.janitor.api.i18n.JanitorFormatting;
import com.eischet.janitor.api.types.builtin.*;
import com.eischet.janitor.api.types.dispatch.AttributeLookupHandler;
import com.eischet.janitor.api.util.JanitorSemantics;
import com.eischet.janitor.toolbox.json.api.JsonException;
import com.eischet.janitor.json.impl.JsonExportControls;
import com.eischet.janitor.toolbox.json.api.JsonInputStream;
import com.eischet.janitor.toolbox.json.api.JsonWriter;
import com.eischet.janitor.api.modules.JanitorModuleRegistration;
import com.eischet.janitor.api.types.*;
import com.eischet.janitor.api.util.DateTimeUtilities;
import com.eischet.janitor.api.util.ObjectUtilities;
import com.eischet.janitor.json.impl.GsonInputStream;
import com.eischet.janitor.json.impl.GsonOutputStream;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Default environment for the Janitor scripting language.
 * Create a subclass and override methods to customize the environment.
 * Every host application is expected to have at least one such subclass.
 */
public abstract class JanitorDefaultEnvironment implements JanitorEnvironment {

    public static final FilterPredicate NUMB = x -> true;

    private final JanitorFormatting formatting;

    private final Scope BUILTIN_SCOPE = Scope.createBuiltinScope(this, Location.at(ScriptModule.builtin(), 0, 0, 0, 0)); //  new Scope(Location.at(ScriptModule.builtin(), 0, 0), null, null);

    {
        BUILTIN_SCOPE.bindF("print", (rs, args) -> rs.getRuntime().print(rs, args));
        BUILTIN_SCOPE.bindF("assert", JanitorDefaultEnvironment::doAssert);
        BUILTIN_SCOPE.bind("__builtin__", BUILTIN_SCOPE); // not sure if this is actually a good idea, because that's a perfect circle of references.
        BUILTIN_SCOPE.seal();
    }

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


    private final Map<String, AttributeLookupHandler<? super JanitorObject>> anyAttributes = new HashMap<>();
    private final Map<String, AttributeLookupHandler<JDateTime>> dateTimeAttributes = new HashMap<>();
    private final Map<String, AttributeLookupHandler<JBinary>> binaryMethods = new HashMap<>();
    private final JanitorDefaultBuiltins builtins;
    // TODO: Float is missing here in the old approach

    @Override
    public JanitorObject lookupClassAttribute(final @NotNull JanitorScriptProcess runningScript, final @NotNull JanitorObject instance, final @NotNull String attributeName) {
        // removed: Jstring, jlist, jmap, set, int
        if (instance instanceof JDateTime) {
            final AttributeLookupHandler<JDateTime> attr = dateTimeAttributes.get(attributeName);
            if (attr != null) {
                final JanitorObject lkup = attr.lookupAttribute((JDateTime) instance, runningScript);
                if (lkup != null) {
                    return lkup;
                }
            }
        } else if (instance instanceof JBinary binary) {
            final AttributeLookupHandler<JBinary> attr = binaryMethods.get(attributeName);
            if (attr != null) {
                final JanitorObject lkup = attr.lookupAttribute(binary, runningScript);
                if (lkup != null) {
                    return lkup;
                }
            }
        }
        final AttributeLookupHandler<? super JanitorObject> attributeBuilder = anyAttributes.get(attributeName);
        if (attributeBuilder != null) {
            return attributeBuilder.lookupAttribute(instance, runningScript);
        }
        return null;
    }


    public JanitorDefaultEnvironment(JanitorFormatting formatting) {
        this.formatting = formatting;
        this.builtins = new JanitorDefaultBuiltins();





        // addDateTimeAttribute("epoch", (runningScript, instance) -> JDateTimeClass.__epoch(instance, runningScript, JCallArgs.empty("epoch", runningScript)));
        addDateTimeAttribute("epoch", (instance, runningScript) -> JDateTimeClass.__epochAsAttribute(instance, runningScript));

        addDateTimeMethod("toEpoch", JDateTimeClass::__epoch);
        addDateTimeMethod("date", JDateTimeClass::__date);
        addDateTimeMethod("time", JDateTimeClass::__time);
        addDateTimeMethod("string", JDateTimeClass::__string);
        addDateTimeMethod("format", JDateTimeClass::__string);
        addDateTimeMethod("formatAtTimezone", JDateTimeClass::__formatAtTimezone);
        addDateTimeMethod("kw", JDateTimeClass::__kw);
        addDateTimeMethod("year", JDateTimeClass::__year);
        // LATER: Zeitzonen JZonedDateTime m.put("atZone", JDateTimeClass::__atZone);



    }

    public void addDateTimeAttribute(final String name, final AttributeLookupHandler<JDateTime> attributeBuilder) {
        dateTimeAttributes.put(name, attributeBuilder);
    }

    public void addDateTimeMethod(final String name, final JUnboundMethod<JDateTime> method) {
        addDateTimeAttribute(name, (instance, runningScript) -> new JBoundMethod<>(name, instance, method));
    }

    public <T extends JanitorObject> void addGeneric(final String name, Class<T> cls, final AttributeLookupHandler<T> attributeBuilder) {
        anyAttributes.put(name, new AttributeLookupHandler<JanitorObject>() {
            @Override
            public JanitorObject lookupAttribute(final JanitorObject instance, final JanitorScriptProcess runningScript) {
                if (cls.isInstance(instance)) {
                    return attributeBuilder.lookupAttribute(cls.cast(instance), runningScript);
                } else {
                    return null;
                }
            }
        });
    }


    @Override
    public @NotNull JanitorFormatting getFormatting() {
        return formatting;
    }

    @Override
    public void addModule(final @NotNull JanitorModuleRegistration registration) {

    }

    public void addAnyAttribute(final String name, final AttributeLookupHandler<? super JanitorObject> attributeBuilder) {
        anyAttributes.put(name, attributeBuilder);
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
            return JFloat.of(dou);
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
            return JDateTime.ofNullable(localDateTime);
        }
        if (o instanceof LocalDate localDate) {
            return JDateTime.ofNullable(localDate.atStartOfDay());
        }
        if (o instanceof Timestamp timestamp) {
            return JDateTime.ofNullable(DateTimeUtilities.convert(timestamp));
        }
        if (o instanceof Date date) {
            return JDateTime.ofNullable(DateTimeUtilities.convert(date));
        }
        if (o instanceof BigDecimal bigDecimal) {
            return JFloat.of(bigDecimal.doubleValue());
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

    public static FilterPredicate of(final JanitorEnvironment env, final String name, final String code) {
        return env.filterScript(name, code);
    }

    @NotNull
    @Override
    public JanitorDefaultBuiltins getBuiltins() {
        return builtins;
    }

    @Override
    public Scope getBuiltinScope() {
        return BUILTIN_SCOPE;
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
}
