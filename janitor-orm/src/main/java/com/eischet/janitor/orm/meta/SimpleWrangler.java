package com.eischet.janitor.orm.meta;

import com.eischet.janitor.api.types.dispatch.DispatchTable;
import com.eischet.janitor.orm.dao.Dao;
import com.eischet.janitor.orm.dao.Uplink;
import com.eischet.janitor.orm.entity.OrmEntity;
import com.eischet.janitor.orm.ref.ForeignKeyNull;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public class SimpleWrangler<T extends OrmEntity, U extends Uplink> implements EntityWrangler<T, U> {

    private final Class<T> wrangledClass;
    private final ForeignKeyNull<T> nullReference;
    private final DispatchTable<T> dispatchTable;
    private final Function<U, T> constructor;
    private final Function<U, Dao<T>> downlinkRetriever;

    public SimpleWrangler(
            final Class<T> wrangledClass,
            final DispatchTable<T> dispatchTable,
            final ForeignKeyNull<T> nullReference,
            final Function<U, T> constructor,
            final Function<U, Dao<T>> downlinkRetriever
    ) {
        this.wrangledClass = wrangledClass;
        this.dispatchTable = dispatchTable;
        this.nullReference = nullReference;
        this.constructor = constructor;
        this.downlinkRetriever = downlinkRetriever;
    }

    public static <T extends OrmEntity, U extends Uplink> EntityWrangler<T, U> of(final Class<T> wrangledClass,
                                                                                  final DispatchTable<T> dispatchTable,
                                                                                  final ForeignKeyNull<T> nullReference,
                                                                                  final Function<U, T> constructor,
                                                                                  final Function<U, Dao<T>> downlinkRetriever) {
        return new SimpleWrangler<>(wrangledClass,
                dispatchTable,
                nullReference,
                constructor,
                downlinkRetriever);
    }


    @Override
    public @NotNull DispatchTable<T> getDispatchTable() {
        return dispatchTable;
    }


    @Override
    public @NotNull Class<T> getWrangledClass() {
        return wrangledClass;
    }

    @Override
    public @NotNull String getSimpleClassName() {
        return wrangledClass.getSimpleName();
    }

    @Override
    public @NotNull ForeignKeyNull<T> getNullReference() {
        return nullReference;
    }

    @Override
    public @NotNull T createNewInstance(@NotNull final U uplink) {
        return constructor.apply(uplink);
    }

    @Override
    public @NotNull Dao<T> retrieveDao(@NotNull final U uplink) {
        return downlinkRetriever.apply(uplink);
    }

    public Function<U, T> getConstructor() {
        return constructor;
    }

    @Override
    public String toString() {
        return "SimpleWrangler{" + wrangledClass.getSimpleName() + "}";
    }
}
