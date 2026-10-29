// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.orm.dao;

import com.eischet.dbxs.DataManager;
import com.eischet.dbxs.dialects.DatabaseDialect;
import com.eischet.dbxs.metadata.DatabaseVersion;
import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.types.composed.JanitorComposed;
import com.eischet.janitor.api.types.dispatch.DispatchTable;
import com.eischet.janitor.api.types.dispatch.Dispatcher;
import com.eischet.janitor.orm.entity.OrmEntity;
import com.eischet.janitor.orm.entity.OrmJoined;
import com.eischet.janitor.orm.meta.EntityDispatchTable;
import com.eischet.janitor.orm.meta.OrmDispatchTable;
import com.eischet.janitor.orm.ref.ForeignKeyNull;
import com.eischet.janitor.versioning.Version;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.time.Clock;
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

    protected final Map<String, DispatchTable<?>> entities = new LinkedHashMap<>();
    protected final Map<String, Dao<? extends OrmEntity>> daos = new LinkedHashMap<>();
    protected final Map<String, JoinDao<? extends OrmJoined>> joinDaos = new LinkedHashMap<>();
    /** Stands in for "no version given": newer than any real one, so that only dialects that cannot limit rows at all say no. */
    protected static final DatabaseVersion MODERN_DATABASE = new DatabaseVersion();

    static {
        MODERN_DATABASE.setProductName("unspecified, assumed modern");
        MODERN_DATABASE.setMajorVersion(Integer.MAX_VALUE);
        MODERN_DATABASE.setMinorVersion(Integer.MAX_VALUE);
    }

    protected final @NotNull DatabaseDialect dialect;
    protected final @Nullable Version schemaVersion;
    protected final @Nullable DatabaseVersion databaseVersion;
    protected final boolean canLimitAndOffset;

    /**
     * Creates a collection that is bound to one database for its whole life; a different database gets a new collection (and new DAOs).
     *
     * @param dispatcher      the dispatcher for scripts
     * @param dialect         the dialect of the database; all DAOs of this collection generate their SQL with it
     * @param schemaVersion   the version of the schema that the entities map to, used to skip fields that do not exist in that version;
     *                        {@code null} if there is no versioning
     * @param databaseVersion the product version of the database, which only matters for old versions of some databases (e.g. Oracle before 12
     *                        or SQL Server before 2012 cannot limit rows in SQL). {@code null} means "a modern database", which is what most
     *                        callers want. Callers that do have to support legacy databases are responsible for passing the real version, e.g. via
     *                        {@link DatabaseVersion#getDatabaseVersion(DataManager)}; a wrong {@code null} leads to SQL errors on old databases.
     */
    public OrmDaoCollection(final @NotNull Dispatcher<S> dispatcher,
                            final @NotNull DatabaseDialect dialect,
                            final @Nullable Version schemaVersion,
                            final @Nullable DatabaseVersion databaseVersion) {
        super(dispatcher);
        this.dialect = Objects.requireNonNull(dialect, "dialect");
        this.schemaVersion = schemaVersion;
        this.databaseVersion = databaseVersion;
        this.canLimitAndOffset = dialect.canLimitAndOffset(databaseVersion == null ? MODERN_DATABASE : databaseVersion);
    }

    /**
     * Returns the schema version of this collection.
     *
     * @return the schema version, which can be {@code null} if no version was specified
     */
    public @Nullable Version getSchemaVersion() {
        return schemaVersion;
    }

    /**
     * The data manager that all DAOs of this collection use by default.
     *
     * @return the data manager
     */
    public abstract DataManager getDataManager();

    /**
     * The dialect of the database that this collection, and therefore all of its DAOs, work against. DAOs take the dialect from here and
     * nowhere else.
     *
     * @return the dialect
     */
    public @NotNull DatabaseDialect getDialect() {
        return dialect;
    }

    /**
     * The version of the database, as given to the constructor. Not to be confused with {@link #getSchemaVersion()}, which is the version of
     * the schema that the entities are mapped to.
     *
     * @return the database version, or {@code null} if none was given, which means that the database is assumed to be modern
     */
    public @Nullable DatabaseVersion getDatabaseVersion() {
        return databaseVersion;
    }

    /**
     * Whether DAOs may let the database limit the number of returned rows ({@code LIMIT}/{@code OFFSET} or the equivalent).
     * Decided once, in the constructor, from the dialect and the database version.
     *
     * @return true if rows can be limited in SQL
     */
    public boolean canLimitAndOffset() {
        return canLimitAndOffset;
    }

    /**
     * The clock that DAOs use for timestamps they maintain themselves, e.g. the modification date, see
     * {@link com.eischet.janitor.orm.JanitorOrm.MetaData#MODIFIED_FIELD}. Override this in tests to get reproducible values.
     *
     * @return the clock, by default the system clock in the default time zone
     */
    public @NotNull Clock getClock() {
        return Clock.systemDefaultZone();
    }

    /**
     * The id that reference columns of all DAOs in this collection hold when they refer to nothing, unless a column says otherwise with
     * {@link com.eischet.janitor.orm.JanitorOrm.MetaData#NULL_REFERENCE_ID}. Override this for schemas whose reference columns are
     * {@code NOT NULL} and point to a system row instead, e.g. {@code return 0L;} for Assyst.
     * <p>
     * The row with this id is then no record of its own: the DAOs ({@link GenericDao}) never return it from any query, by id, key, filter or
     * otherwise, and refuse to update or delete it.
     * </p>
     * <p>
     * The DAOs ask for this while they are constructed, which usually happens in the constructor of the collection, so an override
     * must not depend on fields of the subclass: return a constant.
     * </p>
     *
     * @return the id, or {@code null} (the default) to write NULL
     */
    public @Nullable Long getNullReferenceId() {
        return null;
    }

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

    /**
     * Registers a DAO, so that it can be found by the name of its entity class.
     * @param dao the DAO
     */
    protected void registerDao(final @NotNull Dao<? extends OrmEntity> dao) {
        entities.put(dao.getEntityClassName(), dao.getEntityDispatchTable());
        daos.put(dao.getEntityClassName(), dao);
    }

    /**
     * Registers a DAO for a join table, so that it can be found by the name of its entity class.
     * @param dao the DAO
     */
    protected void registerJoinDao(final @NotNull JoinDao<? extends OrmJoined> dao) {
        joinDaos.put(dao.getEntityClassName(), dao);
    }

    // lookup by class name, as needed for foreign keys and scripts

    /**
     * Looks up the DAO of an entity class.
     * @param className the simple name of the entity class
     * @return the DAO, or null if there is none
     */
    public @Nullable Dao<? extends OrmEntity> getDao(final @Nullable String className) {
        return daos.get(className);
    }

    /**
     * Looks up the DAO of a join table.
     * @param className the simple name of the join record class
     * @return the DAO, or null if there is none
     */
    public @Nullable JoinDao<? extends OrmJoined> getJoinDao(final @Nullable String className) {
        return joinDaos.get(className);
    }

    /**
     * Looks up the dispatch table of an entity class.
     * @param className the simple name of the entity class
     * @return the dispatch table, or null if there is none
     */
    public @Nullable DispatchTable<?> getEntity(final @Nullable String className) {
        return entities.get(className);
    }

    /**
     * @return the names of all registered entity classes
     */
    public @Unmodifiable Set<String> getEntityNames() {
        return Collections.unmodifiableSet(new LinkedHashSet<>(entities.keySet()));
    }

    /**
     * @return the names of all registered join record classes
     */
    public @Unmodifiable Set<String> getJoinNames() {
        return Collections.unmodifiableSet(new LinkedHashSet<>(joinDaos.keySet()));
    }

    /**
     * @return the dispatch tables of all registered entities
     */
    public @Unmodifiable Collection<DispatchTable<?>> getEntities() {
        return List.copyOf(entities.values());
    }

    /**
     * @return all registered DAOs
     */
    public @Unmodifiable Collection<Dao<? extends OrmEntity>> getDaos() {
        return List.copyOf(daos.values());
    }

    /**
     * Calls the consumer for the dispatch table of each entity and join table.
     * @param consumer receives the name of the entity class and its dispatcher
     */
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
