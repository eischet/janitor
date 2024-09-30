package com.eischet.janitor.api.types.dispatch;

import com.eischet.janitor.api.JanitorEnvironment;
import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.api.types.TemporaryAssignable;
import com.eischet.janitor.api.types.builtin.*;
import com.eischet.janitor.api.types.functions.JBoundMethod;
import com.eischet.janitor.api.types.functions.JConstructor;
import com.eischet.janitor.api.types.functions.JUnboundMethod;
import com.eischet.janitor.api.types.functions.JVoidMethod;
import com.eischet.janitor.api.types.wrapped.JanitorWrapper;
import com.eischet.janitor.toolbox.json.api.*;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public abstract class GenericDispatchTable<T extends JanitorObject> implements Dispatcher<T> {

    protected final JsonSupportDelegate<String> JSON_STRING = new JsonSupportDelegate<>(JsonInputStream::nextString, JsonOutputStream::value);
    protected final JsonSupportDelegate<Integer> JSON_INT = new JsonSupportDelegate<>(JsonInputStream::nextInt, JsonOutputStream::value);
    protected final JsonSupportDelegate<Long> JSON_LONG = new JsonSupportDelegate<>(JsonInputStream::nextLong, JsonOutputStream::value);
    protected final JsonSupportDelegate<Double> JSON_DOUBLE = new JsonSupportDelegate<>(JsonInputStream::nextDouble, JsonOutputStream::value);

    // The difference between JSON_BOOL and JSON_BOOL_NULLABLE is subtle for JSON purposes: boolean true/false is only written when true and omitted otherwise,
    // Boolean null/true/false writes both true and false but omits null. I think that's what people using these types should expect.
    // The null check is done outside, so that's whey in JSON_BOOL_NULLABLE there's no such check.
    protected final JsonSupportDelegate<Boolean> JSON_BOOL = new JsonSupportDelegate<>(JsonInputStream::nextBoolean, JsonOutputStream::value, value -> !value);
    protected final JsonSupportDelegate<Boolean> JSON_BOOL_NULLABLE = new JsonSupportDelegate<>(JsonInputStream::nextBoolean, JsonOutputStream::value, anything -> false);


    private final DispatchDelegate<T> parentLookupHandler;
    private final Map<String, AttributeLookupHandler<T>> map = new HashMap<>();
    private final List<Attribute<T>> attributes = new ArrayList<>();
    private JConstructor<T> constructor;
    private final ParentAttributeReader<T> parentAttributeReader;
    private final ParentAttributeWriter<T> parentAttributeWriter;


    @FunctionalInterface
    private interface ParentAttributeReader<T> {
        boolean readAttribute(final JsonInputStream stream, final String key, final T instance) throws JsonException;
    }

    @FunctionalInterface
    private interface ParentAttributeWriter<T> {
        void writeToJson(final JsonOutputStream stream, T instance) throws JsonException;
    }

    public GenericDispatchTable() {
        parentLookupHandler = null;
        parentAttributeReader = null;
        parentAttributeWriter = null;
    }

    public <P extends JanitorObject> GenericDispatchTable(final Dispatcher<P> parent, final Function<T, P> caster) {
        parentLookupHandler = (instance, process, name) -> parent.dispatch(caster.apply(instance), process, name);
        // not tested yet: can we use this to delegate
        parentAttributeReader = (stream, key, instance) -> {
            if (parent instanceof GenericDispatchTable<P> parentDispatch) {
                return parentDispatch.readAttribute(stream, key, caster.apply(instance));
            } else {
                return false;
            }
        };
        parentAttributeWriter = (stream, instance) -> {
            System.out.println("parent is an instance of " + parent.getClass());
            if (parent instanceof GenericDispatchTable<P> parentDispatch) {
                parentDispatch.writeMyAttributes(stream, caster.apply(instance));
            }
        };
    }

    public <X> JsonAdapter<T> adapt(final @NotNull JsonSupport<X> delegate, final @NotNull Function<T, X> getter, final @Nullable BiConsumer<T, X> setter) {
        return new JsonAdapter<>() {
            @Override
            public void write(final JsonOutputStream stream, final T instance) throws JsonException {
                final X propertyValue = getter.apply(instance);
                delegate.write(stream, propertyValue);
            }

            @Override
            public void read(final JsonInputStream stream, final T instance) throws JsonException {
                if (setter != null) {
                    final @NotNull X jsonValue = delegate.read(stream);
                    setter.accept(instance, jsonValue);
                // } else { // TODO: warn!
                }
            }

            @Override
            public boolean isDefault(final T instance) {
                final X propertyValue = getter.apply(instance);
                if (propertyValue == null || propertyValue == JNull.NULL) {
                    return true;
                }
                return delegate.isDefault(propertyValue);
            }
        };
    }

    public <X> JsonAdapter<T> adaptList(final @NotNull JsonSupportDelegate<X> delegate, final @NotNull Function<T, List<X>> getter, final @Nullable BiConsumer<T, List<X>> setter) {
        return new JsonAdapter<T>() {
            @Override
            public void write(final JsonOutputStream stream, final T instance) throws JsonException {
                final List<X> listValue = getter.apply(instance);
                stream.beginArray();
                for (final X element : listValue) {
                    delegate.write(stream, element);
                }
                stream.endArray();
            }

            @Override
            public void read(final JsonInputStream stream, final T instance) throws JsonException {
                if (setter != null) {
                    final List<X> listValue = new ArrayList<>();
                    stream.beginArray();
                    while (stream.hasNext()) {
                        final @NotNull X element = delegate.read(stream);
                        listValue.add(element);
                    }
                    stream.endArray();
                    setter.accept(instance, listValue);
                }
            }

            @Override
            public boolean isDefault(final T instance) {
                final List<X> listValue = getter.apply(instance);
                return listValue == null || listValue.isEmpty();
            }
        };
    }

    private void put(final String name, final AttributeLookupHandler<T> handler, final @Nullable JsonAdapter<T> jsonSupport) {
        map.put(name, handler);
        attributes.removeIf(element -> Objects.equals(element.name, name));
        attributes.add(new Attribute<>(name, handler, jsonSupport));
    }

    public JConstructor<T> getConstructor() {
        return constructor;
    }

    public void setConstructor(JConstructor<T> constructor) {
        this.constructor = constructor;
    }

    /**
     * Adds a method to the dispatch table.
     *
     * @param name   the name of the method
     * @param method the method
     */
    public void addMethod(final String name, final JUnboundMethod<T> method) {
        put(name, (instance, process) -> new JBoundMethod<>(name, instance, method), null);
    }

    /**
     * Adds a method to the dispatch table that automatically returns the instance, useful for fluent interfaces.
     *
     * @param name   the name of the method
     * @param method the method
     */
    public void addBuilderMethod(final String name, final JVoidMethod<T> method) {
        put(name, (instance, process) -> new JBoundMethod<>(name, instance, (self, p1, arguments) -> {
            method.call(instance, p1, arguments);
            return instance;
        }), null);
    }

    /**
     * Add a method to the dispatch table that returns (Janitor) 'null'.
     * This is just a shorthand for addMethod where you could also return null manually.
     *
     * @param name   the method's name
     * @param method the method
     */
    public void addVoidMethod(final String name, final JVoidMethod<T> method) {
        put(name, (instance, process) -> new JBoundMethod<>(name, instance, (self, p1, arguments) -> {
            method.call(instance, p1, arguments);
            return JNull.NULL;
        }), null);
    }

    /**
     * Adds a read-only integer property.
     *
     * @param name   property name
     * @param getter property getter
     */
    public void addIntegerProperty(final String name, final Function<T, Integer> getter) {
        put(name, (instance, process) -> process.getBuiltins().integer(getter.apply(instance)), adapt(JSON_INT, getter, null));
    }

    /**
     * Adds a read-write integer property.
     *
     * @param name   property name
     * @param getter property getter
     * @param setter property setter
     */
    public void addIntegerProperty(final String name, final Function<T, Integer> getter, final BiConsumer<T, Integer> setter) {
        put(
                name,
                (instance, process) -> new TemporaryAssignable(
                        process.getEnvironment().getBuiltinTypes().integer(getter.apply(instance)),
                        value -> setter.accept(instance, JInt.requireInt(process, value).janitorGetHostValue().intValue())
                ),
                adapt(JSON_INT, getter, setter));
    }

    /**
     * Adds a read-only long property.
     *
     * @param name   property name
     * @param getter property getter
     */
    public void addLongProperty(final String name, final Function<T, Long> getter) {
        put(name, (instance, process) -> process.getEnvironment().getBuiltinTypes().integer(getter.apply(instance)), adapt(JSON_LONG, getter, null));
    }

    /**
     * Adds a read-write long property.
     *
     * @param name   property name
     * @param getter property getter
     * @param setter property setter
     */
    public void addLongProperty(final String name, final Function<T, Long> getter, final BiConsumer<T, Long> setter) {
        put(name, (instance, process) -> new TemporaryAssignable(process.getEnvironment().getBuiltinTypes().integer(getter.apply(instance)), value -> setter.accept(instance, JInt.requireInt(process, value).janitorGetHostValue())), adapt(JSON_LONG, getter, setter));
    }

    /**
     * Adds a read-only double property.
     *
     * @param name   property name
     * @param getter property getter
     */
    public void addDoubleProperty(final String name, final Function<T, Double> getter, final BiConsumer<T, Double> setter) {
        put(name, (instance, process) -> new TemporaryAssignable(process.getEnvironment().getBuiltinTypes().floatingPoint(getter.apply(instance)), value -> setter.accept(instance, process.requireFloat(value).janitorGetHostValue())), adapt(JSON_DOUBLE, getter, setter));
    }

    /**
     * Adds a read-only list property.
     *
     * @param name   property name
     * @param getter property getter
     */
    public void addListProperty(final String name, final Function<T, JList> getter) {
        put(name, (instance, process) -> getter.apply(instance), null);
    }

    public <E> void addListProperty(final String name, final Function<T, List<E>> getter, final BiConsumer<T, List<E>> setter, final TwoWayConverter<E> converter, final @Nullable JsonSupportDelegate<E> jsonSupportDelegate) {
        put(name, (instance, process) -> new TemporaryAssignable(ConverterToJanitor.toJanitorList(process, getter.apply(instance), converter), value -> {
                    if (!(value instanceof JList argList)) {
                        throw new IllegalArgumentException("Expected a list");
                    }
                    final List<E> list = ConverterFromJanitor.toList(process, argList, converter);
                    setter.accept(instance, list);
                }),
                jsonSupportDelegate == null ? null : adaptList(jsonSupportDelegate, getter, setter));
    }

    public void addListOfStringsProperty(final String name, final Function<T, List<String>> getter, final BiConsumer<T, List<String>> setter) {
        addListProperty(name, getter, setter, StringConverter.INSTANCE, JSON_STRING);
    }

    public void addListOfIntegersProperty(final String name, final Function<T, List<Integer>> getter, final BiConsumer<T, List<Integer>> setter) {
        addListProperty(name, getter, setter, IntegerConverter.INSTANCE, JSON_INT);
    }

    public void addListOfDoublesProperty(final String name, final Function<T, List<Double>> getter, final BiConsumer<T, List<Double>> setter) {
        addListProperty(name, getter, setter, FloatConverter.INSTANCE, JSON_DOUBLE);
    }

    /**
     * Adds a read-only boolean property.
     * This variant maps false to null! If you want to preserve null values for scripts, use addNullableBooleanProperty instead!
     *
     * @param name   property name
     * @param getter property getter
     */
    public void addBooleanProperty(final String name, final Function<T, Boolean> getter) {
        put(name, (instance, process) -> JBool.of(getter.apply(instance)), adapt(JSON_BOOL, getter, null));
    }

    /**
     * Adds a read-only boolean property.
     * This variant allows scripts to see true, false and null, while the simpler method maps false to null!
     *
     * @param name   property name
     * @param getter property getter
     */
    public void addNullableBooleanProperty(final String name, Function<T, Boolean> getter) {
        put(name, (instance, process) -> JBool.nullableBooleanOf(getter.apply(instance)), adapt(JSON_BOOL_NULLABLE, getter, null));
    }

    /**
     * Adds a read-write boolean property.
     *
     * @param name   property name
     * @param getter property getter
     * @param setter property setter
     */
    public void addBooleanProperty(final String name, final Function<T, Boolean> getter, final BiConsumer<T, Boolean> setter) {
        put(name, (instance, process) -> new TemporaryAssignable(JBool.of(getter.apply(instance)), value -> setter.accept(instance, JBool.require(process, value).janitorIsTrue())), adapt(JSON_BOOL, getter, setter));
    }

    /**
     * Adds a read-write boolean property.
     * This variant allows scripts to see and supply true, false and null, while the simpler method maps false to null!
     * Any other values are mapped to true/false according to their built-in "truthiness".
     *
     * @param name   property name
     * @param getter property getter
     * @param setter property setter
     */
    public void addNullableBooleanProperty(final String name, final Function<T, Boolean> getter, final BiConsumer<T, Boolean> setter) {
        put(name, (instance, process) -> new TemporaryAssignable(JBool.nullableBooleanOf(getter.apply(instance)), value -> setter.accept(instance, toNullableBoolean(value))), adapt(JSON_BOOL_NULLABLE, getter, setter));
    }

    /**
     * Special case for setting Boolean (not boolean!) fields.
     * Janitor's NULL is mapped to null, while everyhing else is mapped according to its regular "truthiness", which
     * implies that true maps to true and false to false.
     *
     * @param value the value to map
     * @return the mapped value, null, TRUE or FALSE
     */
    private @Nullable Boolean toNullableBoolean(final @NotNull JanitorObject value) {
        if (value == JNull.NULL) {
            return null;
        }
        return value.janitorIsTrue();
    }

    /**
     * Adds a read-only string property.
     *
     * @param name   property name
     * @param getter property getter
     */
    public void addStringProperty(final String name, final Function<T, String> getter) {
        put(name, (instance, process) -> process.getBuiltins().nullableString(getter.apply(instance)), adapt(JSON_STRING, getter, null));
    }

    /**
     * Adds a read-write string property.
     *
     * @param name   property name
     * @param getter property getter
     * @param setter property setter
     */
    public void addStringProperty(final String name, final Function<T, String> getter, final BiConsumer<T, String> setter) {
        put(name, (instance, process) -> new TemporaryAssignable(process.getBuiltins().nullableString(getter.apply(instance)), value -> setter.accept(instance, JString.require(process, value).janitorGetHostValue())), adapt(JSON_STRING, getter, setter));
    }

    /**
     * Adds a read-only date property.
     *
     * @param name   property name
     * @param getter property getter
     */
    public void addDateProperty(final String name, final Function<T, LocalDate> getter) {
        put(name, (instance, process) -> process.getBuiltins().nullableDate(getter.apply(instance)), null); // TODO: support dates
    }

    /**
     * Adds a read-write date property.
     *
     * @param name   property name
     * @param getter property getter
     * @param setter property setter
     */
    public void addDateProperty(final String name, final Function<T, LocalDate> getter, final BiConsumer<T, LocalDate> setter) {
        put(name, (instance, process) -> new TemporaryAssignable(process.getBuiltins().date(getter.apply(instance)), value -> setter.accept(instance, JDate.require(process, value).janitorGetHostValue())), null); // TODO: support dates
    }

    /**
     * Adds a read-only date-time property.
     *
     * @param name   property name
     * @param getter property getter
     */
    public void addDateTimeProperty(final String name, final Function<T, LocalDateTime> getter) {
        put(name, (instance, process) -> process.getBuiltins().nullableDateTime(getter.apply(instance)), null); // TODO: support datetime
    }

    /**
     * Adds a read-write date-time property.
     *
     * @param name   property name
     * @param getter property getter
     * @param setter property setter
     */
    public void addDateTimeProperty(final String name, final Function<T, LocalDateTime> getter, final BiConsumer<T, LocalDateTime> setter) {
        put(name, (instance, process) -> new TemporaryAssignable(process.getBuiltins().nullableDateTime(getter.apply(instance)), value -> setter.accept(instance, JDateTime.require(process, value).janitorGetHostValue())), null);
    }

    /**
     * Adds a read-only object property.
     *
     * @param name   property name
     * @param getter property getter
     */
    public <X extends JanitorObject> void addObjectProperty(final String name, final Function<T, X> getter) {
        put(name, (instance, process) -> getter.apply(instance), null); // no jsonSupport when there's no setter!
    }

    /**
     * Adds a read-write object property.
     *
     * @param name   property name
     * @param getter property getter
     * @param setter property setter
     */
    public <X extends JanitorObject> void addObjectProperty(final String name, final Function<T, X> getter, final BiConsumer<T, X> setter, final Supplier<X> constructor) {
        // because we'll turn a class cast exception into a script runtime error:
        // noinspection unchecked
        put(name, (instance, process) -> new TemporaryAssignable(getter.apply(instance), value -> setter.accept(instance, (X) value)), adapt(shim(constructor), getter, setter));
    }

    private <X extends JanitorObject> @NotNull JsonSupport<X> shim(final Supplier<X> constructor) {
        return new JsonSupport<>() {
            @Override
            public boolean isDefault(final @NotNull JanitorObject object) {
                return !object.janitorIsTrue();
            }

            @Override
            public @NotNull X read(final @NotNull JsonInputStream stream) throws JsonException {
                final X instance = constructor.get();
                if (instance instanceof JsonReader jsonReader) {
                    jsonReader.readJson(stream);
                } else {
                    // there might be more ways to read json we need to implement...
                    stream.skipValue();
                }
                return instance;
            }

            @Override
            public void write(final @NotNull JsonOutputStream stream, final @NotNull X object) throws JsonException {
                if (object instanceof JsonWriter writer) {
                    writer.writeJson(stream);
                } else {
                    throw new JsonException("Cannot serialize " + object + " [" + object.getClass().getSimpleName() + "]");
                }
            }
        };
    }


    /**
     * Dispatches a call to the instance, using the dispatch table to figure out what to do.
     *
     * @param process  the running script
     * @param name     the name of the attribute
     * @param required whether the attribute is required
     * @param instance the object to dispatch the call to
     * @return the result of the lookup
     */
    @SuppressWarnings("unchecked")
    public JanitorObject dispatch(final JanitorScriptProcess process, final String name, final boolean required, final JanitorWrapper<? extends T> instance) throws JanitorRuntimeException {
        final AttributeLookupHandler<T> handler = map.get(name);
        if (handler != null) {
            //noinspection unchecked
            return handler.lookupAttribute((T) instance, process);
        }
        return null;
    }

    /**
     * Dispatches a call to the instance, using the dispatch table to figure out what to do.
     *
     * @param instance the JanitorObject to dispatch the method call to
     * @param process  the running script
     * @param name     the name of the method to call
     * @return the result of the lookup
     */
    @Override
    public JanitorObject dispatch(final T instance, final JanitorScriptProcess process, final String name) throws JanitorRuntimeException {
        final AttributeLookupHandler<T> handler = map.get(name);
        if (handler != null) {
            return handler.lookupAttribute(instance, process);
        }
        if (parentLookupHandler != null) {
            return parentLookupHandler.delegate(instance, process, name);
        }
        return null;
    }

    public AttributeLookupHandler<T> get(final String key) {
        return map.get(key);
    }

    public boolean has(final String key) {
        return map.containsKey(key);
    }

    public void writeToJson(final JsonOutputStream stream, final T instance) throws JsonException {
        stream.beginObject();
        if (parentAttributeWriter != null) {
            parentAttributeWriter.writeToJson(stream, instance);
        }
        writeMyAttributes(stream, instance);
        stream.endObject();
    }

    private void writeMyAttributes(final JsonOutputStream stream, final T instance) throws JsonException {
        for (final Attribute<T> attribute : attributes) {
            @Nullable final JsonAdapter<T> attributeAdapter = attribute.jsonAdapter;
            if (attributeAdapter != null) {
                if (!attributeAdapter.isDefault(instance)) {
                    stream.key(attribute.name());
                    attributeAdapter.write(stream, instance);
                }
            }
        }

    }

    // TODO: the basic "inheritance" implemented by Dispatcher<...> etc. is not used in the JSON code. Figure out how, then do it.

    @Language("JSON")
    public String writeToJson(final JanitorEnvironment env, final T instance) throws JsonException {
        return env.writeJson(producer -> writeToJson(producer, instance));
    }

    public T readFromJson(final Supplier<T> constructor, final JsonInputStream stream) throws JsonException {
        final T instance = constructor.get();
        stream.beginObject();
        while (stream.hasNext()) {
            final String key = stream.nextKey();
            if (!readAttribute(stream, key, instance)) {
                if (parentAttributeReader != null) {
                    if (!parentAttributeReader.readAttribute(stream, key, instance)) {
                        stream.skipValue(); // TODO: and warn
                    }
                } else {
                    stream.skipValue(); // TODO: warn
                }
            }
        }
        stream.endObject();
        return instance;
    }


    private boolean readAttribute(final JsonInputStream stream, final String key, final T instance) throws JsonException {
        final Attribute<T> matched = attributes.stream().filter(attr -> Objects.equals(attr.name(), key)).findFirst().orElse(null);
        if (matched != null && matched.jsonAdapter != null) {
            matched.jsonAdapter.read(stream, instance);
            return true;
        }
        // can we delegate to the optional parent lookup table here, recursing up to the top?
        // the problem is, we need to cast to (P) and make sure the parent is GenericDispatch, too.
        // This whole structure is a bit messy right now. Writing to JSON has the same problem.
        return false;
    }

    public T readFromJson(final JanitorEnvironment env, final Supplier<T> constructor, @Language("JSON") final String json) throws JsonException {
        return readFromJson(constructor, env.getLenientJsonConsumer(json));
    }

    private record Attribute<T extends JanitorObject>(
            @NotNull String name,
            @NotNull AttributeLookupHandler<T> handler,
            @Nullable JsonAdapter<T> jsonAdapter) {
    }


}
