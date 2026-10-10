package com.eischet.janitor.orm.meta;

import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.api.types.builtin.JNumber;
import com.eischet.janitor.api.types.builtin.JString;
import com.eischet.janitor.api.types.dispatch.DispatchTable;
import com.eischet.janitor.api.types.dispatch.JsonAdapter;
import com.eischet.janitor.api.types.dispatch.ValueExpander;
import com.eischet.janitor.api.types.interop.NotNullGetter;
import com.eischet.janitor.api.types.interop.NotNullSetter;
import com.eischet.janitor.logging.JanitorLogger;
import com.eischet.janitor.orm.JanitorOrm;
import com.eischet.janitor.orm.dao.Dao;
import com.eischet.janitor.orm.dao.Uplink;
import com.eischet.janitor.orm.entity.OrmEntity;
import com.eischet.janitor.orm.entity.OrmObject;
import com.eischet.janitor.orm.ref.ForeignKey;
import com.eischet.janitor.orm.ref.ForeignKeyIdentity;
import com.eischet.janitor.orm.ref.ForeignKeyInteger;
import com.eischet.janitor.orm.ref.ForeignKeyNull;
import com.eischet.janitor.orm.ref.ForeignKeyString;
import com.eischet.janitor.orm.ref.ForeignKeys;
import com.eischet.janitor.orm.sql.ColumnTypeHint;
import com.eischet.janitor.toolbox.json.api.JsonException;
import com.eischet.janitor.toolbox.json.api.JsonInputStream;
import com.eischet.janitor.toolbox.json.api.JsonOutputStream;
import com.eischet.janitor.toolbox.json.api.JsonTokenType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Helper interface that makes interacting with {@link OrmEntity} easier.
 * There's a certain amount of code bloat associated with having to pass around class, name, ref etc. all the time,
 * so we're centralizing them in a single place.
 *
 * @param <T> any type of ORM entity
 * @param <U> type of uplink, that is an object that contains your DAOs
 */
public interface EntityWrangler<T extends OrmEntity, U extends Uplink> extends Wrangler<T, U> {

    JanitorLogger log = JanitorLogger.getLogger(EntityWrangler.class);

    @NotNull ForeignKeyNull<T> getNullReference();

    @NotNull Dao<T> retrieveDao(final @NotNull U uplink);

    /**
     * Builds a {@link ValueExpander} that converts scripting values into a {@link ForeignKey} pointing at this
     * wrangler's entity type, resolving the DAO from the uplink obtained via {@code uplinkOf}.
     *
     * @param uplinkOf retrieves this wrangler's uplink type from the instance the property is being set on
     * @param <S>      the type of the instance owning the foreign-key property
     * @return a value expander suitable for {@link #addReference}
     */
    default <S extends JanitorObject> @NotNull ValueExpander<S, ForeignKey<T>> getValueExpander(final @NotNull Function<S, U> uplinkOf) {
        return (instance, value) -> {
            if (value instanceof ForeignKey<?> fk) {
                if (fk.getReferencedEntityClass() != getWrangledClass()) {
                    log.warn("expandValue: {} is a foreign key to {}, but we are looking for {}", value, fk.getReferencedEntityClass(), getWrangledClass());
                }
                //noinspection unchecked
                return (ForeignKey<T>) fk;
            }
            if (getWrangledClass().isInstance(value) && value instanceof ForeignKeyIdentity<?>) {
                //noinspection unchecked
                return (ForeignKeyIdentity<T>) value; // this works because all entities implement ForeignKeyIdentity
            }
            if (value == Janitor.NULL) {
                return getNullReference();
            }
            if (value instanceof JNumber idPointer) {
                return new ForeignKeyInteger<>(idPointer.toLong(), retrieveDao(uplinkOf.apply(instance)));
            }
            if (value instanceof JString keyPointer) {
                return new ForeignKeyString<>(keyPointer.janitorGetHostValue(), retrieveDao(uplinkOf.apply(instance)));
            }
            throw new IllegalArgumentException("Cannot convert " + value + " to a foreign key");
        };
    }

    /**
     * Adds a reference to this wrangler's entity type that is backed by a database column (an id) and is
     * read and written as a short code in JSON, see {@link #addKeyReference}.
     */
    default <V extends OrmObject> OrmPropertyHandle<V, ForeignKey<T>> addReference(final DispatchTable<V> dispatch,
                                                                 final String propertyName,
                                                                 final String columnName,
                                                                 final NotNullGetter<V, ForeignKey<T>> getter,
                                                                 final NotNullSetter<V, ForeignKey<T>> setter,
                                                                 final Function<V, U> uplinkOf) {
        return addKeyReference(dispatch, propertyName, getter, setter, uplinkOf)
                .setMetaData(JanitorOrm.MetaData.COLUMN_NAME, columnName)
                .setMetaData(JanitorOrm.MetaData.COLUMN_TYPE, ColumnTypeHint.INT);
    }

    /**
     * Adds a reference to this wrangler's entity type to any scriptable object, not necessarily an entity: for example a
     * JSON configuration class that lives inside a column of an entity. There is no column; the reference is stored as the
     * short code of its target, in JSON as well as in whatever holds the JSON.
     * <p>
     * Scripts can assign short codes, ids, entities or {@code null}; reading JSON looks the short code up through the DAO
     * that {@code uplinkOf} leads to, so a reference always resolves where its owner belongs. JSON is written as the short
     * code (see {@link #keyJsonAdapter}).
     * </p>
     *
     * @param uplinkOf retrieves the uplink (the object that holds the DAOs) from the instance owning the reference
     */
    default <V extends JanitorObject> OrmPropertyHandle<V, ForeignKey<T>> addKeyReference(final DispatchTable<V> dispatch,
                                                                    final String propertyName,
                                                                    final NotNullGetter<V, ForeignKey<T>> getter,
                                                                    final NotNullSetter<V, ForeignKey<T>> setter,
                                                                    final Function<V, U> uplinkOf) {
        final ValueExpander<V, ForeignKey<T>> expander = getValueExpander(uplinkOf);
        return OrmPropertyHandle.of(dispatch.addObjectPropertyWithJsonAdapter(
                        propertyName,
                        getter::get,
                        (v, value) -> setter.set(v, value == null ? getNullReference() : value),
                        expander,
                        keyJsonAdapter(propertyName, getter, setter, expander)))
                .setMetaData(Janitor.MetaData.REF, getSimpleClassName());
    }

    /**
     * JSON handling for a reference: always the short code of the target, or {@code null} for no reference. Reading goes
     * through the {@code expander}, i.e. it is the same as a script assigning that short code.
     * <p>
     * Writing a reference to something without a short code is an error. A reference to an id that points to nothing (any
     * more) is written as {@code null}.
     * </p>
     */
    default <V extends JanitorObject> @NotNull JsonAdapter<V> keyJsonAdapter(final @NotNull String propertyName,
                                                                            final @NotNull NotNullGetter<V, ForeignKey<T>> getter,
                                                                            final @NotNull NotNullSetter<V, ForeignKey<T>> setter,
                                                                            final @NotNull ValueExpander<V, ForeignKey<T>> expander) {
        return new JsonAdapter<>() {
            @Override
            public void write(final JsonOutputStream stream, final V instance) throws Exception {
                final String key = ForeignKeys.keyOf(getter.get(instance));
                if (key == null) {
                    stream.nullValue();
                } else {
                    stream.value(key);
                }
            }

            @Override
            public void read(final JsonInputStream stream, final V instance) throws Exception {
                final JanitorObject value;
                final JsonTokenType token = stream.peek();
                if (token == JsonTokenType.NULL) {
                    stream.nextNull();
                    value = Janitor.NULL;
                } else if (token == JsonTokenType.STRING) {
                    value = Janitor.getBuiltins().nullableString(stream.nextString());
                } else {
                    throw new JsonException("the reference '" + propertyName + "' must be a short code or null, at " + stream.getPath());
                }
                setter.set(instance, expander.expandValue(instance, value));
            }

            @Override
            public boolean isDefault(final V instance) throws Exception {
                return getter.get(instance).isNull();
            }
        };
    }

    /**
     * Adds a list of references to this wrangler's entity type to any scriptable object, the list counterpart of
     * {@link #addKeyReference}: stored as an array of short codes, with the same rules for scripts and for resolving.
     * The list is never {@code null}; no references and an empty list are the same, and an empty list is not written.
     *
     * @param uplinkOf retrieves the uplink (the object that holds the DAOs) from the instance owning the list
     */
    default <V extends JanitorObject> OrmPropertyHandle<V, List<ForeignKey<T>>> addKeyReferenceList(final DispatchTable<V> dispatch,
                                                                              final String propertyName,
                                                                              final NotNullGetter<V, List<ForeignKey<T>>> getter,
                                                                              final NotNullSetter<V, List<ForeignKey<T>>> setter,
                                                                              final Function<V, U> uplinkOf) {
        final ValueExpander<V, ForeignKey<T>> expander = getValueExpander(uplinkOf);
        return OrmPropertyHandle.of(dispatch.addListPropertyWithJsonAdapter(
                        propertyName,
                        getter::get,
                        (v, value) -> setter.set(v, value == null ? new ArrayList<>() : value),
                        expander,
                        keyListJsonAdapter(propertyName, getter, setter, expander)))
                .setMetaData(Janitor.MetaData.REF, getSimpleClassName());
    }

    /**
     * JSON handling for a list of references: an array of the targets' short codes. Reading goes through the {@code expander}
     * for each code; {@code null} and blank entries are dropped, and {@code null} for the whole array means an empty list.
     * <p>
     * As with {@link #keyJsonAdapter}, writing a reference to something without a short code is an error; a reference to an
     * id that points to nothing (any more) is left out.
     * </p>
     */
    default <V extends JanitorObject> @NotNull JsonAdapter<V> keyListJsonAdapter(final @NotNull String propertyName,
                                                                                final @NotNull NotNullGetter<V, List<ForeignKey<T>>> getter,
                                                                                final @NotNull NotNullSetter<V, List<ForeignKey<T>>> setter,
                                                                                final @NotNull ValueExpander<V, ForeignKey<T>> expander) {
        return new JsonAdapter<>() {
            @Override
            public void write(final JsonOutputStream stream, final V instance) throws Exception {
                stream.beginArray();
                for (final ForeignKey<T> reference : getter.get(instance)) {
                    final String key = ForeignKeys.keyOf(reference);
                    if (key != null) {
                        stream.value(key);
                    }
                }
                stream.endArray();
            }

            @Override
            public void read(final JsonInputStream stream, final V instance) throws Exception {
                final List<ForeignKey<T>> references = new ArrayList<>();
                if (stream.peek() == JsonTokenType.NULL) {
                    stream.nextNull();
                } else {
                    stream.beginArray();
                    while (stream.hasNext()) {
                        final JsonTokenType token = stream.peek();
                        if (token == JsonTokenType.NULL) {
                            stream.nextNull();
                        } else if (token == JsonTokenType.STRING) {
                            final ForeignKey<T> reference = expander.expandValue(instance, Janitor.getBuiltins().nullableString(stream.nextString()));
                            if (!reference.isNull()) {
                                references.add(reference);
                            }
                        } else {
                            throw new JsonException("the references in '" + propertyName + "' must be short codes, at " + stream.getPath());
                        }
                    }
                    stream.endArray();
                }
                setter.set(instance, references);
            }

            @Override
            public boolean isDefault(final V instance) throws Exception {
                return getter.get(instance).isEmpty();
            }
        };
    }

    /**
     * Copies an object by copying all assignable scripting attributes.
     * In most cases, this should be enough to create a proper clone.
     * If you want to have full control over the copying, implement EntityWrangler.Duplicating in your class.
     * Make sure that it can be cast to T in this context, or you'll get a class cast exception here.
     *
     * @param uplink   uplink object
     * @param original original object
     * @return a copy
     */
    T duplicate(U uplink, T original);

    /**
     * Interface for objects that will duplicate themselves, instead of relying on the simple approach of the duplicate method.
     */
    interface Duplicating {
        /**
         * @return a copy of this entity
         */
        OrmEntity duplicate();
    }

}
