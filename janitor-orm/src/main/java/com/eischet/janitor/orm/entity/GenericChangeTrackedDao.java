package com.eischet.janitor.orm.entity;

import com.eischet.janitor.api.types.dispatch.DispatchTable;
import com.eischet.janitor.orm.dao.GenericDao;
import com.eischet.janitor.orm.meta.EntityIndex;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public abstract class GenericChangeTrackedDao<T extends ChangeTrackedOrmEntity> extends GenericDao<T> {

    public GenericChangeTrackedDao(
        final @NotNull DispatchTable<? extends GenericDao<T>> childDispatch,
        final @NotNull EntityIndex entityIndex,
        final @NotNull Class<T> entityClass,
        final @NotNull DispatchTable<T> entityDispatch,
        final @NotNull Supplier<T> newValue
    ) {
        super(childDispatch, entityIndex, entityClass, entityDispatch, newValue);
    }

    @Override
    public boolean isChangeTracked() {
        return true;
    }

}
