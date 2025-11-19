package com.eischet.janitor.orm.meta;

import com.eischet.janitor.api.errors.glue.JanitorGlueException;
import com.eischet.janitor.api.errors.runtime.JanitorError;
import com.eischet.janitor.api.types.JAssignable;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.api.types.TemporaryAssignable;
import com.eischet.janitor.api.types.dispatch.DispatchTable;
import com.eischet.janitor.orm.dao.Dao;
import com.eischet.janitor.orm.dao.Uplink;
import com.eischet.janitor.orm.entity.OrmEntity;
import com.eischet.janitor.orm.ref.ForeignKeyNull;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Function;

import static com.eischet.janitor.api.util.ObjectUtilities.simpleClassNameOf;

public class SimpleWrangler<T extends OrmEntity, U extends Uplink> implements EntityWrangler<T, U> {

    private static final Logger log = LoggerFactory.getLogger(SimpleWrangler.class);

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
            } catch (JanitorGlueException e) {
                throw new JanitorError("error copying attribute " + attr + " from " + original + " to a new copy", e);
            }
        });
        return copy;
    }

}
