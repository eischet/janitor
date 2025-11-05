package com.eischet.janitor.orm.meta;

import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.api.types.dispatch.DispatchTable;
import com.eischet.janitor.orm.dao.Dao;
import com.eischet.janitor.orm.dao.JoinDao;
import com.eischet.janitor.orm.entity.OrmEntity;
import com.eischet.janitor.orm.entity.OrmJoined;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.*;

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

}
