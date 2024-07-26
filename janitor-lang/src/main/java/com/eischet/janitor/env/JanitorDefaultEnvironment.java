package com.eischet.janitor.env;

import com.eischet.janitor.api.scopes.Location;
import com.eischet.janitor.api.scopes.Scope;
import com.eischet.janitor.api.scopes.ScriptModule;
import com.eischet.janitor.api.scripting.*;
import com.eischet.janitor.api.FilterPredicate;
import com.eischet.janitor.api.JanitorEnvironment;
import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.calls.JBoundMethod;
import com.eischet.janitor.api.calls.JUnboundMethod;
import com.eischet.janitor.api.errors.compiler.JanitorCompilerException;
import com.eischet.janitor.api.i18n.JanitorFormatting;
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
        BUILTIN_SCOPE.bindF("assert", Scope::doAssert);
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
    private final Map<String, AttributeLookupHandler<JString>> stringAttributes = new HashMap<>();
    private final Map<String, AttributeLookupHandler<JDateTime>> dateTimeAttributes = new HashMap<>();
    private final Map<String, AttributeLookupHandler<JList>> listAttributes = new HashMap<>();
    private final Map<String, AttributeLookupHandler<JSet>> setAttributes = new HashMap<>();
    private final Map<String, AttributeLookupHandler<JMap>> mapAttributes = new HashMap<>();
    private final Map<String, AttributeLookupHandler<JInt>> intAttributes = new HashMap<>();
    private final Map<String, AttributeLookupHandler<JBinary>> binaryMethods = new HashMap<>();
    private final JanitorDefaultBuiltins builtins;
    // TODO: Float is missing here in the old approach

    @Override
    public JanitorObject lookupClassAttribute(final @NotNull JanitorScriptProcess runningScript, final @NotNull JanitorObject instance, final @NotNull String attributeName) {
        if (instance instanceof JString str) {
            final JanitorObject attr = lookupStringAttribute(runningScript, str, attributeName);
            if (attr != null) {
                return attr;
            }
        } else if (instance instanceof JDateTime) {
            final AttributeLookupHandler<JDateTime> attr = dateTimeAttributes.get(attributeName);
            if (attr != null) {
                final JanitorObject lkup = attr.lookupAttribute((JDateTime) instance, runningScript);
                if (lkup != null) {
                    return lkup;
                }
            }
        } else if (instance instanceof JList list) {
            final AttributeLookupHandler<JList> attr = listAttributes.get(attributeName);
            if (attr != null) {
                final JanitorObject lkup = attr.lookupAttribute(list, runningScript);
                if (lkup != null) {
                    return lkup;
                }
            }
        } else if (instance instanceof JSet set) {
            final AttributeLookupHandler<JSet> attr = setAttributes.get(attributeName);
            if (attr != null) {
                final JanitorObject lkup = attr.lookupAttribute(set, runningScript);
                if (lkup != null) {
                    return lkup;
                }
            }
        } else if (instance instanceof JMap map) {
            final AttributeLookupHandler<JMap> attr = mapAttributes.get(attributeName);
            if (attr != null) {
                final JanitorObject lkup = attr.lookupAttribute(map, runningScript);
                if (lkup != null) {
                    return lkup;
                }
            }
        } else if (instance instanceof JInt integer) {
            final AttributeLookupHandler<JInt> attr = intAttributes.get(attributeName);
            if (attr != null) {
                final JanitorObject lkup = attr.lookupAttribute(integer, runningScript);
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


        // TODO: these should all be replaced by proper DispatchTables, which were "invented" later.
        // MOVED TO JanitorDefaultBuiltings: addStringMethod("length", JStringClass::__length);
        addStringMethod("trim", JStringClass::__trim);
        addStringMethod("contains", JStringClass::__contains);
        addStringMethod("containsIgnoreCase", JStringClass::__containsIgnoreCase);
        addStringMethod("splitLines", JStringClass::__splitLines);
        addStringMethod("indexOf", JStringClass::__indexOf);
        addStringMethod("empty", JStringClass::__empty);
        addStringMethod("startsWith", JStringClass::__startsWith);
        addStringMethod("endsWith", JStringClass::__endsWith);
        addStringMethod("removeLeadingZeros", JStringClass::__removeLeadingZeros);
        addStringMethod("substring", JStringClass::__substring);
        addStringMethod("replaceAll", JStringClass::__replaceAll);
        addStringMethod("replace", JStringClass::__replace);
        addStringMethod("replaceFirst", JStringClass::__replaceFirst);
        addStringMethod("toUpperCase", JStringClass::__toUpperCase);
        addStringMethod("toLowerCase", JStringClass::__toLowerCase);
        addStringMethod("count", JStringClass::__count);
        addStringMethod("format", JStringClass::__format);
        addStringMethod("expand", JStringClass::__expand);
        addStringMethod("toBinaryUtf8", JStringClass::__toBinaryUtf8);
        addStringMethod("int", JStringClass::__toInt);
        addStringMethod("toInt", JStringClass::__toInt);
        addStringMethod("toFloat", JStringClass::__toFloat);
        addStringMethod("get", JStringClass::__get);
        addStringMethod("__get__", JStringClass::__get); // das lassen wir auch so: keine Zuweisung per Index an String-Teile, die sind ja immutable
        addStringMethod("isNumeric", JStringClass::__isNumeric);
        addStringMethod("startsWithNumbers", JStringClass::__startsWithNumbers);
        addStringMethod("parseDate", JStringClass::__parseDate);
        addStringMethod("parseDateTime", JStringClass::__parseDateTime);
        addStringMethod("cutFilename", JStringClass::__cutFilename);
        addStringMethod("urlEncode", JStringClass::__urlEncode);
        addStringMethod("urlDecode", JStringClass::__urlDecode);
        addStringMethod("decodeBase64", JStringClass::__decodeBase64);


        // addDateTimeAttribute("epoch", (runningScript, instance) -> JDateTimeClass.__epoch(instance, runningScript, JCallArgs.empty("epoch", runningScript)));
        addDateTimeAttribute("epoch", (instance, runningScript) -> JDateTimeClass.__epochAsAttribute(instance));
        addDateTimeMethod("toEpoch", JDateTimeClass::__epoch);
        addDateTimeMethod("date", JDateTimeClass::__date);
        addDateTimeMethod("time", JDateTimeClass::__time);
        addDateTimeMethod("string", JDateTimeClass::__string);
        addDateTimeMethod("format", JDateTimeClass::__string);
        addDateTimeMethod("formatAtTimezone", JDateTimeClass::__formatAtTimezone);
        addDateTimeMethod("kw", JDateTimeClass::__kw);
        addDateTimeMethod("year", JDateTimeClass::__year);
        // LATER: Zeitzonen JZonedDateTime m.put("atZone", JDateTimeClass::__atZone);

        addListMethod("toJson", JListOperations::__toJson);
        addListMethod("parseJson", JListOperations::__parseJson);
        addListMethod("count", JListOperations::__count);
        addListMethod("filter", JListOperations::__filter);
        addListMethod("map", JListOperations::__map);
        addListMethod("join", JListOperations::__join);
        addListMethod("toSet", JListOperations::__toSet);
        addListMethod("size", JListOperations::__size);
        addListMethod("isEmpty", JListOperations::__isEmpty);
        addListMethod("contains", JListOperations::__contains);
        addListMethod("randomSublist", JListOperations::__randomSublist);
        addListMethod("addAll", JListOperations::__addAll);
        addListMethod("put", JListOperations::__put);
        addListMethod("add", JListOperations::__add);
        addListMethod("get", JListOperations::__get);
        addListMethod("__get__", JListOperations::__getSliced);

        addSetMethod("add", JSetClass::__add);
        addSetMethod("remove", JSetClass::__remove);
        addSetMethod("contains", JSetClass::__contains);
        addSetMethod("toList", JSetClass::__toList);
        addSetMethod("size", JSetClass::__size);
        addSetMethod("isEmpty", JSetClass::__isEmpty);

        addMapMethod("toJson", JMap::__toJson);
        addMapMethod("parseJson", JMap::__parseJson);
    }

    public void addStringAttribute(final String name, final AttributeLookupHandler<JString> attributeBuilder) {
        stringAttributes.put(name, attributeBuilder);
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


    public void addListMethod(final String name, final JUnboundMethod<JList> method) {
        listAttributes.put(name, (instance, runningScript) -> new JBoundMethod<>(name, instance, method));
    }

    public void addSetMethod(final String name, final JUnboundMethod<JSet> method) {
        setAttributes.put(name, (instance, runningScript) -> new JBoundMethod<>(name, instance, method));
    }

    public void addMapMethod(final String name, final JUnboundMethod<JMap> method) {
        mapAttributes.put(name, (instance, runningScript) -> new JBoundMethod<>(name, instance, method));
    }

    public void addStringMethod(final String name, final JUnboundMethod<JString> method) {
        stringAttributes.put(name, (instance, runningScript) -> new JBoundMethod<>(name, instance, method));
    }

    @Override
    public @NotNull JanitorFormatting getFormatting() {
        return formatting;
    }

    @Override
    public void addModule(final @NotNull JanitorModuleRegistration registration) {

    }


    private JanitorObject lookupStringAttribute(final JanitorScriptProcess runningScript, final JString instance, final String attributeName) {
        final AttributeLookupHandler<JString> attributeBuilder = stringAttributes.get(attributeName);
        if (attributeBuilder != null) {
            return attributeBuilder.lookupAttribute(instance, runningScript);
        } else {
            return null;
        }
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
            return JInt.of(lo);
        }
        if (o instanceof Integer in) {
            return JInt.of(in);
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
}
