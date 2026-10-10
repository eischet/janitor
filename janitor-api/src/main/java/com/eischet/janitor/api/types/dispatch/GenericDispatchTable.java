package com.eischet.janitor.api.types.dispatch;

import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.glue.JanitorGlueException;
import com.eischet.janitor.api.errors.runtime.JanitorArgumentException;
import com.eischet.janitor.api.errors.runtime.JanitorNativeException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.metadata.MetaDataBuilder;
import com.eischet.janitor.api.metadata.MetaDataKey;
import com.eischet.janitor.api.metadata.MetaDataMap;
import com.eischet.janitor.api.metadata.MetaDataRetriever;
import com.eischet.janitor.api.metadata.PropertyHandle;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.api.types.TemporaryAssignable;
import com.eischet.janitor.api.types.builtin.*;
import com.eischet.janitor.api.types.functions.JBoundMethod;
import com.eischet.janitor.api.types.functions.JConstructor;
import com.eischet.janitor.api.types.functions.JUnboundMethod;
import com.eischet.janitor.api.types.functions.JVoidMethod;
import com.eischet.janitor.api.types.interop.*;
import com.eischet.janitor.api.types.wrapped.JanitorWrapper;
import com.eischet.janitor.logging.JanitorLogger;
import com.eischet.janitor.toolbox.json.api.*;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static com.eischet.janitor.api.Janitor.MetaData.HOST_NULLABLE;
import static com.eischet.janitor.api.Janitor.MetaData.TYPE_HINT;
import static com.eischet.janitor.api.util.ObjectUtilities.simpleClassNameOf;

/**
 * Base class for dispatch tables, which map attribute names to the code that gets or sets them, for a given host type.
 * Dispatch tables are the bridge between Janitor scripts and Java objects: they describe which properties and methods
 * a script can use, and how these are converted from and to JSON.
 * @param <T> the type of object that this table dispatches for
 */
public abstract class GenericDispatchTable<T extends JanitorObject> implements Dispatcher<T> {

    private static final JanitorLogger log = JanitorLogger.getLogger(GenericDispatchTable.class);

    protected final JsonSupportDelegate<String> JSON_STRING = new JsonSupportDelegate<>(JsonInputStream::nextString, JsonOutputStream::value, String::isEmpty);

    protected final JsonSupportDelegate<Integer> JSON_NULLABLE_INT = new JsonSupportDelegate<>(JsonInputStream::nextInt, JsonOutputStream::value);
    protected final JsonSupportDelegate<Long> JSON_NULLABLE_LONG = new JsonSupportDelegate<>(JsonInputStream::nextLong, JsonOutputStream::value);
    protected final JsonSupportDelegate<Double> JSON_NULLABLE_DOUBLE = new JsonSupportDelegate<>(JsonInputStream::nextDouble, JsonOutputStream::value);

    protected final JsonSupportDelegate<Integer> JSON_INT = new JsonSupportDelegate<>(JsonInputStream::nextInt, JsonOutputStream::value, intValue -> intValue == 0);
    protected final JsonSupportDelegate<Long> JSON_LONG = new JsonSupportDelegate<>(JsonInputStream::nextLong, JsonOutputStream::value, longValue -> longValue == 0L);
    protected final JsonSupportDelegate<BigDecimal> JSON_BIGD = new JsonSupportDelegate<>(
            stream -> {
                if (stream.peek() == JsonTokenType.NULL) {
                    stream.nextNull();
                    return BigDecimal.ZERO;
                } else if (stream.peek() == JsonTokenType.NUMBER) {
                    return new BigDecimal(stream.nextDouble());
                } else {
                    throw new JsonException("Expected a number, but got " + stream.peek() + " instead.");
                }
            },
            (stream, value) -> stream.value(value.doubleValue()),
            BigDecimal.ZERO::equals
    );
    protected final JsonSupportDelegate<Double> JSON_DOUBLE = new JsonSupportDelegate<>(JsonInputStream::nextDouble, JsonOutputStream::value, doubleValue -> doubleValue == 0.0d);

    // The difference between JSON_BOOL and JSON_BOOL_NULLABLE is subtle for JSON purposes: boolean true/false is only written when true and omitted otherwise,
    // Boolean null/true/false writes both true and false but omits null. I think that's what people using these types should expect.
    // The null check is done outside, so that's whey in JSON_BOOL_NULLABLE there's no such check.
    protected final JsonSupportDelegate<Boolean> JSON_BOOL = new JsonSupportDelegate<>(JsonInputStream::nextBoolean, JsonOutputStream::value, value -> !value);
    protected final JsonSupportDelegate<Boolean> JSON_BOOL_NULLABLE = new JsonSupportDelegate<>(JsonInputStream::nextBoolean, JsonOutputStream::value, anything -> false);

    private @Nullable
    final Dispatcher<?> parent;
    private final DispatchDelegate<T> parentLookupHandler;
    private final Map<String, AttributeLookupHandler<T>> map = new HashMap<>();
    private final List<String> attributeNames = new ArrayList<>();
    private final Set<String> readOnlyProperties = new java.util.HashSet<>();
    private final List<Attribute<T>> attributes = new ArrayList<>();
    private final ParentAttributeReader<T> parentAttributeReader;
    private final ParentAttributeWriter<T> parentAttributeWriter;
    private final Map<String, MetaDataMap> attributeMetaData = new HashMap<>();
    private final MetaDataMap classMetaData = new MetaDataMap();
    private JConstructor<T> constructor;
    private @Nullable Supplier<T> javaDefaultConstructor;
    private @Nullable Map<String, String> aliases;

    public GenericDispatchTable(final @Nullable Supplier<T> javaDefaultConstructor, boolean includeApplyMethod) {
        parent = null;
        parentLookupHandler = null;
        parentAttributeReader = null;
        parentAttributeWriter = null;
        this.javaDefaultConstructor = javaDefaultConstructor;
        if (includeApplyMethod) {
            addApplyMethod();
        }
    }

    /**
     * Makes an attribute available under a second name, e.g. to point {@code a.foo} to {@code a.bar} automatically.
     * This is useful for legacy entities whose attributes have been renamed.
     * @param alias the additional name
     * @param attributeName the name of the existing attribute that the alias refers to
     * @return this table
     */
    public GenericDispatchTable<T> addAlias(String alias, String attributeName) {
        if (aliases == null) {
            aliases = new HashMap<>();
        }
        aliases.put(alias, attributeName);
        return this;
    }

    /*

    From a discussion with an AI:

    public DispatchTable<T> extendFrom(Dispatcher<? super T> parent) {
        this.parent = parent;
        return this;
    }
@Override
public JanitorObject dispatch(T instance, JanitorScriptProcess process, String name) {
    JanitorObject local = ...; // local lookup
    if (local != null) return local;
    return parent != null ? parent.dispatch(instance, process, name) : null;
}

     */


    /**
     * Creates a table that falls back to a parent table for everything it does not define itself.
     * @param parent the parent dispatcher
     * @param caster converts instances of this table's type to instances of the parent's type
     * @param includeApplyMethod whether to add the "apply" method
     * @param <P> the parent type
     */
    public <P extends JanitorObject> GenericDispatchTable(final @NotNull Dispatcher<P> parent, final Function<T, P> caster, boolean includeApplyMethod) {
        this.parent = parent;
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
            if (parent instanceof GenericDispatchTable<P> parentDispatch) {
                parentDispatch.writeMyAttributes(stream, caster.apply(instance));
                return;
            }
            // otherwise?
            System.out.println("parent is an instance of " + parent.getClass() + ", but only GenericDispatchTable is supported at the moment. This needs to be fixed within GenericDispatchTable.java in the Janitor project, module janitor-api.");
        };
        if (includeApplyMethod) {
            addApplyMethod();
        }
    }

    private void addApplyMethod() {
        addMethod("apply", (self, process, args) -> {
            final JMap data = args.getRequired(0, JMap.class);
            data.applyTo(process, self);
            return self;
        });
    }

    /**
     * @return the attributes that this table defines itself, i.e. without those of its parent
     */
    public @NotNull @Unmodifiable List<Attribute<T>> getDirectAttributes() {
        return List.copyOf(attributes);
    }

    /**
     * Creates a JSON adapter for a property, based on a getter, a setter and a JSON delegate.
     * @param name the name of the property
     * @param delegate reads and writes values of the property's type
     * @param getter reads the property from an instance
     * @param setter writes the property to an instance
     * @param <X> the type of the property
     * @return the adapter
     */
    public <X> JsonAdapter<T> adapt(final String name,
                                    final @NotNull JsonSupport<X> delegate,
                                    final @NotNull NullableGetter<T, X> getter,
                                    final @NotNull NullableSetter<T, X> setter) {
        return new JsonAdapter<>() {
            @Override
            public void write(final JsonOutputStream stream, final T instance) throws JsonException {
                try {
                    final X propertyValue = getter.get(instance);
                    if (propertyValue != null) {
                        delegate.write(stream, propertyValue);
                    } else {
                        throw new JsonException("error writing field '" + name + "' to JSON because it must not be null here");

                    }
                } catch (Exception e) {
                    throw new JsonException("error writing field '" + name + "' to JSON", e);
                }
            }

            @Override
            public void read(final JsonInputStream stream, final T instance) throws JsonException {
                final @NotNull X jsonValue = delegate.read(stream);
                try {
                    setter.set(instance, jsonValue);
                } catch (final Exception e) {
                    throw new JsonException("error reading " + jsonValue + " at " + stream.getPath(), e);
                }
            }

            @Override
            public boolean isDefault(final T instance) {
                try {
                    final X propertyValue = getter.get(instance);
                    if (propertyValue == null || propertyValue == JNull.NULL) {
                        return true;
                    }
                    return delegate.isDefault(propertyValue);
                } catch (Exception e) {
                    return false; // which will lead to it being written, which will trigger the same exception in write()...
                }
            }
        };
    }

    /**
     * Creates a JSON adapter for a list property, based on a getter, a setter and a JSON delegate for the list elements.
     * @param delegate reads and writes the list elements
     * @param getter reads the list from an instance
     * @param setter writes the list to an instance, or null if the list is read-only
     * @param <X> the element type
     * @return the adapter
     */
    public <X> JsonAdapter<T> adaptList(final @NotNull JsonSupportDelegate<X> delegate,
                                        final @NotNull NullableGetter<T, List<X>> getter,
                                        final @Nullable NullableSetter<T, List<X>> setter) {
        return new JsonAdapter<>() {
            @Override
            public void write(final JsonOutputStream stream, final T instance) throws JsonException {
                try {
                    final List<X> listValue = getter.get(instance);
                    if (listValue != null) {
                        stream.beginArray();
                        for (final X element : listValue) {
                            delegate.write(stream, element);
                        }
                        stream.endArray();
                    } else {
                        stream.nullValue();
                    }
                } catch (Exception e) {
                    throw new JsonException("Error writing list property", e);
                }
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
                    try {
                        setter.set(instance, listValue);
                    } catch (final Exception e) {
                        throw new JsonException("error reading " + listValue + " at " + stream.getPath(), e);
                    }
                }
            }

            @Override
            public boolean isDefault(final T instance) {
                try {
                    final List<X> listValue = getter.get(instance);
                    return listValue == null || listValue.isEmpty();
                } catch (Exception e) {
                    return false;
                }
            }
        };
    }

    private <K> void storeMetaData(final @NotNull String attributeName, final @NotNull MetaDataKey<K> key, final @Nullable K value) {
        attributeMetaData.computeIfAbsent(attributeName, k -> new MetaDataMap()).put(key, value);
    }

    /**
     * Store meta-data on the top level of this object, e.g. about a scripted class.
     *
     * @param key   the user-defined key
     * @param value the value, which must match the key's definition
     * @param <K>   the type of value, which must match the key's definition
     */
    public <K> void setMetaData(final @NotNull MetaDataKey<K> key, final @Nullable K value) {
        classMetaData.put(key, value);
    }

    @Override
    public <K> @Nullable K getMetaData(final @NotNull MetaDataKey<K> key) {
        // TODO: meta-data should be retrieved from a parent class / dispatch table, too, if available
        final K value = classMetaData.get(key);
        if (value != null) {
            return value;
        }
        if (parent != null) {
            return parent.getMetaData(key);
        }
        return null;
    }

    @Override
    public <K> @Nullable K getMetaData(final @NotNull String attributeName, final @NotNull MetaDataKey<K> key) {
        // TODO: meta-data should be retrieved from a parent class / dispatch table, too, if available
        final MetaDataMap propertyMap = attributeMetaData.get(attributeName);
        if (propertyMap != null) {
            final K value = propertyMap.get(key);
            if (value != null) {
                return value;
            }
        }
        if (parent != null) {
            return parent.getMetaData(attributeName, key);
        }
        return null;
    }

    private void registerAttribute(final @NotNull String name, final @NotNull AttributeLookupHandler<T> handler, final @Nullable JsonAdapter<T> jsonSupport) {
        attributeNames.add(name);
        map.put(name, handler);
        attributes.removeIf(element -> Objects.equals(element.name, name));
        attributes.add(new Attribute<>(name, handler, jsonSupport));
    }

    private MetaDataBuilder<T> internalAddProperty(final @NotNull String name,
                                                   final @NotNull AttributeLookupHandler<T> handler,
                                                   final @Nullable JsonAdapter<T> jsonSupport,
                                                   final @Nullable JsonType jsonType,
                                                   final @Nullable Dispatcher<?> dispatcher) {
        registerAttribute(name, handler, jsonSupport);
        final InternalMetaDataBuilder<T> builder = new InternalMetaDataBuilder<>(name);
        builder.setMetaData(Janitor.MetaData.NAME, name);
        if (jsonType != null) {
            builder.setMetaData(Janitor.MetaData.JSON_TYPE, jsonType);
        }
        if (dispatcher != null) {
            builder.setMetaData(Janitor.MetaData.DISPATCHER, dispatcher);
        }
        return builder;
    }

    /**
     * Like the five-argument {@link #internalAddProperty}, but for an actual property (as opposed to a
     * method): also captures {@code getter}/{@code setter} so they can be handed back out through the
     * returned {@link PropertyHandle}, instead of only being closed over by the internal adapters and
     * then discarded.
     */
    private <V> PropertyHandle<T, V> internalAddProperty(final @NotNull String name,
                                                          final @NotNull AttributeLookupHandler<T> handler,
                                                          final @Nullable JsonAdapter<T> jsonSupport,
                                                          final @Nullable JsonType jsonType,
                                                          final @Nullable Dispatcher<?> dispatcher,
                                                          final @NotNull NullableGetter<T, V> getter,
                                                          final @Nullable NullableSetter<T, V> setter) {
        registerAttribute(name, handler, jsonSupport);
        if (setter == null) {
            readOnlyProperties.add(name);
        } else {
            readOnlyProperties.remove(name);
        }
        final InternalPropertyHandle<V> builder = new InternalPropertyHandle<>(name, getter, setter);
        builder.setMetaData(Janitor.MetaData.NAME, name);
        if (jsonType != null) {
            builder.setMetaData(Janitor.MetaData.JSON_TYPE, jsonType);
        }
        if (dispatcher != null) {
            builder.setMetaData(Janitor.MetaData.DISPATCHER, dispatcher);
        }
        return builder;
    }

    /**
     * Starts to override the meta-data of an existing attribute.
     * @param name the name of the attribute
     * @return a builder for the attribute's meta-data
     */
    public MetaDataBuilder<T> override(final @NotNull String name) {
        return new InternalMetaDataBuilder<>(name);
    }

    /**
     * @return the constructor that scripts can use to create instances of this type, or null if there is none
     */
    public JConstructor<T> getConstructor() {
        return constructor;
    }

    /**
     * Sets the constructor that scripts can use to create instances of this type.
     * @param constructor the constructor
     */
    public void setConstructor(JConstructor<T> constructor) {
        this.constructor = constructor;
    }

    /**
     * Adds a method to the dispatch table.
     *
     * @param name   the name of the method
     * @param method the method
     * @return a meta-data builder
     */
    public MetaDataBuilder<T> addMethod(final @NotNull String name, final @NotNull JUnboundMethod<T> method) {
        return internalAddProperty(name, instance -> new JBoundMethod<>(name, instance, method, new MetaDataRetriever() {
            @Override
            public <K> @Nullable K retrieveMetaData(final @NotNull MetaDataKey<K> key) {
                return GenericDispatchTable.this.getMetaData(name, key);
            }
        }), null, null, null)
                .setMetaData(TYPE_HINT, Janitor.MetaData.TypeHint.METHOD);
    }

    /**
     * Adds a method to the dispatch table that automatically returns the instance, useful for fluent interfaces.
     *
     * @param name   the name of the method
     * @param method the method
     * @return a meta-data builder
     */
    public MetaDataBuilder<T> addBuilderMethod(final @NotNull String name, final @NotNull JVoidMethod<T> method) {
        return internalAddProperty(name, instance -> new JBoundMethod<>(name, instance, (self, p1, arguments) -> {
            method.call(instance, p1, arguments);
            return instance;
        }, new MetaDataRetriever() {
            @Override
            public <K> @Nullable K retrieveMetaData(final @NotNull MetaDataKey<K> key) {
                return GenericDispatchTable.this.getMetaData(name, key);
            }
        }), null, null, null)
                .setMetaData(TYPE_HINT, Janitor.MetaData.TypeHint.METHOD);
    }

    /**
     * Add a method to the dispatch table that returns (Janitor) 'null'.
     * This is just a shorthand for addMethod where you could also return null manually.
     *
     * @param name   the method's name
     * @param method the method
     * @return a meta-data builder
     */
    public MetaDataBuilder<T> addVoidMethod(final @NotNull String name, final @NotNull JVoidMethod<T> method) {
        return internalAddProperty(name, instance -> new JBoundMethod<>(name, instance, (self, p1, arguments) -> {
            method.call(instance, p1, arguments);
            return JNull.NULL;
        }, new MetaDataRetriever() {
            @Override
            public <K> @Nullable K retrieveMetaData(final @NotNull MetaDataKey<K> key) {
                return GenericDispatchTable.this.getMetaData(name, key);
            }
        }), null, null, null)
                .setMetaData(TYPE_HINT, Janitor.MetaData.TypeHint.METHOD);
    }

    /**
     * Adds a read-only integer property.
     *
     * @param name   property name
     * @param getter property getter
     * @return a meta-data builder for further configuration
     */
    public PropertyHandle<T, Integer> addIntegerProperty(final @NotNull String name, final PrimitiveIntGetter<T> getter) {
        return internalAddProperty(name,
                instance -> Janitor.getBuiltins().integer(getter.get(instance)),
                adapt(name, JSON_INT, getter::get, NullableSetter.readOnly(name)),
                JsonType.NUMBER,
                null,
                NullableGetter.of(getter),
                null
        ).setMetaData(TYPE_HINT, Janitor.MetaData.TypeHint.INTEGER)
                .setMetaData(HOST_NULLABLE, false);
    }

    /**
     * Adds a read-only, nullable integer property.
     *
     * @param name   property name
     * @param getter property getter
     * @return a meta-data builder for further configuration
     */
    public PropertyHandle<T, Integer> addNullableIntegerProperty(final String name, final NullableGetter<T, Integer> getter) {
        return internalAddProperty(name,
                instance -> Janitor.nullableInteger(getter.get(instance)),
                adapt(name, JSON_INT, getter, NullableSetter.readOnly(name)),
                JsonType.NUMBER,
                null,
                getter,
                null)
                .setMetaData(TYPE_HINT, Janitor.MetaData.TypeHint.INTEGER)
                .setMetaData(Janitor.MetaData.HOST_NULLABLE, true)
                ;
    }


    /**
     * Adds a read-write integer property.
     *
     * @param name   property name
     * @param getter property getter
     * @param setter property setter
     * @return a metadata builder for further configuration
     */
    public PropertyHandle<T, Integer> addIntegerProperty(final @NotNull String name,
                                                 final @NotNull PrimitiveIntGetter<T> getter,
                                                 final @NotNull PrimitiveIntSetter<T> setter) {
        return internalAddProperty(
                name,
                instance -> TemporaryAssignable.of(
                        name,
                        Janitor.integer(getter.get(instance)),
                        value -> setter.set(instance, Janitor.requireInt(value).janitorGetHostValue().intValue())
                ),
                adapt(name, JSON_INT, getter::get, NullableSetter.guard(setter)),
                JsonType.NUMBER,
                null,
                NullableGetter.of(getter),
                NullableSetter.guard(setter)
        ).setMetaData(TYPE_HINT, Janitor.MetaData.TypeHint.INTEGER).setMetaData(HOST_NULLABLE, false);
    }

    /**
     * Adds a read-write, nullable integer property.
     *
     * @param name   property name
     * @param getter property getter
     * @param setter property setter
     * @return a metadata builder for further configuration
     */
    public PropertyHandle<T, Integer> addNullableIntegerProperty(final String name,
                                                         final NullableGetter<T, Integer> getter,
                                                         final NullableSetter<T, Integer> setter) {
        return internalAddProperty(
                name,
                instance -> TemporaryAssignable.of(
                        name,
                        Janitor.nullableInteger(getter.get(instance)),
                        value -> setter.set(instance, Conversions.toNullableJavaInteger(value))
                ),
                adapt(name, JSON_INT, getter, setter),
                JsonType.NUMBER,
                null,
                getter,
                setter)
                .setMetaData(TYPE_HINT, Janitor.MetaData.TypeHint.INTEGER)
                .setMetaData(Janitor.MetaData.HOST_NULLABLE, true)
                ;
    }


    /**
     * Adds a read-only long property.
     *
     * @param name   property name
     * @param getter property getter
     * @return a meta-data builder for further configuration
     */
    public PropertyHandle<T, Long> addLongProperty(final String name, final PrimitiveLongGetter<T> getter) {
        return internalAddProperty(name,
                instance -> Janitor.getBuiltins().integer(getter.get(instance)),
                adapt(name, JSON_LONG, getter::get, NullableSetter.readOnly(name)),
                JsonType.NUMBER, null,
                NullableGetter.of(getter),
                null)
                .setMetaData(TYPE_HINT, Janitor.MetaData.TypeHint.INTEGER);
    }

    /**
     * Adds a read-only nullable long property.
     *
     * @param name   property name
     * @param getter property getter
     * @return a meta-data builder for further configuration
     */
    public PropertyHandle<T, Long> addNullableLongProperty(final String name, final @NotNull NullableGetter<T, Long> getter) {
        return internalAddProperty(name, instance -> Janitor.nullableInteger(getter.get(instance)),
                adapt(name, JSON_LONG, getter, NullableSetter.readOnly(name)),
                JsonType.NUMBER, null,
                getter,
                null)
                .setMetaData(TYPE_HINT, Janitor.MetaData.TypeHint.INTEGER)
                .setMetaData(Janitor.MetaData.HOST_NULLABLE, true)
                ;
    }


    /**
     * Adds a read-write long property.
     *
     * @param name   property name
     * @param getter property getter
     * @param setter property setter
     * @return a meta-data builder for further configuration
     */
    public PropertyHandle<T, Long> addLongProperty(final @NotNull String name,
                                              final @NotNull PrimitiveLongGetter<T> getter,
                                              final @NotNull PrimitiveLongSetter<T> setter) {
        return internalAddProperty(name, instance -> TemporaryAssignable.of(
                        name,
                        Janitor.getBuiltins().integer(getter.get(instance)),
                        value -> setter.set(instance, Janitor.requireInt(value).janitorGetHostValue())
                ),
                adapt(name, JSON_LONG, getter::get, NullableSetter.guard(setter)),
                JsonType.NUMBER,
                null,
                NullableGetter.of(getter),
                NullableSetter.guard(setter)
        ).setMetaData(TYPE_HINT, Janitor.MetaData.TypeHint.INTEGER);
    }

    /**
     * Adds a BigDecimal property.
     */
    public PropertyHandle<T, BigDecimal> addBigDecimalProperty(final @NotNull String name,
                                                    final @NotNull NullableGetter<T, BigDecimal> getter,
                                                    final @NotNull NullableSetter<T, BigDecimal> setter) {
        return internalAddProperty(name, instance -> TemporaryAssignable.of(
                        name,
                        Janitor.nullableInteger(getter.get(instance)),
                        value -> {
                            if (value == Janitor.NULL) {
                                setter.set(instance, null);
                            } else if (value instanceof JNumber number) {
                                setter.set(instance, new BigDecimal(number.toDouble()));
                            } else {
                                throw new JanitorGlueException(JanitorArgumentException::fromGlue, "Expected a numeric value but got " + value.janitorClassName() + ".");
                            }
                        }),
                adapt(name, JSON_BIGD, getter, setter),
                JsonType.NUMBER, null,
                getter,
                setter
        ).setMetaData(TYPE_HINT, Janitor.MetaData.TypeHint.INTEGER);
    }


    /**
     * Adds a read-write, nullable long property.
     *
     * @param name   property name
     * @param getter property getter
     * @param setter property setter
     * @return a meta-data builder for further configuration
     */
    public PropertyHandle<T, Long> addNullableLongProperty(final @NotNull String name,
                                                      final @NotNull NullableGetter<T, Long> getter,
                                                      final @NotNull NullableSetter<T, Long> setter) {
        return internalAddProperty(name, instance -> TemporaryAssignable.of(name, Janitor.getBuiltins().nullableInteger(getter.get(instance)),
                        value -> setter.set(instance, Conversions.toNullableJavaLong(value))),
                adapt(name, JSON_LONG, getter, setter),
                JsonType.NUMBER, null,
                getter,
                setter
        )
                .setMetaData(TYPE_HINT, Janitor.MetaData.TypeHint.INTEGER)
                .setMetaData(Janitor.MetaData.HOST_NULLABLE, true)
                ;
    }


    /**
     * Adds a read-only double property.
     *
     * @param name   property name
     * @param getter property getter
     * @return a meta-data builder for further configuration
     */
    public PropertyHandle<T, Double> addDoubleProperty(final @NotNull String name,
                                                final PrimitiveDoubleGetter<T> getter) {
        return internalAddProperty(
                name,
                instance -> Janitor.getBuiltins().nullableFloatingPoint(getter.get(instance)),
                adapt(name, JSON_NULLABLE_DOUBLE, getter::get, NullableSetter.readOnly(name)
                ), JsonType.NUMBER, null,
                NullableGetter.of(getter),
                null
        ).setMetaData(TYPE_HINT, Janitor.MetaData.TypeHint.FLOAT);
    }


    /**
     * Adds a nullable double property
     *
     * @param name   property name
     * @param getter property getter
     * @param setter property setter
     * @return a meta-data builder for further configuration
     */
    public PropertyHandle<T, Double> addNullableDoubleProperty(final @NotNull String name,
                                                        final NullableGetter<T, Double> getter,
                                                        final NullableSetter<T, Double> setter) {
        return internalAddProperty(name, (instance) -> TemporaryAssignable.of(
                        name,
                        Janitor.nullableFloatingPoint(getter.get(instance)),
                        value -> setter.set(instance, Conversions.requireFloat(value).janitorGetHostValue())), adapt(name, JSON_DOUBLE, getter, setter),
                JsonType.NUMBER,
                null,
                getter,
                setter
        )
                .setMetaData(TYPE_HINT, Janitor.MetaData.TypeHint.FLOAT);
    }


    /**
     * Adds a double property
     *
     * @param name   property name
     * @param getter property getter
     * @param setter property setter
     * @return a meta-data builder for further configuration
     */
    public PropertyHandle<T, Double> addDoubleProperty(final @NotNull String name,
                                                final PrimitiveDoubleGetter<T> getter,
                                                final PrimitiveDoubleSetter<T> setter) {
        return internalAddProperty(name,
                (instance) -> TemporaryAssignable.of(name, Janitor.getBuiltins().floatingPoint(getter.get(instance)), value -> setter.set(instance, Conversions.requireFloat(value).janitorGetHostValue())),
                adapt(name, JSON_DOUBLE, getter::get, NullableSetter.guard(setter)),
                JsonType.NUMBER, null,
                NullableGetter.of(getter),
                NullableSetter.guard(setter)
        )
                .setMetaData(TYPE_HINT, Janitor.MetaData.TypeHint.FLOAT);
    }

    /**
     * Adds a list property.
     *
     * @param name   property name
     * @param getter property getter
     * @return a meta-data builder for further configuration
     */
    public PropertyHandle<T, JList> addListProperty(final @NotNull String name, final NullableGetter<T, @Nullable JList> getter) {
        return internalAddProperty(name, instance -> getter.get(instance), new JsonAdapter<>() {
                    @Override
                    public void write(final JsonOutputStream stream, final T instance) throws Exception {
                        final JList list = getter.get(instance);
                        if (list != null) {
                            list.writeJson(stream);
                        } else {
                            stream.beginArray().endArray();
                        }
                    }

                    @Override
                    public void read(final JsonInputStream stream, final T instance) throws Exception {
                        final JList list = getter.get(instance);
                        // Reading the list from JSON will only have an effect if the (possibly temporary) list returned by the getter has an onUpdate listener.
                        // We assume that, if such a listener exists, the list will properly be written back to the instance. If not, fail.
                        if (list == null || list.countOnUpdateReceivers() == 0) {
                            throw new JsonException("Unexpected JSON read operation for readonly list property " + name + " on instance " + instance);
                        } else {
                            list.readJson(stream);
                        }
                    }

                    @Override
                    public boolean isDefault(final T instance) throws Exception {
                        final JList list = getter.get(instance);
                        return list == null || list.isEmpty();
                    }
                },
                JsonType.ARRAY,
                null,
                getter,
                null
        ).setMetaData(TYPE_HINT, Janitor.MetaData.TypeHint.LIST);
    }

    /**
     * Add a list property.
     *
     * @param name                field name
     * @param getter              getter
     * @param setter              setter
     * @param converter           converter
     * @param jsonSupportDelegate json support
     * @param <E>                 type of list
     * @return meta data builder
     */
    public <E> PropertyHandle<T, List<E>> addListProperty(final String name,
                                                  final NullableGetter<T, List<E>> getter,
                                                  final NullableSetter<T, List<E>> setter,
                                                  final TwoWayConverter<E> converter,
                                                  final @Nullable JsonSupportDelegate<E> jsonSupportDelegate) {
        return internalAddProperty(name, instance -> TemporaryAssignable.of(name, Conversions.toJanitorList(getter.get(instance), converter), value -> {
                    if (!(value instanceof JList argList)) {
                        throw new IllegalArgumentException("Expected a list");
                    }
                    final List<E> list = Conversions.toList(argList, converter);
                    setter.set(instance, list);
                }),
                jsonSupportDelegate == null ? null : adaptList(jsonSupportDelegate, getter, setter),
                JsonType.ARRAY,
                null, // TODO: we could take the dispatcher as a parameter
                getter,
                setter
        ).setMetaData(TYPE_HINT, Janitor.MetaData.TypeHint.LIST);
    }

    /**
     * Adds a list property whose elements are Janitor objects themselves, with the JSON handling supplied by the caller.
     * Like {@link #addObjectPropertyWithJsonAdapter}, this is for lists whose elements can only be created knowing the
     * instance the list belongs to, e.g. a list of references that have to be looked up through whatever the instance
     * belongs to. Scripts assign a list; each element is converted by the {@code elementExpander}.
     *
     * @param name            property name
     * @param getter          property getter
     * @param setter          property setter
     * @param elementExpander converts each element a script assigns into the list's element type
     * @param jsonAdapter     reads and writes the property from/to JSON
     * @param <E>             type of the list's elements
     * @return a property handle
     */
    public <E extends JanitorObject> PropertyHandle<T, List<E>> addListPropertyWithJsonAdapter(final @NotNull String name,
                                                                                             final @NotNull NullableGetter<T, List<E>> getter,
                                                                                             final @NotNull NullableSetter<T, List<E>> setter,
                                                                                             final @NotNull ValueExpander<T, E> elementExpander,
                                                                                             final @NotNull JsonAdapter<T> jsonAdapter) {
        return internalAddProperty(name, instance -> TemporaryAssignable.of(name, Conversions.toJanitorList(getter.get(instance), element -> element), value -> {
                    if (!(value instanceof JList argList)) {
                        throw new IllegalArgumentException("Expected a list");
                    }
                    final List<E> list = new ArrayList<>(argList.size());
                    for (final JanitorObject element : argList) {
                        list.add(elementExpander.expandValue(instance, element));
                    }
                    setter.set(instance, list);
                }),
                jsonAdapter,
                JsonType.ARRAY,
                null,
                getter,
                setter
        ).setMetaData(TYPE_HINT, Janitor.MetaData.TypeHint.LIST);
    }

    /**
     * Adds a read-write property that is a list of strings.
     * @param name the name of the property
     * @param getter reads the list from an instance
     * @param setter writes the list to an instance
     * @return a handle for adding meta-data to the property
     */
    public PropertyHandle<T, List<String>> addListOfStringsProperty(final String name, final NullableGetter<T, List<String>> getter, final NullableSetter<T, List<String>> setter) {
        return addListProperty(name, getter, setter, StringConverter.INSTANCE, JSON_STRING).setMetaData(TYPE_HINT, Janitor.MetaData.TypeHint.LIST);
    }

    /**
     * Adds a read-write property that is a list of integers.
     * @param name the name of the property
     * @param getter reads the list from an instance
     * @param setter writes the list to an instance
     * @return a handle for adding meta-data to the property
     */
    public PropertyHandle<T, List<Integer>> addListOfIntegersProperty(final String name, final NullableGetter<T, List<Integer>> getter, final NullableSetter<T, List<Integer>> setter) {
        return addListProperty(name, getter, setter, IntegerConverter.INSTANCE, JSON_INT).setMetaData(TYPE_HINT, Janitor.MetaData.TypeHint.INTEGER);
    }

    /**
     * Adds a read-write property that is a list of longs.
     * @param name the name of the property
     * @param getter reads the list from an instance
     * @param setter writes the list to an instance
     * @return a handle for adding meta-data to the property
     */
    public PropertyHandle<T, List<Long>> addListOfLongsProperty(final String name, final NullableGetter<T, List<Long>> getter, final NullableSetter<T, List<Long>> setter) {
        return addListProperty(name, getter, setter, LongConverter.INSTANCE, JSON_LONG).setMetaData(TYPE_HINT, Janitor.MetaData.TypeHint.INTEGER);
    }

    /**
     * Adds a read-write property that is a list of doubles.
     * @param name the name of the property
     * @param getter reads the list from an instance
     * @param setter writes the list to an instance
     * @return a handle for adding meta-data to the property
     */
    public PropertyHandle<T, List<Double>> addListOfDoublesProperty(final String name, final NullableGetter<T, List<Double>> getter, final NullableSetter<T, List<Double>> setter) {
        return addListProperty(name, getter, setter, FloatConverter.INSTANCE, JSON_DOUBLE).setMetaData(TYPE_HINT, Janitor.MetaData.TypeHint.FLOAT);
    }

    /* TODO:
    public <N extends JanitorObject> void addListOfNativeObjectsProperty(final String name, final Function<T, List<N>> getter, final BiConsumer<T, List<N>> setter) {
        put(name,
                instance ->new TemporaryAssignable(ConverterToJanitor.toJanitorList(process, getter.apply(instance), new ConverterToJanitor<N>() {
                    @Override
                    public JanitorObject convertToJanitor(final JanitorScriptProcess process, final N value) throws JanitorRuntimeException {
                        return value;
                    }
                }), value -> {
                    if (!(value instanceof JList argList)) {
                        throw new IllegalArgumentException("Expected a list");
                    }
                    final List<N> list = ConverterFromJanitor.toList(process, argList, new ConverterFromJanitor<N>() {
                        @Override
                        public N convertFromJanitor(final JanitorScriptProcess process, final JanitorObject value) throws JanitorRuntimeException {
                            return (N) value;
                        }
                    });
                    setter.accept(instance, list);
                }), );
    }

     */

    /**
     * Adds a read-only boolean property.
     * This variant maps false to null! If you want to preserve null values for scripts, use addNullableBooleanProperty instead!
     *
     * @param name   property name
     * @param getter property getter
     * @return
     */
    public PropertyHandle<T, Boolean> addBooleanProperty(final @NotNull String name, final PrimitiveBooleanGetter<T> getter) {
        return internalAddProperty(
                name,
                instance -> Janitor.toBool(getter.get(instance)),
                adapt(name, JSON_BOOL, getter::get, NullableSetter.readOnly(name)),
                JsonType.BOOLEAN,
                null,
                NullableGetter.of(getter),
                null)
                .setMetaData(TYPE_HINT, Janitor.MetaData.TypeHint.BOOLEAN);
    }

    /**
     * Adds a read-only boolean property.
     * This variant allows scripts to see true, false and null, while the simpler method maps false to null!
     *
     * @param name   property name
     * @param getter property getter
     * @return
     */
    public PropertyHandle<T, Boolean> addNullableBooleanProperty(final @NotNull String name, NullableGetter<T, Boolean> getter) {
        return internalAddProperty(
                name,
                instance -> Janitor.nullableBooleanOf(getter.get(instance)),
                adapt(name, JSON_BOOL_NULLABLE, getter, NullableSetter.readOnly(name)),
                JsonType.BOOLEAN, null,
                getter,
                null
        )
                .setMetaData(TYPE_HINT, Janitor.MetaData.TypeHint.BOOLEAN)
                .setMetaData(Janitor.MetaData.HOST_NULLABLE, true)
                ;
    }

    /**
     * Adds a read-write boolean property.
     *
     * @param name   property name
     * @param getter property getter
     * @param setter property setter
     * @return
     */
    public PropertyHandle<T, Boolean> addBooleanProperty(final @NotNull String name,
                                                 final @NotNull PrimitiveBooleanGetter<T> getter,
                                                 final @NotNull PrimitiveBooleanSetter<T> setter) {
        return internalAddProperty(name,
                instance -> TemporaryAssignable.of(name, Janitor.toBool(getter.get(instance)),
                        value -> setter.set(instance, Janitor.requireBool(value).janitorIsTrue())),
                adapt(name, JSON_BOOL, getter::get, NullableSetter.guard(setter)),
                JsonType.BOOLEAN, null,
                NullableGetter.of(getter),
                NullableSetter.guard(setter)
        )
                .setMetaData(TYPE_HINT, Janitor.MetaData.TypeHint.BOOLEAN)
                .setMetaData(Janitor.MetaData.HOST_NULLABLE, true)
                ;
    }

    /**
     * Adds a read-write boolean property.
     * This variant allows scripts to see and supply true, false and null, while the simpler method maps false to null!
     * Any other values are mapped to true/false according to their built-in "truthiness".
     *
     * @param name   property name
     * @param getter property getter
     * @param setter property setter
     * @return
     */
    public PropertyHandle<T, Boolean> addNullableBooleanProperty(final @NotNull String name,
                                                         final @NotNull NullableGetter<T, Boolean> getter,
                                                         final @NotNull NullableSetter<T, Boolean> setter) {
        return internalAddProperty(name, instance -> TemporaryAssignable.of(name, Janitor.nullableBooleanOf(getter.get(instance)), value -> {
                    setter.set(instance, toNullableBoolean(value));
                }), adapt(name, JSON_BOOL_NULLABLE, getter, setter),
                JsonType.BOOLEAN, null,
                getter,
                setter
        )
                .setMetaData(TYPE_HINT, Janitor.MetaData.TypeHint.BOOLEAN)
                .setMetaData(Janitor.MetaData.HOST_NULLABLE, true);
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
     * @return
     */
    public PropertyHandle<T, String> addStringProperty(final @NotNull String name,
                                                final @NotNull NullableGetter<T, String> getter) {
        return internalAddProperty(name,
                instance -> Janitor.getBuiltins().nullableString(getter.get(instance)),
                adapt(name, JSON_STRING, getter, NullableSetter.readOnly(name)),

                JsonType.STRING, null,
                getter,
                null
        ).setMetaData(TYPE_HINT, Janitor.MetaData.TypeHint.STRING);
    }

    /**
     * Adds a read-write string property.
     *
     * @param name   property name
     * @param getter property getter
     * @param setter property setter
     * @return a meta-data builder
     */
    public PropertyHandle<T, String> addStringProperty(final @NotNull String name,
                                                final @NotNull NullableGetter<T, String> getter,
                                                final @NotNull NullableSetter<T, String> setter) {
        return internalAddProperty(name,
                instance -> TemporaryAssignable.of(name, Janitor.getBuiltins().nullableString(getter.get(instance)),
                        value -> setter.set(instance, stringOrNull(value))),
                adapt(name, JSON_STRING, getter, setter),
                JsonType.STRING, null,
                getter,
                setter
        ).setMetaData(TYPE_HINT, Janitor.MetaData.TypeHint.STRING).setMetaData(HOST_NULLABLE, true);
    }

    /**
     * Adds a read-only date property.
     *
     * @param name   property name
     * @param getter property getter
     * @return a meta-data builder
     */
    public PropertyHandle<T, LocalDate> addDateProperty(final @NotNull String name, final NullableGetter<@NotNull T, @Nullable LocalDate> getter) {
        return internalAddProperty(name, instance -> Janitor.getBuiltins().nullableDate(getter.get(instance)), new JsonAdapter<T>() {
                    @Override
                    public void write(final JsonOutputStream stream, final T instance) throws Exception {
                        @Nullable final LocalDate value = getter.get(instance);
                        if (value == null) {
                            stream.nullValue();
                        } else {
                            stream.value(JDate.DATE_FORMAT.format(value));
                        }
                    }

                    @Override
                    public void read(final JsonInputStream stream, final T instance) throws JsonException {
                        throw new JsonException("Unexpected JSON read operation for readonly date property " + name + " on instance " + instance);
                    }

                    @Override
                    public boolean isDefault(final T instance) throws Exception {
                        return getter.get(instance) == null;
                    }
                },
                JsonType.STRING, null,
                getter,
                null
        ).setMetaData(TYPE_HINT, Janitor.MetaData.TypeHint.DATE).setMetaData(HOST_NULLABLE, true);
    }

    /**
     * Adds a read-write date property.
     *
     * @param name   property name
     * @param getter property getter
     * @param setter property setter
     * @return a meta-data builder
     */
    public PropertyHandle<T, LocalDate> addDateProperty(final @NotNull String name, final @NotNull NullableGetter<T, LocalDate> getter, final @NotNull NullableSetter<T, LocalDate> setter) {
        return internalAddProperty(name, instance -> TemporaryAssignable.of(name, Janitor.getBuiltins().nullableDate(getter.get(instance)), value -> setter.set(instance, dateOrNull(value))), new JsonAdapter<>() {
            @Override
            public void write(final JsonOutputStream stream, final T instance) throws Exception {
                @Nullable final LocalDate value = getter.get(instance);
                if (value == null) {
                    stream.nullValue();
                } else {
                    stream.value(JDate.DATE_FORMAT.format(value));
                }
            }

            @Override
            public void read(final JsonInputStream stream, final T instance) throws Exception {
                if (stream.peek() == JsonTokenType.NULL) {
                    setter.set(instance, null);
                } else {
                    if (stream.peek() == JsonTokenType.NULL) {
                        setter.set(instance, null);
                    } else {
                        final String jsonString = stream.nextString();
                        final JDate scriptValue = (JDate) Janitor.nullableDateFromJsonString(jsonString);
                        setter.set(instance, scriptValue.janitorGetHostValue());
                    }
                }
            }

            @Override
            public boolean isDefault(final T instance) throws Exception {
                return getter.get(instance) == null;
            }
        }, JsonType.STRING, null, getter, setter).setMetaData(TYPE_HINT, Janitor.MetaData.TypeHint.DATE).setMetaData(HOST_NULLABLE, true);
    }


    private String stringOrNull(final JanitorObject value) throws JanitorGlueException {
        for (final var v : value.janitorUnpackAll()) {
            if (v == Janitor.NULL) {
                return null;
            } else if (v instanceof JString jstring) {
                return jstring.janitorGetHostValue();
            }
        }
        throw new JanitorGlueException(JanitorArgumentException::fromGlue, "expected String or null, but received " + value + " [" + simpleClassNameOf(value) + "]");
    }

    private LocalDateTime dateTimeOrNull(final JanitorObject v) throws JanitorGlueException {
        for (final var value : v.janitorUnpackAll()) {
            if (value == Janitor.NULL) {
                return null;
            } else if (value instanceof JDate date) {
                return date.janitorGetHostValue().atStartOfDay();
            } else if (value instanceof JDateTime dateTime) {
                return dateTime.janitorGetHostValue();
            } else if (value instanceof JString stringRep) {
                try {
                    @NotNull final JanitorObject possibleDateTime = Janitor.nullableDateTimeFromJsonString(stringRep.janitorGetHostValue());
                    if (possibleDateTime instanceof JDateTime dateTime) {
                        return dateTime.janitorGetHostValue();
                    }
                    log.warn("invalid datetime string: {}", stringRep);
                } catch (JsonException e) {
                    log.warn("invalid datetime string: {}", stringRep, e);
                }
            }
        }
        throw new JanitorGlueException(JanitorArgumentException::fromGlue, "expected DateTime (or Date, or null), but received " + v + " [" + simpleClassNameOf(v) + "]");
    }

    private LocalDate dateOrNull(final JanitorObject v) throws JanitorGlueException {
        for (final var value : v.janitorUnpackAll()) {
            if (value == Janitor.NULL) {
                return null;
            } else if (value instanceof JDate date) {
                return date.janitorGetHostValue();
            } else if (value instanceof JDateTime dateTime) {
                return dateTime.toDate().janitorGetHostValue();
            } else if (value instanceof JString stringRep) {
                try {
                    @NotNull final JanitorObject possibleDate = Janitor.nullableDateFromJsonString(stringRep.janitorGetHostValue());
                    if (possibleDate instanceof JDate date) {
                        return date.janitorGetHostValue();
                    }
                    log.warn("invalid date string: {}", stringRep);
                } catch (JsonException e) {
                    log.warn("invalid date string: {}", stringRep, e);
                }
            }
        }
        throw new JanitorGlueException(JanitorArgumentException::fromGlue, "expected Date (or DateTime, or null), but received " + v + " [" + simpleClassNameOf(v) + "]");
    }

    /**
     * Adds a read-only date-time property.
     *
     * @param name   property name
     * @param getter property getter
     * @return a meta-data builder
     */
    public PropertyHandle<T, LocalDateTime> addDateTimeProperty(final @NotNull String name, final @NotNull NullableGetter<T, LocalDateTime> getter) {
        return internalAddProperty(name, instance -> Janitor.getBuiltins().nullableDateTime(getter.get(instance)), new JsonAdapter<T>() {
            @Override
            public void write(final JsonOutputStream stream, final T instance) throws Exception {
                final LocalDateTime value = getter.get(instance);
                if (value == null) {
                    stream.nullValue();
                } else {
                    stream.value(JDateTime.JSON_FORMAT.format(value));
                }
            }

            @Override
            public void read(final JsonInputStream stream, final T instance) throws JsonException {
                throw new JsonException("Unexpected JSON read operation for readonly date-time property " + name + " on instance " + instance);
            }

            @Override
            public boolean isDefault(final T instance) throws Exception {
                return getter.get(instance) == null;
            }
        }, JsonType.STRING, null, getter, null)
                .setMetaData(TYPE_HINT, Janitor.MetaData.TypeHint.DATETIME).setMetaData(HOST_NULLABLE, true); // TODO: support datetime
    }

    /**
     * Adds a read-write date-time property.
     *
     * @param name   property name
     * @param getter property getter
     * @param setter property setter
     * @return a meta-data builder
     */
    public PropertyHandle<T, LocalDateTime> addDateTimeProperty(final String name, final @NotNull NullableGetter<T, LocalDateTime> getter, final @NotNull NullableSetter<T, LocalDateTime> setter) {
        return internalAddProperty(name, instance -> TemporaryAssignable.of(name, Janitor.getBuiltins().nullableDateTime(getter.get(instance)),
                value -> setter.set(instance, dateTimeOrNull(value))), new JsonAdapter<>() {
            @Override
            public void write(final JsonOutputStream stream, final T instance) throws Exception {
                final LocalDateTime value = getter.get(instance);
                if (value == null) {
                    stream.nullValue();
                } else {
                    stream.value(JDateTime.JSON_FORMAT.format(value));
                }
            }

            @Override
            public void read(final JsonInputStream stream, final T instance) throws Exception {
                if (stream.peek() == JsonTokenType.NULL) {
                    setter.set(instance, null);
                } else {
                    final String jsonString = stream.nextString();
                    final JDateTime scriptValue = (JDateTime) Janitor.nullableDateTimeFromJsonString(jsonString);
                    setter.set(instance, scriptValue.janitorGetHostValue());
                }
            }

            @Override
            public boolean isDefault(final T instance) throws Exception {
                return getter.get(instance) == null;
            }
        }, JsonType.STRING, null, getter, setter).setMetaData(TYPE_HINT, Janitor.MetaData.TypeHint.DATETIME).setMetaData(HOST_NULLABLE, true);
    }

    /**
     * Adds a read-only object property.
     *
     * @param name   property name
     * @param getter property getter
     * @return a meta-data builder
     */
    public <X extends JanitorObject> PropertyHandle<T, X> addObjectProperty(final String name, final NullableGetter<T, @Nullable X> getter) {
        return internalAddProperty(name, getter::get, adaptGetterOnly(name, getter), JsonType.OBJECT, null, getter, null).setMetaData(HOST_NULLABLE, true);
        // no jsonSupport when there's no setter!
    }


    /**
     * Adds a read-only object property.
     *
     * @param name       property name
     * @param getter     property getter
     * @param dispatcher dispatcher for the object type
     * @return a meta-data builder
     */
    public <X extends JanitorObject> PropertyHandle<T, X> addObjectProperty(final String name, final NullableGetter<T, @Nullable X> getter, final Dispatcher<X> dispatcher) {
        return internalAddProperty(name, getter::get, adaptGetterOnly(name, getter), JsonType.OBJECT, dispatcher, getter, null).setMetaData(HOST_NULLABLE, true);
        // no jsonSupport when there's no setter!
    }


    private <X extends JanitorObject> JsonAdapter<T> adaptGetterOnly(final String name, final NullableGetter<T, X> getter) {
        return new JsonAdapter<>() {
            @Override
            public void write(final JsonOutputStream stream, final T instance) throws Exception {
                final X value = getter.get(instance);
                if (value instanceof JsonWriter writer) {
                    writer.writeJson(stream);
                } else {
                    log.warn("the object property {} cannot be written to json because we do not handle type {}", name, simpleClassNameOf(value));
                }
            }

            @Override
            public void read(final JsonInputStream stream, final @NotNull T instance) throws JsonException {
                log.warn("the property {} cannot be read into {} because it has no setter", name, instance);
            }

            @Override
            public boolean isDefault(final T instance) throws Exception {
                final X value = getter.get(instance);
                return value == null || !value.janitorIsTrue();
            }
        };
    }

    /**
     * Adds a read-write object property.
     *
     * @param name   property name
     * @param getter property getter
     * @param setter property setter
     * @return a meta-data builder
     */
    public <X extends JanitorObject> PropertyHandle<T, X> addObjectProperty(final @NotNull String name,
                                                                          final @NotNull NullableGetter<T, X> getter,
                                                                          final @NotNull NullableSetter<T, X> setter,
                                                                          final @NotNull DefaultConstructor<X> constructor) {
        //noinspection unchecked
        return addObjectProperty(name, getter, setter, constructor, (self, v) -> (X) v);
    }

    /**
     * Adds a read-write object property whose JSON default is a shared singleton instance, e.g. a "null object".
     * @param name the name of the property
     * @param getter reads the object from an instance
     * @param setter writes the object to an instance
     * @param singletonDefault the default value
     * @param <X> the type of the property
     * @return a handle for adding meta-data to the property
     */
    public <X extends JanitorObject> PropertyHandle<T, X> addObjectPropertyWithSingletonDefault(final @NotNull String name,
                                                                                              final @NotNull NullableGetter<T, X> getter,
                                                                                              final @NotNull NullableSetter<T, X> setter,
                                                                                              final @NotNull X singletonDefault) {
        //noinspection unchecked
        return addObjectPropertyWithSingletonDefault(name, getter, setter, singletonDefault, (self, v) -> (X) v);
    }


    /**
     * Adds a constructor method.
     * This is meant for constructors of <strong>another</strong> objects, for use in factories and modules.
     *
     * @param name        name of the constructor, e.g. "String" for "Foo", conventionally starting with an upper case letter
     * @param constructor the constructor (taking no arguments)
     * @param <X>         any JanitorObject
     * @return a metadata builder for adding additional information
     */
    public <X extends JanitorObject> MetaDataBuilder<T> addConstructor(final @NotNull String name, final DefaultConstructor<X> constructor) {
        return addMethod(name, (self, process, args) -> {
            final X instance = constructor.create();
            if (args.size() == 1) {
                final JMap data = args.getRequired(0, JMap.class);
                data.applyTo(process, instance);
            }
            return instance;
        });
    }


    /**
     * Adds a read-write object property.
     *
     * @param name   property name
     * @param getter property getter
     * @param setter property setter
     * @return a meta-data builder
     */
    public <X extends JanitorObject> PropertyHandle<T, X> addObjectProperty(final @NotNull String name,
                                                                          final @NotNull NullableGetter<T, X> getter,
                                                                          final @NotNull NullableSetter<T, X> setter,
                                                                          final @NotNull DefaultConstructor<X> constructor,
                                                                          final @NotNull ValueExpander<T, X> expander) {
        // because we'll turn a class cast exception into a script runtime error:
        // noinspection unchecked
        return internalAddProperty(name,
                instance -> TemporaryAssignable.of(name, Janitor.nullableObject(getter.get(instance)), value -> setter.set(instance, expander.expandValue(instance, value))),
                adapt(name, shim(constructor), getter, setter), JsonType.OBJECT, null, getter, setter).setMetaData(HOST_NULLABLE, true);
    }

    /**
     * Adds a read-write object property whose JSON default is a shared singleton instance, e.g. a "null object".
     * @param name the name of the property
     * @param getter reads the object from an instance
     * @param setter writes the object to an instance
     * @param singletonDefault the default value
     * @param expander converts values assigned by scripts to the property type
     * @param <X> the type of the property
     * @return a handle for adding meta-data to the property
     */
    public <X extends JanitorObject> PropertyHandle<T, X> addObjectPropertyWithSingletonDefault(final @NotNull String name,
                                                                                              final @NotNull NullableGetter<T, X> getter,
                                                                                              final @NotNull NullableSetter<T, X> setter,
                                                                                              final @NotNull X singletonDefault,
                                                                                              final @NotNull ValueExpander<T, X> expander) {
        return addObjectPropertyWithJsonAdapter(name, getter, setter, expander, adapt(name, shim(() -> singletonDefault), getter, setter));
    }

    /**
     * Adds a read-write object property like {@link #addObjectPropertyWithSingletonDefault}, but with the JSON handling supplied by
     * the caller. Scripts assign through the {@code expander}; reading and writing JSON is up to {@code jsonAdapter}, which, unlike
     * the default one, is handed the instance the property belongs to when reading. That is what a reference needs that is stored as
     * just a short code and has to be looked up in whatever the owning instance belongs to.
     *
     * @param name        property name
     * @param getter      property getter
     * @param setter      property setter
     * @param expander    converts what scripts assign into the property's value
     * @param jsonAdapter reads and writes the property from/to JSON
     * @return a property handle
     */
    public <X extends JanitorObject> PropertyHandle<T, X> addObjectPropertyWithJsonAdapter(final @NotNull String name,
                                                                                         final @NotNull NullableGetter<T, X> getter,
                                                                                         final @NotNull NullableSetter<T, X> setter,
                                                                                         final @NotNull ValueExpander<T, X> expander,
                                                                                         final @NotNull JsonAdapter<T> jsonAdapter) {
        // because we'll turn a class cast exception into a script runtime error:
        // noinspection unchecked
        return internalAddProperty(name,
                instance -> TemporaryAssignable.of(name, Janitor.nullableObject(getter.get(instance)), value -> setter.set(instance, expander.expandValue(instance, value))),
                jsonAdapter, JsonType.OBJECT, null, getter, setter).setMetaData(HOST_NULLABLE, true);
    }


    private <X extends JanitorObject> @NotNull JsonSupport<X> shim(final DefaultConstructor<X> constructor) {
        return new JsonSupport<>() {
            @Override
            public boolean isDefault(final @NotNull JanitorObject object) {
                return !object.janitorIsTrue();
            }

            @Override
            public @NotNull X read(final @NotNull JsonInputStream stream) throws JsonException {
                final X instance = constructor.create();
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
     * @param name     the name of the attribute
     * @param instance the object to dispatch the call to
     * @return the result of the lookup
     */
    @SuppressWarnings("unchecked")
    public JanitorObject dispatch(final String name, final JanitorWrapper<? extends T> instance) throws Exception {
        final AttributeLookupHandler<T> handler = map.get(name);
        if (handler != null) {
            //noinspection unchecked
            return handler.lookupAttribute((T) instance);
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
        try {
            final AttributeLookupHandler<T> handler = map.get(name);
            if (handler != null) {
                // TODO: this is, I think, the right place to mix in available metadata for "name" into the resulting attribute!?
                return handler.lookupAttribute(instance);
            }
            if (aliases != null) {
                final String target = aliases.get(name);
                if (target != null && map.get(target) != null) {
                    return dispatch(instance, process, target);
                }
            }
            if (parentLookupHandler != null) {
                return parentLookupHandler.delegate(instance, process, name);
            }
            return null;
        } catch (JanitorRuntimeException e) {
            throw e;
        } catch (JanitorGlueException e) {
            throw e.toRuntimeException(process);
        } catch (Exception e) {
            throw new JanitorNativeException(process, "unhandled exception in native code", e);
        }
    }

    /**
     * Looks up the handler for an attribute.
     * @param key the name of the attribute
     * @return the handler, or null if there is no such attribute
     */
    public AttributeLookupHandler<T> get(final String key) {
        return map.get(key);
    }

    /**
     * Checks whether this table directly defines an attribute.
     * @param key the name of the attribute
     * @return true if the attribute exists
     */
    public boolean has(final String key) {
        return map.containsKey(key);
    }

    @Override
    public void writeToJson(final JsonOutputStream stream, final T instance) throws JsonException {
        stream.beginObject();
        try {
            if (parentAttributeWriter != null) {
                parentAttributeWriter.writeToJson(stream, instance);
            }
            writeMyAttributes(stream, instance);
        } catch (Exception e) {
            throw new JsonException("error writing " + instance + " [" + simpleClassNameOf(instance) + "]", e);
        }
        stream.endObject();
    }

    @Override
    public void writeSchemaToJson(final JsonOutputStream stream) throws JsonException {
        stream.beginObject();
        stream.key("type").value("object");
        stream.key("properties").beginObject();
        if (parent != null) {
            parent.writeSchemaToJson(stream);
        }
        for (final Attribute<T> attribute : attributes) {
            final Dispatcher<?> subDispatch = getMetaData(attribute.name(), Janitor.MetaData.DISPATCHER);
            final JsonType jsonType = getMetaData(attribute.name, Janitor.MetaData.JSON_TYPE);
            if (subDispatch != null) {
                stream.key(attribute.name());
                subDispatch.writeSchemaToJson(stream);
            } else if (jsonType != null) {
                stream.key(attribute.name());
                stream.beginObject();
                stream.key("type").value(jsonType.getJsonSchemaType());
                stream.endObject();
            }
        }
        stream.endObject();
        stream.endObject();
    }


    private void writeMyAttributes(final JsonOutputStream stream, final T instance) throws Exception {
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

    @Language("JSON")
    @Override
    public String writeToJson(final T instance) throws JsonException {
        return Janitor.current().writeJson(producer -> writeToJson(producer, instance));
    }

    @Override
    public T readFromJson(final Supplier<T> constructor, final JsonInputStream stream) throws JsonException {
        final T instance = constructor.get();
        if (instance instanceof JList list && list.getElementDispatchTable() != null) {
            stream.beginArray();
            while (stream.hasNext()) {
                list.getElementDispatchTable().readAsListElement(stream, list::add);
            }
            stream.endArray();
        } else if (instance instanceof JList list) {
            stream.beginArray();
            while (stream.hasNext()) {
                final JsonTokenType token = stream.peek();
                switch (token) {
                    case BEGIN_ARRAY -> {
                        final JList subList = Janitor.list();
                        subList.readJson(stream);
                        list.add(subList);
                    }
                    case BEGIN_OBJECT -> {
                        final JMap object = Janitor.map();
                        object.readJson(stream);
                        list.add(object);
                    }
                    case STRING -> list.add(Janitor.string(stream.nextString()));
                    case NUMBER -> list.add(Janitor.numeric(stream.nextDouble()));
                    case BOOLEAN -> list.add(stream.nextBoolean() ? Janitor.TRUE : Janitor.FALSE);
                    case NULL -> list.add(Janitor.NULL);
                    case END_ARRAY, END_OBJECT, NAME, END_DOCUMENT ->
                            throw new JsonException("Unexpected token while reading list: " + token);
                }
            }
            stream.endArray();
        } else {
            stream.beginObject();
            while (stream.hasNext()) {
                final String key = stream.nextKey();
                try {
                    if (!readAttribute(stream, key, instance)) {
                        if (parentAttributeReader != null) {
                            if (!parentAttributeReader.readAttribute(stream, key, instance)) {
                                stream.skipValue(); // TODO: and warn
                            }
                        } else {
                            stream.skipValue(); // TODO: warn
                        }
                    }
                } catch (Exception e) {
                    throw new JsonException("Cannot read attribute " + key, e);
                }
            }
            stream.endObject();
        }
        return instance;
    }

    private boolean readAttribute(final JsonInputStream stream, final String key, final T instance) throws Exception {
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


    @Override
    public T readFromJson(final Supplier<T> constructor, @Language("JSON") final String json) throws JsonException {
        return readFromJson(constructor, Janitor.current().getLenientJsonConsumer(json));
    }

    /**
     * Whether the attribute is a property registered without a setter. Such a value is derived (or loaded lazily) and cannot be assigned, so
     * code that copies assignable values can skip it without evaluating its getter.
     */
    public boolean isReadOnlyProperty(final @NotNull String name) {
        return readOnlyProperties.contains(name);
    }

    @Override
    public Stream<String> streamAttributeNames() {
        if (parent != null) {
            return Stream.concat(parent.streamAttributeNames(), attributeNames.stream());
        } else {
            return attributeNames.stream();
        }
    }

    /**
     * Reads one object of this table's type from JSON, and passes it to the consumer.
     * @param stream the JSON stream, positioned at the start of the object
     * @param elementConsumer receives the new object
     * @throws JsonException if the JSON is malformed, or if this table has no Java constructor
     */
    public void readAsListElement(final JsonInputStream stream, Consumer<JanitorObject> elementConsumer) throws JsonException {
        if (javaDefaultConstructor == null) {
            throw new JsonException("Error: you need to supply a Java constructor to read objects of this type from JSON as a list!");
        }
        elementConsumer.accept(readFromJson(javaDefaultConstructor, stream));
    }

    @Override
    public @Nullable Supplier<T> getJavaDefaultConstructor() {
        return javaDefaultConstructor;
    }


    @FunctionalInterface
    private interface ParentAttributeReader<T> {
        /**
         * Reads one attribute of an object from JSON.
         * @param stream the source
         * @param key the name of the attribute
         * @param instance the object
         * @return true if the attribute was read, false if it is unknown
         * @throws Exception on errors
         */
        boolean readAttribute(final JsonInputStream stream, final String key, final T instance) throws Exception;
    }

    @FunctionalInterface
    private interface ParentAttributeWriter<T> {
        /**
         * Writes the attributes of an object as JSON.
         * @param stream the target
         * @param instance the object
         * @throws Exception on errors
         */
        void writeToJson(final JsonOutputStream stream, T instance) throws Exception;
    }

    /**
     * A named attribute of a dispatch table.
     * @param name the name of the attribute
     * @param handler gets or sets the attribute
     * @param jsonAdapter reads and writes the attribute from and to JSON, or null if it is not part of the JSON representation
     */
    public record Attribute<T extends JanitorObject>(
            @NotNull String name,
            @NotNull AttributeLookupHandler<T> handler,
            @Nullable JsonAdapter<T> jsonAdapter) {
    }

    private class InternalMetaDataBuilder<U> implements MetaDataBuilder<U> {
        private final String name;

        public InternalMetaDataBuilder(final String name) {
            this.name = name;
        }

        @Override
        public <K> MetaDataBuilder<U> setMetaData(final MetaDataKey<K> key, final K value) {
            storeMetaData(name, key, value);
            return this;
        }
    }

    private class InternalPropertyHandle<V> implements PropertyHandle<T, V> {
        private final String name;
        private final @NotNull NullableGetter<T, V> getter;
        private final @Nullable NullableSetter<T, V> setter;

        InternalPropertyHandle(final String name, final @NotNull NullableGetter<T, V> getter, final @Nullable NullableSetter<T, V> setter) {
            this.name = name;
            this.getter = getter;
            this.setter = setter;
        }

        @Override
        public @NotNull String getName() {
            return name;
        }

        @Override
        public <K> @Nullable K getMetaData(final @NotNull MetaDataKey<K> key) {
            return GenericDispatchTable.this.getMetaData(name, key);
        }

        @Override
        public @NotNull NullableGetter<T, V> getGetter() {
            return getter;
        }

        @Override
        public @Nullable NullableSetter<T, V> getSetter() {
            return setter;
        }

        @Override
        public <K> PropertyHandle<T, V> setMetaData(final @NotNull MetaDataKey<K> key, final @Nullable K value) {
            storeMetaData(name, key, value);
            return this;
        }
    }


}
