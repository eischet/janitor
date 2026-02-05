package com.eischet.janitor.orm.meta;

import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.types.dispatch.DispatchTable;
import com.eischet.janitor.api.types.dispatch.ValueExpander;
import com.eischet.janitor.api.types.interop.NotNullGetter;
import com.eischet.janitor.api.types.interop.NotNullSetter;
import com.eischet.janitor.orm.JanitorOrm;
import com.eischet.janitor.orm.dao.Dao;
import com.eischet.janitor.orm.dao.Uplink;
import com.eischet.janitor.orm.entity.OrmEntity;
import com.eischet.janitor.orm.entity.OrmObject;
import com.eischet.janitor.orm.ref.ForeignKey;
import com.eischet.janitor.orm.ref.ForeignKeyNull;
import com.eischet.janitor.orm.sql.ColumnTypeHint;
import org.jetbrains.annotations.NotNull;

/**
 * Helper interface that makes interacting with {@link OrmEntity} easier.
 * There's a certain amount of code bloat associated with having to pass around class, name, ref etc. all the time,
 * so we're centralizing them in a single place.
 *
 * @param <T> any type of ORM entity
 * @param <U> type of uplink, that is an object that contains your DAOs
 */
public interface EntityWrangler<T extends OrmEntity, U extends Uplink> extends Wrangler<T, U> {

    @NotNull ForeignKeyNull<T> getNullReference();

    @NotNull Dao<T> retrieveDao(final @NotNull U uplink);

    default <V extends OrmObject> void addReference(final DispatchTable<V> dispatch,
                                                    final String propertyName,
                                                    final String columnName,
                                                    final NotNullGetter<V, ForeignKey<T>> getter,
                                                    final NotNullSetter<V, ForeignKey<T>> setter,
                                                    final ValueExpander<V, ForeignKey<T>> expander) {
        dispatch.addObjectPropertyWithSingletonDefault(
                        propertyName,
                        getter::get,
                        (v, value) -> setter.set(v, value == null ? getNullReference() : value),
                        getNullReference(),
                        expander)
                .setMetaData(JanitorOrm.MetaData.COLUMN_NAME, columnName)
                .setMetaData(JanitorOrm.MetaData.COLUMN_TYPE, ColumnTypeHint.INT)
                .setMetaData(Janitor.MetaData.REF, getSimpleClassName())
                .setMetaData(JanitorOrm.MetaData.WRANGLER, () -> this);
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
        OrmEntity duplicate();
    }

}
