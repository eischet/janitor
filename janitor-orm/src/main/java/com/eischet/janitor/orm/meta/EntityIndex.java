package com.eischet.janitor.orm.meta;

import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.api.types.dispatch.DispatchTable;
import com.eischet.janitor.api.types.dispatch.Dispatcher;
import com.eischet.janitor.orm.dao.Dao;
import com.eischet.janitor.orm.dao.JoinDao;
import com.eischet.janitor.orm.entity.OrmEntity;
import com.eischet.janitor.orm.entity.OrmJoined;
import com.eischet.janitor.orm.ref.ForeignKeyNull;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.*;
import java.util.function.BiConsumer;

public class EntityIndex {

    private final Map<String, DispatchTable<?>> mapping = new HashMap<>();
    private final Map<String, Dao<? extends OrmEntity>> daos = new HashMap<>();
    private final Map<String, JoinDao<? extends OrmJoined>> joinDaos = new HashMap<>();

    public EntityIndex addEntity(final @NotNull String className, final @NotNull DispatchTable<?> dispatchTable) {
        mapping.put(className, dispatchTable);
        return this;
    }

    public <T extends JanitorObject> EntityIndex addEntity(final @NotNull Class<T> objectClass, final @NotNull DispatchTable<T> dispatchTable) {
        return addEntity(objectClass.getSimpleName(), dispatchTable);
    }

    /**
     * Registers an ORM dispatch table under its class name; the class name is taken from the table itself.
     * Because the table is also the wrangler, this makes the dao's counterpart, the null reference and the
     * table's other ORM features available from this one registry.
     */
    public EntityIndex addEntity(final @NotNull OrmDispatchTable<?, ?> dispatchTable) {
        return addEntity(dispatchTable.getSimpleClassName(), dispatchTable);
    }

    /**
     * Looks up the ORM dispatch table for an entity class, if it was registered as one.
     * Entities that were registered as plain {@link DispatchTable}s are not found here, use {@link #getEntity(String)} for those.
     */
    public @Nullable OrmDispatchTable<?, ?> getOrmDispatchTable(final @Nullable String className) {
        return mapping.get(className) instanceof OrmDispatchTable<?, ?> table ? table : null;
    }

    /**
     * Looks up the entity dispatch table for an entity class, if it was registered as one.
     */
    public @Nullable EntityDispatchTable<?, ?> getEntityDispatchTable(final @Nullable String className) {
        return mapping.get(className) instanceof EntityDispatchTable<?, ?> table ? table : null;
    }

    /**
     * Typed variant of {@link #getEntityDispatchTable(String)}, keyed by the entity class.
     */
    @SuppressWarnings("unchecked")
    public <T extends OrmEntity> @Nullable EntityDispatchTable<T, ?> getEntityDispatchTable(final @NotNull Class<T> entityClass) {
        return (EntityDispatchTable<T, ?>) getEntityDispatchTable(entityClass.getSimpleName());
    }

    /**
     * The null reference of an entity class, if it was registered as an {@link EntityDispatchTable}.
     */
    public <T extends OrmEntity> @Nullable ForeignKeyNull<T> getNullReference(final @NotNull Class<T> entityClass) {
        final EntityDispatchTable<T, ?> table = getEntityDispatchTable(entityClass);
        return table == null ? null : table.getNullReference();
    }

    /**
     * Typed variant of {@link #getDao(String)}, keyed by the entity class.
     */
    @SuppressWarnings("unchecked")
    public <T extends OrmEntity> @Nullable Dao<T> getDaoFor(final @NotNull Class<T> entityClass) {
        return (Dao<T>) daos.get(entityClass.getSimpleName());
    }

    public @Nullable DispatchTable<?> getEntity(final String className) {
        return mapping.get(className);
    }

    public @Unmodifiable Set<String> getEntityNames() {
        return Set.copyOf(mapping.keySet());
    }

    public @Unmodifiable Collection<DispatchTable<?>> getEntities() {
        return List.copyOf(mapping.values());
    }

    public @NotNull Set<String> getJoinNames() {
        return Set.copyOf(joinDaos.keySet());
    }

    public Dao<? extends OrmEntity> getDao(final @Nullable String className) {
        return daos.get(className);
    }

    public void setDao(final String className, final Dao<? extends OrmEntity> dao) {
        daos.put(className, dao);
    }

    public void setJoinDao(final String className, final JoinDao<? extends OrmJoined> joinDao) {
        joinDaos.put(className, joinDao);
    }

    public JoinDao<? extends OrmJoined> getJoinDao(final @Nullable String className) {
        return joinDaos.get(className);
    }

    public void forEachDispatchTable(final BiConsumer<String, Dispatcher<?>> consumer) {
        mapping.forEach(consumer);
        joinDaos.forEach((n, d) -> consumer.accept(n, d.getDispatcher()));
    }



}
