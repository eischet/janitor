package com.eischet.janitor.orm.meta;

import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.types.dispatch.DispatchTable;
import com.eischet.janitor.api.types.dispatch.ValueExpander;
import com.eischet.janitor.orm.JanitorOrm;
import com.eischet.janitor.orm.dao.Dao;
import com.eischet.janitor.orm.dao.Uplink;
import com.eischet.janitor.orm.entity.OrmEntity;
import com.eischet.janitor.orm.entity.OrmObject;
import com.eischet.janitor.orm.ref.ForeignKey;
import com.eischet.janitor.orm.ref.ForeignKeyNull;
import com.eischet.janitor.orm.sql.ColumnTypeHint;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Helper interface that makes interacting with {@link OrmEntity} easier.
 * There's a certain amount of code bloat associated with having to pass around class, name, ref etc. all the time,
 * so we're centralizing them in a single place.
 * @param <T> any type of ORM entity
 * @param <U> type of uplink, that is an object that contains your DAOs
 */
public interface EntityWrangler<T extends OrmEntity, U extends Uplink> extends Wrangler<T, U> {

    @NotNull ForeignKeyNull<T> getNullReference();
    @NotNull Dao<T> retrieveDao(final @NotNull U uplink);

    default <V extends OrmObject> void addReference(final DispatchTable<V> dispatch,
                                                    final String propertyName,
                                                    final String columnName,
                                                    final Function<V, ForeignKey<T>> getter,
                                                    final BiConsumer<V, ForeignKey<T>> setter,
                                                    final ValueExpander<V, ForeignKey<T>> expander) {
        dispatch.addObjectPropertyWithSingletonDefault(propertyName, getter, setter, getNullReference(), expander)
                .setMetaData(JanitorOrm.MetaData.COLUMN_NAME, columnName)
                .setMetaData(JanitorOrm.MetaData.COLUMN_TYPE, ColumnTypeHint.INT)
                .setMetaData(Janitor.MetaData.REF, getSimpleClassName())
                .setMetaData(JanitorOrm.MetaData.WRANGLER, () -> this);
    }

}
