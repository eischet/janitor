package com.eischet.janitor.orm.entity;

import com.eischet.janitor.api.types.dispatch.DispatchTable;
import com.eischet.janitor.orm.dao.GenericDao;
import com.eischet.janitor.orm.dao.OrmDaoCollection;
import com.eischet.janitor.orm.meta.EntityDispatchTable;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

/**
 * A {@link GenericDao} for entities that track their changes.
 * @param <T> the type of the entities
 * @param <U> the type of the DAO collection that this DAO belongs to
 */
public abstract class GenericChangeTrackedDao<T extends ChangeTrackedOrmEntity, U extends OrmDaoCollection<U>> extends GenericDao<T, U> {

    public GenericChangeTrackedDao(
        final @NotNull DispatchTable<? extends GenericDao<T, U>> childDispatch,
        final @NotNull OrmDaoCollection<?> collection,
        final @NotNull Class<T> entityClass,
        final @NotNull EntityDispatchTable<T, U> entityDispatch,
        final @NotNull Supplier<T> newValue
    ) {
        super(childDispatch, collection, entityClass, entityDispatch, newValue);
    }

    @Override
    public boolean isChangeTracked() {
        return true;
    }

}
