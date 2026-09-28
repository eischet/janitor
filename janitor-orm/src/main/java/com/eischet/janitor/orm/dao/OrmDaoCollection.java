package com.eischet.janitor.orm.dao;

import com.eischet.dbxs.DataManager;
import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.types.composed.JanitorComposed;
import com.eischet.janitor.api.types.dispatch.DispatchTable;
import com.eischet.janitor.api.types.dispatch.Dispatcher;
import com.eischet.janitor.orm.entity.OrmEntity;
import com.eischet.janitor.orm.entity.OrmJoined;
import com.eischet.janitor.orm.meta.EntityDispatchTable;
import com.eischet.janitor.orm.meta.OrmDispatchTable;
import com.eischet.janitor.orm.ref.ForeignKeyNull;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.*;
import java.util.function.BiConsumer;

/**
 * Base class for the object that holds all the DAOs of an application, which is the "uplink" of the ORM.
 * <p>
 * Subclasses declare their DAOs as explicit fields with explicit getters, e.g. {@code getPersonDao()}; there is
 * deliberately no generic "get(Class)" accessor, so that code completion can offer all the DAOs.
 * </p>
 * <p>
 * This class is also the registry that maps entity class names to dispatch tables and DAOs. DAOs register themselves
 * (and the dispatch table of their entity) when they are constructed, so nothing needs to be listed by hand.
 * The lookup by name exists because foreign keys refer to their target by class name ({@code Janitor.MetaData.REF}).
 * </p>
 *
 * @param <S> the type of the subclass, e.g. {@code MyDaoCollection extends OrmDaoCollection<MyDaoCollection>}
 */
public abstract class OrmDaoCollection<S extends OrmDaoCollection<S>> extends JanitorComposed<S> implements Uplink {

    private final Map<String, DispatchTable<?>> entities = new LinkedHashMap<>();
    private final Map<String, Dao<? extends OrmEntity>> daos = new LinkedHashMap<>();
    private final Map<String, JoinDao<? extends OrmJoined>> joinDaos = new LinkedHashMap<>();

    protected OrmDaoCollection(final @NotNull Dispatcher<S> dispatcher) {
        super(dispatcher);
    }

    /**
     * The data manager that all DAOs of this collection use by default.
     *
     * @return the data manager
     */
    public abstract DataManager getDataManager();

    /**
     * Adds the scripting properties "entities" and "joins", which list all DAOs, to a dispatch table for this collection.
     * Call this once, e.g. in the static initializer that sets up your dispatch table.
     *
     * @param dispatch the dispatch table of your subclass
     * @param <S>      the subclass
     */
    public static <S extends OrmDaoCollection<S>> void addRegistryProperties(final @NotNull DispatchTable<S> dispatch) {
        dispatch.addListProperty("entities", self -> Janitor.list(self.getEntityNames().stream().map(self::getDao).filter(Objects::nonNull)));
        dispatch.addListProperty("joins", self -> Janitor.list(self.getJoinNames().stream().map(self::getJoinDao).filter(Objects::nonNull)));
    }

    // registration: called by the DAO constructors

    void registerDao(final @NotNull String className, final @NotNull Dao<? extends OrmEntity> dao, final @NotNull DispatchTable<?> entityDispatch) {
        entities.put(className, entityDispatch);
        daos.put(className, dao);
    }

    void registerJoinDao(final @NotNull String className, final @NotNull JoinDao<? extends OrmJoined> dao, final @NotNull DispatchTable<?> entityDispatch) {
        joinDaos.put(className, dao);
    }

    // lookup by class name, as needed for foreign keys and scripts

    public @Nullable Dao<? extends OrmEntity> getDao(final @Nullable String className) {
        return daos.get(className);
    }

    public @Nullable JoinDao<? extends OrmJoined> getJoinDao(final @Nullable String className) {
        return joinDaos.get(className);
    }

    public @Nullable DispatchTable<?> getEntity(final @Nullable String className) {
        return entities.get(className);
    }

    public @Unmodifiable Set<String> getEntityNames() {
        return Collections.unmodifiableSet(new LinkedHashSet<>(entities.keySet()));
    }

    public @Unmodifiable Set<String> getJoinNames() {
        return Collections.unmodifiableSet(new LinkedHashSet<>(joinDaos.keySet()));
    }

    public @Unmodifiable Collection<DispatchTable<?>> getEntities() {
        return List.copyOf(entities.values());
    }

    public @Unmodifiable Collection<Dao<? extends OrmEntity>> getDaos() {
        return List.copyOf(daos.values());
    }

    public void forEachDispatchTable(final BiConsumer<String, Dispatcher<?>> consumer) {
        entities.forEach(consumer);
        joinDaos.forEach((name, dao) -> consumer.accept(name, dao.getDispatcher()));
    }

    /**
     * Looks up the ORM dispatch table for an entity class, if the DAO was created with one.
     */
    public @Nullable OrmDispatchTable<?, ?> getOrmDispatchTable(final @Nullable String className) {
        return entities.get(className) instanceof OrmDispatchTable<?, ?> table ? table : null;
    }

    /**
     * Looks up the entity dispatch table for an entity class, if the DAO was created with one.
     */
    public @Nullable EntityDispatchTable<?, ?> getEntityDispatchTable(final @Nullable String className) {
        return entities.get(className) instanceof EntityDispatchTable<?, ?> table ? table : null;
    }

    /**
     * Typed variant of {@link #getEntityDispatchTable(String)}, keyed by the entity class.
     */
    @SuppressWarnings("unchecked")
    public <T extends OrmEntity> @Nullable EntityDispatchTable<T, ?> getEntityDispatchTable(final @NotNull Class<T> entityClass) {
        return (EntityDispatchTable<T, ?>) getEntityDispatchTable(entityClass.getSimpleName());
    }

    /**
     * The null reference of an entity class, if the DAO was created with an {@link EntityDispatchTable}.
     */
    public <T extends OrmEntity> @Nullable ForeignKeyNull<T> getNullReference(final @NotNull Class<T> entityClass) {
        final EntityDispatchTable<T, ?> table = getEntityDispatchTable(entityClass);
        return table == null ? null : table.getNullReference();
    }

}
