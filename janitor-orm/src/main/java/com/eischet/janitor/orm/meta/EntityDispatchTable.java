package com.eischet.janitor.orm.meta;

import com.eischet.janitor.api.errors.runtime.JanitorError;
import com.eischet.janitor.api.types.JAssignable;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.api.types.TemporaryAssignable;
import com.eischet.janitor.api.types.dispatch.DispatchTable;
import com.eischet.janitor.api.types.dispatch.ValueExpander;
import com.eischet.janitor.api.types.interop.NotNullGetter;
import com.eischet.janitor.api.types.interop.NotNullSetter;
import com.eischet.janitor.logging.JanitorLogger;
import com.eischet.janitor.orm.JanitorOrm;
import com.eischet.janitor.orm.dao.Dao;
import com.eischet.janitor.orm.dao.Uplink;
import com.eischet.janitor.orm.entity.OrmEntity;
import com.eischet.janitor.orm.ref.ForeignKey;
import com.eischet.janitor.orm.ref.ForeignKeyNull;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

import static com.eischet.janitor.api.util.ObjectUtilities.simpleClassNameOf;

/**
 * {@link OrmDispatchTable} for entities; the table is its own {@link EntityWrangler}.
 * <p>
 * The null reference stays a separate object so that it can be exposed as {@code Person.NULL}; the table just holds on to it.
 * </p>
 * <pre>{@code
 * public static final ForeignKeyNull<Frist> NULL = new ForeignKeyNull<>();
 * public static final EntityDispatchTable<Frist, DaoCollection> DISPATCH =
 *         new EntityDispatchTable<>(Frist.class, Frist::new, NULL, DaoCollection::getFristDao);
 * }</pre>
 *
 * @param <T> any type of ORM entity
 * @param <U> type of uplink
 */
public class EntityDispatchTable<T extends OrmEntity, U extends Uplink> extends OrmDispatchTable<T, U> implements EntityWrangler<T, U> {

    private static final JanitorLogger log = JanitorLogger.getLogger(EntityDispatchTable.class);

    protected final @NotNull ForeignKeyNull<T> nullReference;
    protected final @NotNull Function<U, Dao<T>> daoRetriever;

    public EntityDispatchTable(final @NotNull Class<T> wrangledClass,
                               final @NotNull Function<U, T> constructor,
                               final @NotNull ForeignKeyNull<T> nullReference,
                               final @NotNull Function<U, Dao<T>> daoRetriever) {
        super(wrangledClass, constructor);
        this.nullReference = nullReference;
        this.daoRetriever = daoRetriever;
    }

    protected EntityDispatchTable(final @NotNull EntityDispatchTable<T, U> parent, final boolean includeApplyMethod) {
        super(parent, includeApplyMethod);
        this.nullReference = parent.nullReference;
        this.daoRetriever = parent.daoRetriever;
    }

    @Override
    public EntityDispatchTable<T, U> extend(final boolean includeApplyMethod) {
        return new EntityDispatchTable<>(this, includeApplyMethod);
    }

    @Override
    public EntityDispatchTable<T, U> extend() {
        return extend(true);
    }

    /**
     * Sets the table-level ORM meta-data that the DAOs need.
     * The class name has already been set by the constructor.
     */
    public EntityDispatchTable<T, U> table(final @NotNull String tableName,
                                           final @NotNull String idField,
                                           final String keyField,
                                           final String nameField,
                                           final String sequenceName) {
        setMetaData(JanitorOrm.MetaData.TABLE_NAME, tableName);
        setMetaData(JanitorOrm.MetaData.ID_FIELD, idField);
        setMetaData(JanitorOrm.MetaData.KEY_FIELD, keyField);
        setMetaData(JanitorOrm.MetaData.NAME_FIELD, nameField);
        setMetaData(JanitorOrm.MetaData.ID_SEQUENCE, sequenceName);
        return this;
    }

    @Override
    public @NotNull ForeignKeyNull<T> getNullReference() {
        return nullReference;
    }

    @Override
    public @NotNull Dao<T> retrieveDao(final @NotNull U uplink) {
        return daoRetriever.apply(uplink);
    }

    /**
     * Adds a foreign key property to this table, pointing to the entity described by {@code target}.
     * Equivalent to {@code target.addReference(this, ...)}, but reads from the owning side.
     */
    public <X extends OrmEntity> void addReference(final @NotNull String propertyName,
                                                                 final @NotNull String columnName,
                                                                 final @NotNull EntityWrangler<X, ?> target,
                                                                 final @NotNull NotNullGetter<T, ForeignKey<X>> getter,
                                                                 final @NotNull NotNullSetter<T, ForeignKey<X>> setter,
                                                                 final @NotNull ValueExpander<T, ForeignKey<X>> expander) {
        target.addReference(this, propertyName, columnName, getter, setter, expander);
    }

    @Override
    public T duplicate(final U uplink, final T original) {
        if (original == null) {
            return null;
        }
        if (original instanceof Duplicating duplicating) {
            //noinspection unchecked
            return (T) duplicating.duplicate();
        }
        final T copy = createNewInstance(uplink);
        copyAttributes(this, original, copy);
        return copy;
    }

    /**
     * Copies all assignable scripting attributes of the dispatch table from one object to another.
     */
    static <T extends OrmEntity> void copyAttributes(final DispatchTable<T> dispatchTable, final T original, final T copy) {
        dispatchTable.streamAttributeNames().forEach(attr -> {
            try {
                final JanitorObject target = dispatchTable.get(attr).lookupAttribute(copy);
                if (target instanceof JAssignable assignableTarget) {
                    final JanitorObject source = dispatchTable.get(attr).lookupAttribute(original);
                    if (source instanceof TemporaryAssignable assignableSource) {
                        assignableTarget.assign(assignableSource.getValue()); // unpack the value
                    } else if (source != null) {
                        assignableTarget.assign(source);
                    } else {
                        log.warn("when copying {}, the property {} could not be copied!", simpleClassNameOf(original), attr);
                    }
                }
            } catch (Exception e) {
                throw new JanitorError("error copying attribute " + attr + " from " + original + " to a new copy", e);
            }
        });
    }

}
